package no.synth.revertalicious

import android.content.Context
import no.synth.revertalicious.GitTask.Companion.resolveUrl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


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


}
