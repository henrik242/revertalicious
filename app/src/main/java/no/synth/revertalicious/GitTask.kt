package no.synth.revertalicious

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.widget.Toast
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import no.synth.revertalicious.auth.AuthenticationMethod
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.FS
import java.io.File

class GitTask(
    val repoUrl: String,
    val password: String?,
    val sshPrivateKey: String?,
    val authMethod: AuthenticationMethod,
    val context: Context
) : AsyncTask<String, Int, Unit>() {

    override fun doInBackground(vararg params: String) {

        try {
            val localDir = resolveDir(context.filesDir, repoUrl)
            deleteRecursive(localDir)

            val gitBuilder = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localDir)

            val gitUrl = resolveUrl(repoUrl, authMethod)

            when (authMethod) {
                AuthenticationMethod.pubkey ->
                    sshPrivateKey?.let {
                        if (!it.contains("BEGIN RSA ")) {
                            throw IllegalArgumentException("You need an older style openssh key, created with 'ssh-keygen -t rsa -m PEM'")
                        }
                        gitBuilder.setTransportConfigCallback(sshTransportCallback(context, gitUrl, it, password)).call()
                    } ?: throw IllegalArgumentException("Missing sshPrivateKey")

                AuthenticationMethod.password -> {
                    val username = gitUrl.user
                    if (password != null && username != null) {
                        gitBuilder.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, password))
                    } else {
                        throw IllegalArgumentException("Missing username or password")
                    }
                }

                AuthenticationMethod.token ->
                    password?.let {
                        gitBuilder.setCredentialsProvider(UsernamePasswordCredentialsProvider("token", it))
                    } ?: throw IllegalArgumentException("Missing password")
            }

            deleteRecursive(localDir)
            val git = gitBuilder.call()

            System.out.println("før")

            git.log().call().forEach {
                System.out.println(it.fullMessage)
            }
            git.revert().include(git.log().call().first()).call()

            System.out.println("etter")
            git.log().call().forEach {
                System.out.println(it.fullMessage)
            }
            git.push().setDryRun(false).call()
        } catch (e: Exception) {

            (context as Activity).runOnUiThread(object : Runnable {
                override fun run() {
                    Toast.makeText(context, "Error: " + e.toString(), Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            })
        }
    }

    companion object {

        fun resolveUrl(repoUrl: String, authMethod: AuthenticationMethod): GitUrl {

            val matches = when (authMethod) {
                AuthenticationMethod.pubkey -> Regex("^(.*)@(.*):.*/([^/]+)$").find(repoUrl)?.groupValues
                else -> Regex("^.*:/*([0-9a-zA-Z]*@)?(.*?)/.*?([^/]+)$").find(repoUrl)?.groupValues
            }
            return matches?.let {
                GitUrl(
                    user = if (it[1].isNotEmpty()) it[1].replace("@", "") else null,
                    host = it[2],
                    dir = it[3]
                )
            } ?: throw IllegalArgumentException("Cannot parse url: $repoUrl")
        }

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
    }
}

data class GitUrl(
    val user: String? = null,
    val host: String,
    val dir: String
)



