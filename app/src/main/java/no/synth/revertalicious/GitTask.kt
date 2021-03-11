package no.synth.revertalicious

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import no.synth.revertalicious.MainActivity.Companion.enableRevertButton
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
import java.lang.ref.WeakReference
import kotlin.text.Charsets.UTF_8

open class GitTask(
    private val repoUrl: String,
    private val username: String?,
    private val passwd: String?,
    private val sshPrivateKey: String?,
    private val authMethod: AuthenticationMethod,
    private val contextRef: WeakReference<Context>
) : AsyncTask<String, Int, Unit>() {

    public override fun doInBackground(vararg params: String) {

        val context = contextRef.get()

        try {
            val git = cloneOrOpen()

            log("before revert:")

            printLastTen(git)
            revertLast(git)

            log("after revert:")

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

                    enableRevertButton(context)
                }
            }
        } catch (e: Exception) {
            if (context is Activity) {
                context.runOnUiThread {
                    Toast.makeText(context, "Error: $e", Toast.LENGTH_LONG).show()
                }
                log("Failing!", e)
            } else {
                throw e
            }
        }
    }

    private fun printLastTen(git: Git) {
        git.log().setMaxCount(10).call().forEach {
            log(it.fullMessage)
        }
    }

    private fun revertLast(git: Git): RevCommit = git.revert().include(git.log().call().first()).call()

    private fun cloneOrOpen(): Git {
        val localDir = contextRef.get()?.filesDir?.resolve("repos/" + repoUrl.replace(Regex("\\W+"), "_"))
        if (localDir?.exists() == true) {
            log("Opening $repoUrl")
            val git = Git.open(localDir)

            git.pull().authenticate().call()

            resetToLatestOnRemote(git)
            return git
        } else {
            log("Cloning $repoUrl")
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
        log("Resetting to $latestRemoteSha")
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
                session.setConfig("StrictHostKeyChecking", "no")
            }

            override fun createDefaultJSch(fs: FS): JSch {
                //val jsch = super.createDefaultJSch(fs)
                val jsch = JSch()
                jsch.setKnownHosts(contextRef.get()?.resources?.openRawResource(R.raw.known_hosts))
                jsch.addIdentity(
                    repoUrl,
                    sshPrivateKey?.toByteArray(UTF_8),
                    null,
                    passwd?.toByteArray(UTF_8)
                )
                return jsch
            }
        }

        return TransportConfigCallback {
            val sshTransport: SshTransport = it as SshTransport
            sshTransport.sshSessionFactory = sshSessionFactory()
        }
    }

    open fun log(message: String, throwable: Throwable? = null) {
        Log.v(GitTask::class.java.simpleName, message, throwable)
    }
}


