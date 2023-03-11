package daylightnebula.papercustomresourcetoolkit

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipManager {
    fun zip(folder: File, target: File) {
        val zipOutput = ZipOutputStream(target.outputStream())
        zipFiles(zipOutput, folder, "")
        zipOutput.close()
    }

    private fun zipFiles(zipOut: ZipOutputStream, sourceDir: File, parentDirPath: String) {
        val data = ByteArray(2048)
        for (f in sourceDir.listFiles()!!) {
            if (f.isDirectory) {
                zipFiles(zipOut, f, parentDirPath + f.name + File.separator)
            } else {
                if (!f.name.contains(".zip")) { //If folder contains a file with extension ".zip", skip it
                    FileInputStream(f).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val path = parentDirPath + f.name
                            println("Adding file: $path")
                            val entry = ZipEntry(path)
                            entry.time = f.lastModified()
                            entry.size = f.length()
                            entry.isDirectory
                            zipOut.putNextEntry(entry)
                            while (true) {
                                val readBytes = origin.read(data)
                                if (readBytes == -1) { break }
                                zipOut.write(data, 0, readBytes)
                            }
                        }
                    }
                } else {
                    zipOut.closeEntry()
                    zipOut.close()
                }
            }
        }
    }
}