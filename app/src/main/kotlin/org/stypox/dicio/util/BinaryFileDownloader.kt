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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream

private const val CHUNK_SIZE = 1024 * 256 // 0.25 MB
private const val CONTENT_LENGTH = "Content-Length"
private const val DOT_PART_SUFFIX = ".part"

/**
 * Deletes any partial files in the application's cache directory.
 */
fun deletePartialFiles(cacheDir: File) {
    cacheDir.list { _, name -> name.endsWith(DOT_PART_SUFFIX) }
        ?.forEach { File(cacheDir, it).delete() }
}

/**
 * Downloads a file with a temporary name into the cache directory, and when the download is
 * finished moves the file to the actual position, ensuring that the file is created/overwritten
 * only if it has been surely downloaded fully. Even has a progress callback, updated every
 * [CHUNK_SIZE] bytes.
 *
 * @param response the prepared network [Response]
 * @param file the file where to save the downloaded data, once download has fully finished
 * @param cacheDir the directory to place the temporary partial file, while download is in progress
 * @param progressCallback called every [CHUNK_SIZE] bytes with the current and total bytes, or
 * `0, 0` if there is no [CONTENT_LENGTH] header in the response
 */
@Throws(IOException::class)
suspend fun downloadBinaryFileWithPartial(
    response: Response,
    file: File,
    cacheDir: File,
    progressCallback: (currentBytes: Long, totalBytes: Long) -> Unit,
) {
    // use a partial file so that the file won't be considered as already downloaded
    // if the download gets interrupted
    val partialFile = withContext(Dispatchers.IO) {
        File.createTempFile(file.name, DOT_PART_SUFFIX, cacheDir)
    }
    partialFile.outputStream().use {
        downloadBinaryFile(response, it, progressCallback)
    }

    // delete the previous file, if any
    withContext(Dispatchers.IO) {
        file.delete()
    }

    // the file has been fully downloaded, so we can rename it
    if (withContext(Dispatchers.IO) { !partialFile.renameTo(file) }) {
        throw IOException("Cannot rename partial file $partialFile to actual file $file")
    }
}

/**
 * @param response the prepared network [Response]
 * @param outputStream where to put downloaded data
 * @param progressCallback called every [CHUNK_SIZE] bytes with the current and total bytes, or
 * `0, 0` if there is no [CONTENT_LENGTH] header in the response
 */
@Throws(IOException::class)
suspend fun downloadBinaryFile(
    response: Response,
    outputStream: OutputStream,
    progressCallback: (currentBytes: Long, totalBytes: Long) -> Unit,
) {
    val responseBody = response.body ?: throw IOException("Response doesn't contain a file")
    val totalBytes = response.header(CONTENT_LENGTH)?.toLong() ?: 0

    progressCallback(0, totalBytes)
    BufferedInputStream(responseBody.byteStream()).use { input ->
        val dataBuffer =
            ByteArray(CHUNK_SIZE)
        var readBytes: Int
        var currentBytes: Long = 0
        while (input.read(dataBuffer).also { readBytes = it } != -1) {
            yield() // manually yield because the input/output streams are blocking
            currentBytes += readBytes.toLong()
            outputStream.write(dataBuffer, 0, readBytes)
            progressCallback(currentBytes, totalBytes)
        }
    }
}

fun OkHttpClient.getResponse(url: String): Response {
    val request: Request = Request.Builder().url(url).build()
    return this.newCall(request).execute()
}
