package no.synth.revertalicious

import android.content.Context
import no.synth.revertalicious.auth.AuthenticationMethod
import no.synth.revertalicious.settings.Settings
import org.eclipse.jgit.util.SystemReader
import org.junit.Before
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

    @Mock
    private lateinit var settings: Settings

    @Before
    fun setup() {
        // Avoid consuming any local git config that may affect the tests
        SystemReader.getInstance().userConfig.clear();
    }

    @Test
    fun can_ssh_auth() {
        val tmpDir = File((System.getProperty("java.io.tmpdir") ?: "/tmp") + "/test-" + System.currentTimeMillis())

        val passwd = envOrElse("GITTASKTEST_PASS", (javaClass.getResource("/gittasktest_pass")?.readText() ?: ""))
        val username = envOrElse("GITTASKTEST_USER", "henrik242")
        val repoUrl = envOrElse("GITTASKTEST_REPO", "https://github.com/henrik242/testing123.git")

        try {
            mwhen(mockContext.filesDir).thenReturn(tmpDir)
            mwhen(settings.value(Settings.USERNAME)).thenReturn(username)
            mwhen(settings.value(Settings.PASSWORD)).thenReturn(passwd)
            mwhen(settings.value(Settings.REPOSITORY)).thenReturn(repoUrl)
            mwhen(settings.authenticationMethod()).thenReturn(AuthenticationMethod.password)

            val task = object : GitTask(settings, WeakReference(mockContext)) {
                override fun log(message: String, throwable: Throwable?) = System.err.println("$message $throwable")
            }
            task.cloneOrOpen()
            task.executeRevert()

        } finally {
            tmpDir.deleteRecursively()
        }
    }

    fun envOrElse(env: String, fallback: String) = if (System.getenv(env).isNullOrBlank()) fallback else System.getenv(env)
}
