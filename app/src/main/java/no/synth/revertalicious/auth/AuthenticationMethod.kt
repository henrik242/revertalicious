package no.synth.revertalicious.auth

import java.util.*

enum class AuthenticationMethod {
    password,
    pubkey;

    companion object {
        val nb_NO = Locale("nb", "NO")

        fun parse(value: String): AuthenticationMethod? = values().find { it.name == value.toLowerCase(nb_NO) }
    }
}
