package no.synth.revertalicious.auth

enum class AuthenticationMethod {
    password,
    pubkey,
    token;

    companion object {
        fun parse(value: String): AuthenticationMethod? =
            AuthenticationMethod.values().find { it.name == value.toLowerCase() }
    }
}
