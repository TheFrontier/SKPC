package frontier.skpc

data class Aliases(val aliases: List<String>, val permission: String? = null) {
    constructor(vararg aliases: String, permission: String? = null) : this(aliases.toList(), permission)
}

infix fun String.permission(permission: String) = Aliases(listOf(this), permission)

infix fun List<String>.permission(permission: String) = Aliases(this, permission)