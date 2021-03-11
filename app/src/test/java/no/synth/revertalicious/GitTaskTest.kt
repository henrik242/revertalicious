package no.synth.revertalicious

import android.content.Context
import no.synth.revertalicious.auth.AuthenticationMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.lang.ref.WeakReference
import org.mockito.Mockito.`when` as mwhen

@RunWith(MockitoJUnitRunner::class)
class GitTaskTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun can_ssh_auth() {
        val tmpDir = File((System.getProperty("java.io.tmpdir") ?: "/tmp/") + System.currentTimeMillis())
        val passwd = if (System.getenv("GITTASKTEST_PASS").isNullOrBlank()) {
            this.javaClass.getResource("/gittasktest_pass")?.readText()
        } else {
            System.getenv("GITTASKTEST_PASS")
        }
        val username = if (System.getenv("GITTASKTEST_USER").isNullOrBlank()) {
            "henrik242"
        } else {
            System.getenv("GITTASKTEST_USER")
        }
        val repoUrl = if (System.getenv("GITTASKTEST_REPO").isNullOrBlank()) {
            "https://github.com/henrik242/testing123.git"
        } else {
            System.getenv("GITTASKTEST_REPO")
        }

        try {
            mwhen(mockContext.filesDir).thenReturn(tmpDir)

            val task = object : GitTask(
                repoUrl,
                username,
                passwd,
                null,
                AuthenticationMethod.password,
                WeakReference(mockContext)
            ) {
                override fun log(message: String, throwable: Throwable?) = System.err.println("$message $throwable")
            }
            task.doInBackground()

        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
