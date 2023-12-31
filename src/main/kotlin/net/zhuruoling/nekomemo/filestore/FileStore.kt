package net.zhuruoling.nekomemo.filestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.io.path.*

object FileStore {
    val files = mutableMapOf<String, File>()

    fun init() {
        files.clear()
        val dir = Path("./data")
        if (dir.exists() || !dir.isDirectory()) {
            dir.toFile().mkdirs()
        }
        files += dir.listDirectoryEntries("*.neko").map(Path::toFile).map { it.name to it }
    }

    fun openFile(fileName: String): InputStream {
        return files[fileName]?.inputStream() ?: throw FileNotFoundException("File $fileName not found.")
    }

    suspend fun <T> useFile(fileName: String, block: suspend (InputStream) -> T): T {
        val file = files[fileName] ?: throw FileNotFoundException("File $fileName not found.")
        return withContext(Dispatchers.IO) {
            val inputStream = file.inputStream()
            val t = block(inputStream)
            inputStream.close()
            t
        }
    }

    suspend fun createNewFile(fileName: String, replace: Boolean = false, block: suspend (OutputStream) -> Unit) {
        if (fileName in files && !replace) throw FileAlreadyExistsException(fileName)
        val file = File("./data/$fileName.neko")
        withContext(Dispatchers.IO) {
            file.toPath().apply {
                deleteIfExists()
                createFile()
            }
            files += fileName to file
            val stream = file.outputStream()
            block(stream)
            stream.close()
        }
    }

    fun filter(pattern: String? = null): Map<String, File>{
        return synchronized(files){
            val exp = Pattern.compile(pattern ?: return files)
            buildMap {
                this += files.filter { exp.matcher(it.key).matches() }
            }
        }
    }

    fun deleteFile(fileName: String) {
       synchronized(files) {
            val file = files[fileName] ?: throw FileNotFoundException("File $fileName not found.")
            if (!file.exists()) throw FileNotFoundException("File $fileName not found.")
            file.delete()
            files.remove(fileName)
        }
    }
}