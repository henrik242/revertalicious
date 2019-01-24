package no.synth.revertalicious

import no.synth.revertalicious.GitTask.Companion.AuthenticationMethod.password
import no.synth.revertalicious.GitTask.Companion.AuthenticationMethod.pubkey
import no.synth.revertalicious.GitTask.Companion.resolveUrl
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class GitTaskTest {
    @Test
    fun can_parse_git_url() {
        val url = resolveUrl("git@github.com:henrik242/testing123.git", pubkey)
        assertEquals(GitUrl("git", "github.com", "testing123.git"), url)
    }

    @Test
    fun can_parse_http_url() {
        val url = resolveUrl("https://foo@github.com/henrik242/testing123.git", password)
        assertEquals(GitUrl("foo", "github.com", "testing123.git"), url)
    }

    @Test
    fun can_parse_http_url_without_user() {
        val url = resolveUrl("https://github.com/henrik242/testing123.git", password)
        assertEquals(GitUrl(null, "github.com", "testing123.git"), url)
    }
}
