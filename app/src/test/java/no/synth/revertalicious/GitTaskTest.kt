package no.synth.revertalicious

import android.content.Context
import no.synth.revertalicious.auth.AuthenticationMethod
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class GitTaskTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun can_ssh_auth() {
        val tmpDir = File(System.getProperty("java.io.tmpdir") + System.currentTimeMillis())
        val passwd = this.javaClass.getResource("/test_passwd").readText()
        val username = "henrik242"

        try {
            `when`(mockContext.filesDir).thenReturn(tmpDir)

            val task = GitTask(
                "https://github.com/henrik242/testing123.git",
                username,
                passwd,
                null,
                AuthenticationMethod.password,
                mockContext
            )
            task.doInBackground()

        } finally {
            tmpDir.deleteRecursively()
        }
    }
}
