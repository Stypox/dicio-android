package org.stypox.dicio.unicodeCldrPlugin.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish
import org.stypox.dicio.unicodeCldrPlugin.util.UnicodeCldrPluginException
import java.io.File

@Throws(UnicodeCldrPluginException::class)
fun ensureGitRepoDownloaded(repo: String, commit: String, directory: File) {
    val git = try {
        Git.open(directory)
    } catch (_: Throwable) {
        println("Cannot open folder $directory, deleting and recloning...")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        Git.init().setDirectory(directory).call()
    }

    git.use { git ->
        try {
            git.remoteAdd()
                .setName("origin")
                .setUri(URIish(repo))
                .call()
        } catch (_: GitAPIException) {
            git.remoteSetUrl()
                .setRemoteName("origin")
                .setRemoteUri(URIish(repo))
                .call()
        }

        git.fetch()
            .setRemote("origin")
            // if this doesn't compile, then Gradle is messing with the jgit version used at
            // runtime; in that case, revert commit "Improve unicode cldr repo cloning"
            .setDepth(1)
            .setRefSpecs(RefSpec(commit))
            .call()

        git.checkout()
            .setName(commit)
            .setStartPoint(commit)
            .call()
    }
}