package com.smushytaco.exec_patcher

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.io.path.*

@CacheableTask
abstract class PatchSourcesJarTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val inputSourcesJar: RegularFileProperty

    @get:OutputDirectory
    abstract val expandedDir: DirectoryProperty

    @get:OutputFile
    abstract val outputSourcesJar: RegularFileProperty

    @TaskAction
    fun run() {
        val inJar = inputSourcesJar.get().asFile.toPath()
        val outDir = expandedDir.get().asFile.toPath()
        val outJar = outputSourcesJar.get().asFile.toPath()

        Files.createDirectories(outDir)
        Files.createDirectories(outJar.parent)

        unzip(inJar, outDir)

        SpoonSourcePatcher.main(arrayOf(outDir.toString()))

        zip(outDir, outJar)
    }

    private fun unzip(zip: Path, toDir: Path) {
        ZipInputStream(zip.inputStream()).use { zin ->
            var e: ZipEntry? = zin.nextEntry
            while (e != null) {
                val outPath = toDir.resolve(e.name)
                if (e.isDirectory) {
                    Files.createDirectories(outPath)
                } else {
                    Files.createDirectories(outPath.parent)
                    outPath.outputStream().use { zin.copyTo(it) }
                }
                zin.closeEntry()
                e = zin.nextEntry
            }
        }
    }

    private fun zip(fromDir: Path, toZip: Path) {
        ZipOutputStream(toZip.outputStream()).use { zOut ->
            Files.walk(fromDir).use { stream ->
                stream.filter { Files.isRegularFile(it) }.forEach { file ->
                    val rel = fromDir.relativize(file).toString().replace("\\", "/")
                    zOut.putNextEntry(ZipEntry(rel))
                    file.inputStream().use { it.copyTo(zOut) }
                    zOut.closeEntry()
                }
            }
        }
    }
}
