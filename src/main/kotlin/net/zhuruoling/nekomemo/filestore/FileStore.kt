package net.zhuruoling.nekomemo.filestore

import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

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

    fun openFile(fileName: String):InputStream{
        return files[fileName]?.inputStream() ?: throw FileNotFoundException("File $fileName not found.")
    }

    fun scheduleDeleteFile(fileName: String){
        val file = files[fileName] ?: throw FileNotFoundException("File $fileName not found.")
    }
}