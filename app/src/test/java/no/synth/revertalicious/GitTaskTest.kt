package no.synth.revertalicious

import android.content.Context
import no.synth.revertalicious.GitTask.Companion.resolveUrl
import no.synth.revertalicious.auth.AuthenticationMethod.token
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


@RunWith(MockitoJUnitRunner::class)
class GitTaskTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun can_parse_git_url() {
        val url = resolveUrl("git@github.com:henrik242/testing123.git")
        assertEquals(GitUrl("git", "github.com", "testing123.git"), url)
    }

    @Test
    fun can_parse_http_url() {
        val url = resolveUrl("https://foo@github.com/henrik242/testing123.git")
        assertEquals(GitUrl("foo", "github.com", "testing123.git"), url)
    }

    @Test
    fun can_parse_http_url_without_user() {
        val url = resolveUrl("https://github.com/henrik242/testing123.git")
        assertEquals(GitUrl(null, "github.com", "testing123.git"), url)
    }

    @Test
    fun can_parse_ssh_url() {
        val url = resolveUrl("ssh://foo@github.com/henrik242/testing123.git")
        assertEquals(GitUrl("foo", "github.com", "testing123.git"), url)
    }

    @Test
    fun can_ssh_auth() {
        val tmpDir = File(System.getProperty("java.io.tmpdir") + System.currentTimeMillis())
        val password = this.javaClass.getResource("/test_passwd").readText()

        try {
            `when`(mockContext.filesDir).thenReturn(tmpDir)

            val task = GitTask(
                "https://github.com/henrik242/testing123.git",
                password,
                null,
                token,
                mockContext
            )
            task.doInBackground()

        } finally {
            tmpDir.deleteRecursively()
        }
    }

}
