package org.stypox.dicio.unicodeCldrPlugin.data

import org.eclipse.jgit.api.Git
import org.stypox.dicio.unicodeCldrPlugin.util.UnicodeCldrPluginException
import java.io.File

@Throws(UnicodeCldrPluginException::class)
fun ensureGitRepoDownloaded(repo: String, commit: String, directory: File) {
    if (directory.exists()) {
        try {
            Git.open(directory).use { git ->
                val headCommit = git.repository.resolve("HEAD")?.name
                if (headCommit != commit) {
                    println("Commit mismatch for $repo ($headCommit != $commit), deleting and recloning...")
                    directory.deleteRecursively()
                } else {
                    return // no need to clone again
                }
            }
        } catch (_: Exception) {
            println("Cannot open folder $directory, deleting and recloning...")
            directory.deleteRecursively()
        }
    }

    Git.cloneRepository()
        .setURI(repo)
        .setDirectory(directory)
        .setCloneAllBranches(false)
        // TODO Shallow clone is not supported by this version of jgit. We can't change version
        //  because somehow gradle forces an old version upon us despite all attempts. If you
        //  uncomment the line below you will see Android Studio gives you no error, because it
        //  resolves the correct version, but Gradle doesn't...
        //.setDepth(1)
        .call()
        .close()
}