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

import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Cycles through every entry of the zip file.
 */
inline fun ZipInputStream.useEntries(forEachEntry: ZipInputStream.(entry: ZipEntry) -> Unit) {
    use { zipInputStream ->
        // cycles through all entries
        while (true) {
            val entry: ZipEntry = zipInputStream.nextEntry ?: break
            forEachEntry(entry)
            zipInputStream.closeEntry()
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
