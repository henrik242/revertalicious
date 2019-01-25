package no.synth.revertalicious

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.auth.AuthenticationMethod.password
import no.synth.revertalicious.auth.AuthenticationMethod.pubkey
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.util.FS

class GitTask(
    val repoUrl: String,
    val username: String?,
    val passwd: String?,
    val sshPrivateKey: String?,
    val authMethod: AuthenticationMethod,
    val context: Context
) : AsyncTask<String, Int, Unit>() {

    public override fun doInBackground(vararg params: String) {

        try {
            val git = cloneOrOpen()

            System.out.println("før")

            printLastTen(git)
            revertLast(git)

            System.out.println("etter")

            printLastTen(git)
            push(git)

            if (context is Activity) {
                context.runOnUiThread {
                    val toast = Toast(context)
                    val view = ImageView(context)
                    view.setImageResource(R.drawable.ic_success)
                    view.setPadding(64, 64, 64, 64)
                    toast.view = view
                    toast.setGravity(Gravity.FILL, 0, 0)
                    toast.show()
                }
            }
        } catch (e: Exception) {
            if (context is Activity) {
                context.runOnUiThread {
                    Toast.makeText(context, "Error: $e", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            } else {
                throw e
            }
        }
    }

    private fun printLastTen(git: Git) {
        git.log().setMaxCount(10).call().forEach {
            System.out.println(it.fullMessage)
        }
    }

    private fun revertLast(git: Git): RevCommit = git.revert().include(git.log().call().first()).call()

    private fun cloneOrOpen(): Git {
        val localDir = context.filesDir.resolve("repos/" + repoUrl.replace(Regex("\\W+"), "_"))
        if (localDir.exists()) {
            System.err.println("Opening")
            val git = Git.open(localDir)

            resetToLatestOnRemote(git)

            git.pull().authenticate().call()
            return git
        } else {
            System.err.println("Cloning")
            return Git
                .cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localDir)
                .authenticate()
                .call()
        }
    }

    private fun resetToLatestOnRemote(git: Git) {
        val latestRemoteSha = git.lsRemote().call().first().objectId.name
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(latestRemoteSha).call()
    }

    private fun push(git: Git): Iterable<PushResult> {
        return git.push().authenticate().call()
    }

    private fun <C : TransportCommand<*, *>> C.authenticate(): C {
        when (authMethod) {
            pubkey ->
                sshPrivateKey?.let {
                    if (!it.contains("BEGIN RSA ")) {
                        throw IllegalArgumentException("You need an older style openssh key, created with 'ssh-keygen -t rsa -m PEM'")
                    }
                    this.setTransportConfigCallback(sshTransportCallback())
                } ?: throw IllegalArgumentException("Missing sshPrivateKey")

            password -> {
                if (passwd != null && username != null) {
                    this.setCredentialsProvider(UsernamePasswordCredentialsProvider(username, passwd))
                } else {
                    throw IllegalArgumentException("Missing username or password")
                }
            }
        }
        return this
    }

    private fun sshTransportCallback(): TransportConfigCallback {

        fun sshSessionFactory() = object : JschConfigSessionFactory() {

            override fun configure(host: OpenSshConfig.Host, session: Session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            override fun createDefaultJSch(fs: FS): JSch {
                //val jsch = super.createDefaultJSch(fs)
                val jsch = JSch()
                jsch.setKnownHosts(context.resources.openRawResource(R.raw.known_hosts))
                jsch.addIdentity(
                    repoUrl,
                    sshPrivateKey?.toByteArray(Charsets.UTF_8),
                    null,
                    passwd?.toByteArray(Charsets.UTF_8)
                )
                return jsch
            }
        }

        return TransportConfigCallback {
            val sshTransport: SshTransport = it as SshTransport
            sshTransport.sshSessionFactory = sshSessionFactory()
        }
    }
}


