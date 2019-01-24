package no.synth.revertalicious

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig.Host
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.FS

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val notifications = preferences.getBoolean(SettingsActivity.NOTIFICATIONS, false)

        Toast.makeText(this, "Notifications: $notifications", Toast.LENGTH_SHORT).show()

        val settingsAuthMethod = AuthenticationMethod.pubkey
        val settingsRepoUrl: Uri = Uri.parse("git@github.com:henrik242/testing123.git")
        val settingsPassword: String? = null
        val settingsPrivateKey: String? = resources.openRawResource(R.raw.testing_priv_key).bufferedReader().readText()

        revert.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()

            GitTask(
                settingsRepoUrl,
                settingsPassword,
                settingsPrivateKey,
                settingsAuthMethod,
                this
            ).execute()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

private enum class AuthenticationMethod { password, pubkey, token; }

private class GitTask(
    val repoUrl: Uri,
    val password: String?,
    val sshPrivateKey: String?,
    val authMethod: AuthenticationMethod?,
    val context: Context
) : AsyncTask<String, Int, Unit>() {

    override fun doInBackground(vararg params: String) {

        try {
            val localDir = context.filesDir.resolve(
                repoUrl.toString().substring(repoUrl.toString().lastIndexOf("/") + 1)
            )

            localDir.deleteRecursively()

            val gitBuilder = Git.cloneRepository()
                .setURI(repoUrl.toString())
                .setDirectory(localDir)

            when (authMethod) {
                AuthenticationMethod.pubkey ->
                    sshPrivateKey?.let {
                        if (!it.contains("BEGIN RSA ")) {
                            throw IllegalArgumentException("You need an older style openssh key, created with 'ssh-keygen -t rsa -m PEM'")
                        }
                        gitBuilder.setTransportConfigCallback(sshTransportCallback(localDir.name, it, password)).call()
                    } ?: throw IllegalArgumentException("Missing sshPrivateKey")

                AuthenticationMethod.password -> {
                    val username = repoUrl.userInfo
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
            git.push().setDryRun(true).call()
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

        fun sshTransportCallback(name: String, privKey: String, password: String? = null): TransportConfigCallback {

            val sshSessionFactory: SshSessionFactory = object : JschConfigSessionFactory() {

                override fun configure(host: Host, session: Session) = Unit

                override fun createDefaultJSch(fs: FS): JSch {
                    val defaultJSch = super.createDefaultJSch(fs)
                    defaultJSch.addIdentity(
                        name,
                        privKey.toByteArray(Charsets.UTF_8),
                        null,
                        password?.toByteArray(Charsets.UTF_8)
                    )
                    JSch.setConfig("StrictHostKeyChecking", "no");
                    return defaultJSch
                }
            }

            return TransportConfigCallback {
                val sshTransport: SshTransport = it as SshTransport
                sshTransport.sshSessionFactory = sshSessionFactory
            }
        }
    }
}

