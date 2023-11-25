package net.zhuruoling.nekomemo.filestore

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

object FileStore {

    val files = mutableListOf<File>()

    fun init(){
        val dir = Path("./data")
        if (dir.exists() || !dir.isDirectory()){
            dir.toFile().mkdirs()
        }
        dir.listDirectoryEntries("*.neko").map(Path::toFile).forEach(files::add)
    }
}