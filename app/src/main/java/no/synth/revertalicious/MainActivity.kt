package no.synth.revertalicious

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.eclipse.jgit.api.Git
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        revert.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()

            GitTask().execute("https://github.com/henrik242/testing123.git", applicationContext.filesDir.resolve("repo").absolutePath)
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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

private class GitTask : AsyncTask<String, Int, Unit>() {
    override fun doInBackground(vararg params: String): Unit {
        val repo = File(params[1])
        repo.deleteRecursively()

        val git = Git.cloneRepository().setURI(params[0]).setDirectory(repo).call()
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
    }
}

