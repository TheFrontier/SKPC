package frontier.skpc.util

inline operator fun <reified K, reified V> MutableMap<K, V>.set(keys: Iterable<K>, value: V) {
    for (key in keys) {
        this[key] = value
    }
}