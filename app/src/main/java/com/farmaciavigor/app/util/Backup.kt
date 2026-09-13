package com.farmaciavigor.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.farmaciavigor.app.dados.BancoDeDados
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Backup manual completo (banco + fotos) num único arquivo .zip, que o usuário
 * salva onde quiser pelo seletor do sistema (Google Drive, pen drive, etc.) e
 * restaura ao trocar de tablet ou reinstalar o app.
 *
 * Estrutura do zip:
 *   db/farmacia_vigor.db   → cópia consistente do banco (via VACUUM INTO)
 *   fotos/<arquivo>.jpg    → cada foto de caixa cadastrada
 */
object Backup {
    private const val NOME_DB = "farmacia_vigor.db"
    private const val PASTA_DB = "db"
    private const val PASTA_FOTOS = "fotos"

    /** Nome sugerido do arquivo, com a data de hoje. */
    fun nomeSugerido(): String {
        val agora = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        return "farmacia-vigor-backup-$agora.zip"
    }

    suspend fun exportar(contexto: Context, banco: BancoDeDados, destino: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                // VACUUM INTO gera um arquivo único e consistente do banco, mesmo com WAL aberto.
                val dbTemp = File(contexto.cacheDir, "export_${System.currentTimeMillis()}.db")
                dbTemp.delete()
                banco.openHelper.writableDatabase
                    .execSQL("VACUUM INTO '${dbTemp.absolutePath.replace("'", "''")}'")

                val fotos = File(contexto.filesDir, PASTA_FOTOS)
                try {
                    contexto.contentResolver.openOutputStream(destino)?.use { saida ->
                        ZipOutputStream(BufferedOutputStream(saida)).use { zip ->
                            zipArquivo(zip, "$PASTA_DB/$NOME_DB", dbTemp)
                            fotos.listFiles()?.forEach { arquivo ->
                                zipArquivo(zip, "$PASTA_FOTOS/${arquivo.name}", arquivo)
                            }
                        }
                    } ?: throw IOException("Não foi possível escrever no destino escolhido.")
                } finally {
                    dbTemp.delete()
                }
                Unit
            }
        }

    /**
     * Substitui o banco e as fotos atuais pelo conteúdo do zip. Valida o arquivo
     * antes de apagar qualquer coisa; se o zip for inválido, nada é alterado.
     * Após restaurar, o app precisa ser reiniciado ([reiniciarApp]) para o Room
     * reabrir a partir do banco novo.
     */
    suspend fun importar(contexto: Context, origem: Uri): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val temp = File(contexto.cacheDir, "restore_${System.currentTimeMillis()}")
                temp.mkdirs()
                try {
                    contexto.contentResolver.openInputStream(origem)?.use { entrada ->
                        ZipInputStream(BufferedInputStream(entrada)).use { zip ->
                            var entrada2: ZipEntry? = zip.nextEntry
                            while (entrada2 != null) {
                                val alvo = File(temp, entrada2.name)
                                // Proteção contra zip malicioso (path traversal).
                                if (!alvo.canonicalPath.startsWith(temp.canonicalPath + File.separator)) {
                                    throw IOException("Arquivo de backup inválido.")
                                }
                                if (entrada2.isDirectory) {
                                    alvo.mkdirs()
                                } else {
                                    alvo.parentFile?.mkdirs()
                                    alvo.outputStream().use { zip.copyTo(it) }
                                }
                                zip.closeEntry()
                                entrada2 = zip.nextEntry
                            }
                        }
                    } ?: throw IOException("Não foi possível ler o arquivo escolhido.")

                    val dbNovo = File(temp, "$PASTA_DB/$NOME_DB")
                    if (!dbNovo.exists() || dbNovo.length() == 0L) {
                        throw IOException("Este arquivo não é um backup válido da Farmácia Vigor.")
                    }

                    // Só chega aqui se o backup é válido: substitui banco e fotos.
                    val dbAtual = contexto.getDatabasePath(NOME_DB)
                    dbAtual.parentFile?.mkdirs()
                    listOf(dbAtual, File("${dbAtual.path}-wal"), File("${dbAtual.path}-shm"))
                        .forEach { it.delete() }
                    dbNovo.copyTo(dbAtual, overwrite = true)

                    val fotos = File(contexto.filesDir, PASTA_FOTOS)
                    fotos.deleteRecursively()
                    fotos.mkdirs()
                    File(temp, PASTA_FOTOS).listFiles()?.forEach { arquivo ->
                        arquivo.copyTo(File(fotos, arquivo.name), overwrite = true)
                    }
                } finally {
                    temp.deleteRecursively()
                }
                Unit
            }
        }

    /** Reinicia o app do zero — necessário depois de restaurar para o banco novo entrar em uso. */
    fun reiniciarApp(contexto: Context) {
        val intent = contexto.packageManager.getLaunchIntentForPackage(contexto.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        contexto.startActivity(intent)
        Runtime.getRuntime().exit(0)
    }

    private fun zipArquivo(zip: ZipOutputStream, caminho: String, arquivo: File) {
        zip.putNextEntry(ZipEntry(caminho))
        arquivo.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }
}
