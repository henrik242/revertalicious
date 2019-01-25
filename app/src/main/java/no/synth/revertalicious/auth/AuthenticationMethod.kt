package no.synth.revertalicious.auth

enum class AuthenticationMethod {
    password,
    pubkey;

    companion object {
        fun parse(value: String): AuthenticationMethod? =
            AuthenticationMethod.values().find { it.name == value.toLowerCase() }
    }
}
