package no.synth.revertalicious

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.password
import no.synth.revertalicious.auth.AuthenticationMethod.pubkey
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.util.FS
import java.io.File

class GitTask(
    val repoUrl: String,
    val passwd: String?,
    val sshPrivateKey: String?,
    val authMethod: AuthenticationMethod,
    val context: Context
) : AsyncTask<String, Int, Unit>() {

    val gitUrl : GitUrl

    init {
        gitUrl = resolveUrl(repoUrl)
    }

    public override fun doInBackground(vararg params: String) {

        try {
            val localDir = resolveDir(context.filesDir, repoUrl)
            deleteRecursive(localDir)

            val cloneCmd = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localDir)

            deleteRecursive(localDir)

            authenticate(cloneCmd)
            val git = cloneCmd.call()

            System.out.println("før")

            git.log().call().forEach {
                System.out.println(it.fullMessage)
            }
            git.revert().include(git.log().call().first()).call()

            System.out.println("etter")
            git.log().call().forEach {
                System.out.println(it.fullMessage)
            }

            push(git)

        } catch (e: Exception) {
            if (context is Activity) {
                context.runOnUiThread {
                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show()
                }
            } else {
                throw e
            }
        }
    }

    private fun push(git: Git) : Iterable<PushResult> {
        val pushCmd = git.push()
        authenticate(pushCmd)
        return pushCmd.call()
    }

    private fun authenticate(cmd: TransportCommand<*, *>) = when (authMethod) {
        pubkey ->
            sshPrivateKey?.let {
                if (!it.contains("BEGIN RSA ")) {
                    throw IllegalArgumentException("You need an older style openssh key, created with 'ssh-keygen -t rsa -m PEM'")
                }
                cmd.setTransportConfigCallback(sshTransportCallback(context, gitUrl, it, passwd)).call()
            } ?: throw IllegalArgumentException("Missing sshPrivateKey")

        password -> {
            val username = gitUrl.user
            if (passwd != null && username != null) {
                cmd.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, passwd))
            } else {
                throw IllegalArgumentException("Missing username or password")
            }
        }
    }

    companion object {


        fun resolveUrl(repoUrl: String): GitUrl =
            when {
                repoUrl.matches(gitUrlPattern) -> gitUrlPattern.find(repoUrl)?.groupValues
                else -> regularUrlPattern.find(repoUrl)?.groupValues
            }?.let {
                GitUrl(
                    user = if (it[1].isNotEmpty()) it[1].replace("@", "") else null,
                    host = it[2],
                    dir = it[3]
                )
            } ?: throw IllegalArgumentException("Cannot parse url: $repoUrl")

        private fun resolveDir(filesDir: File, repoUrl: String): File =
            filesDir.resolve(repoUrl.substring(repoUrl.lastIndexOf("/") + 1))

        fun sshTransportCallback(context: Context, repoUrl: GitUrl, privKey: String, password: String? = null): TransportConfigCallback {

            fun sshSessionFactory() = object : JschConfigSessionFactory() {

                override fun configure(host: OpenSshConfig.Host, session: Session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                override fun createDefaultJSch(fs: FS): JSch {
                    //val jsch = super.createDefaultJSch(fs)
                    val jsch = JSch()
                    jsch.setKnownHosts(context.resources.openRawResource(R.raw.known_hosts))

//                    jsch.hostKeyRepository = object : HostKeyRepository {
//                        override fun check(host: String?, key: ByteArray?): Int = HostKeyRepository.OK
//                        override fun add(hostkey: HostKey?, ui: UserInfo?) {}
//                        override fun getHostKey(): Array<HostKey> = arrayOf()
//                        override fun getHostKey(host: String?, type: String?): Array<HostKey> = arrayOf()
//                        override fun remove(host: String?, type: String?) {}
//                        override fun remove(host: String?, type: String?, key: ByteArray?) {}
//                        override fun getKnownHostsRepositoryID(): String = "someId"
//                    }
                    jsch.addIdentity(
                        repoUrl.host,
                        privKey.toByteArray(Charsets.UTF_8),
                        null,
                        password?.toByteArray(Charsets.UTF_8)
                    )
                    return jsch
                }
            }

            return TransportConfigCallback {
                val sshTransport: SshTransport = it as SshTransport
                sshTransport.sshSessionFactory = sshSessionFactory()
            }
        }

        fun deleteRecursive(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory)
                for (child in fileOrDirectory.listFiles())
                    deleteRecursive(child)

            fileOrDirectory.delete()
        }

        val gitUrlPattern = Regex("^(\\w+)@([.\\w]+):.*/(.*?)$")
        val regularUrlPattern = Regex("^\\w+://(\\w*@)?(.*?)/.*?([^/]+)$")
    }
}

data class GitUrl(
    val user: String? = null,
    val host: String,
    val dir: String
)



