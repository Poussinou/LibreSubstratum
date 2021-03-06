package com.jereksel.libresubstratum.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object ZipUtils {

    fun File.extractZip(dest: File, prefix: String = "", progressCallback: (Int) -> Unit = {},
                        streamTransform: (InputStream) -> InputStream = { it }) {
        if (dest.exists()) {
            dest.deleteRecursively()
        }
        dest.mkdirs()

        val length = ZipFile(this).size()

        FileInputStream(this).use { fis ->
            ZipInputStream(BufferedInputStream(fis)).use { zis ->
                zis.generateSequence().forEachIndexed { index, ze ->

                    val fileName = ze.name.removeSuffix(".enc")

                    progressCallback((index * 100) / length)

//                    Log.d("extractZip", fileName)

                    if (!fileName.startsWith(prefix)) {
                        return@forEachIndexed
                    }

                    if (ze.isDirectory) {
                        File(dest, fileName).mkdirs()
                        return@forEachIndexed
                    }

                    val destFile = File(dest, fileName)

                    destFile.parentFile.mkdirs()
                    destFile.createNewFile()

                    FileOutputStream(destFile).use { fout ->
                        streamTransform(zis).copyTo(fout)
                    }

                    if (Thread.interrupted()) {
                        return
                    }

                }
            }
        }
    }

    //We can't just use second function alone - we will close entry when there is no entry opened yet
    fun ZipInputStream.generateSequence() = generateSequence({ this.nextEntry }, { this.closeEntry(); this.nextEntry })

}