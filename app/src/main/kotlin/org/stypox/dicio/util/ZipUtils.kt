/*
 * Taken from /e/OS Assistant
 *
 * Copyright (C) 2024 MURENA SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.stypox.dicio.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.stypox.dicio.ui.util.Progress
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

private const val CHUNK_SIZE = 1024 * 256 // 0.25 MB

suspend fun extractZip(
    sourceZip: File,
    destinationDirectory: File,
    progressCallback: (Progress) -> Unit,
) {
    withContext(Dispatchers.IO) { ZipFile(sourceZip) }.use { zipFile ->
        // counting just files
        var currentCount = 0
        var totalCount = 0
        for (entry in zipFile.entries()) {
            if (!entry.isDirectory) {
                totalCount += 1
            }
        }

        for (entry in zipFile.entries()) {
            val destinationFile = withContext(Dispatchers.IO) {
                getDestinationFile(destinationDirectory, entry.name)
            }

            if (entry.isDirectory) {
                // create directory
                if (withContext(Dispatchers.IO) {
                        !destinationFile.exists() && !destinationFile.mkdirs()
                    }) {
                    throw IOException("mkdirs failed: $destinationFile")
                }
                continue
            }

            // else copy file
            zipFile.getInputStream(entry).use { inputStream ->
                BufferedOutputStream(FileOutputStream(destinationFile)).use { outputStream ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var length: Int
                    var currentBytes: Long = 0
                    progressCallback(Progress(currentCount, totalCount, 0, entry.size))

                    while (inputStream.read(buffer).also { length = it } > 0) {
                        yield() // manually yield because the input/output streams are blocking
                        outputStream.write(buffer, 0, length)
                        currentBytes += length
                        progressCallback(
                            Progress(currentCount, totalCount, currentBytes, entry.size)
                        )
                    }
                    outputStream.flush()
                }
            }

            currentCount += 1
        }
    }
}

/**
 * Returns the path inside [destinationDirectory] where to save the zip entry with name [entryName],
 * while protecting from Zip Slip vulnerabilities by checking whether the actual generated path is
 * inside [destinationDirectory].
 */
@Throws(IOException::class)
fun getDestinationFile(destinationDirectory: File, entryName: String): File {
    // model files are under a subdirectory, so get the path after the first /
    val filePath = entryName.substring(entryName.indexOf('/') + 1)

    // protect from Zip Slip vulnerability (!)
    val destinationFile = File(destinationDirectory, filePath)
    if (destinationDirectory.canonicalPath != destinationFile.canonicalPath
        && !destinationFile.canonicalPath.startsWith(
            destinationDirectory.canonicalPath + File.separator
        )
    ) {
        throw IOException("Entry is outside of the target dir: $entryName")
    }
    return destinationFile
}
