package com.farmaciavigor.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object Fotos {

    private fun pastaFotos(contexto: Context): File =
        File(contexto.filesDir, "fotos").apply { mkdirs() }

    fun arquivo(contexto: Context, nome: String): File =
        File(pastaFotos(contexto), nome)

    /** Copia a imagem apontada por [uri] para o armazenamento do app e retorna o nome do arquivo. */
    suspend fun salvarDeUri(contexto: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val nome = "injecao_${System.currentTimeMillis()}.jpg"
            contexto.contentResolver.openInputStream(uri)?.use { entrada ->
                arquivo(contexto, nome).outputStream().use { saida -> entrada.copyTo(saida) }
            } ?: return@withContext null
            nome
        }.getOrNull()
    }

    /** Cria um destino temporário para a câmera gravar a foto da caixa. */
    fun uriParaCamera(contexto: Context): Uri {
        val pasta = File(contexto.cacheDir, "fotos_temp").apply { mkdirs() }
        val arquivo = File(pasta, "captura_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(contexto, contexto.packageName + ".fileprovider", arquivo)
    }
}
