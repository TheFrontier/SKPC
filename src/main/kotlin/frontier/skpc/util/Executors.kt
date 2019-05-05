package frontier.skpc.util

inline fun <reified A, reified B> executor(
    crossinline block: (A, B) -> Unit
): ((RTuple<A, B>) -> Unit) =
    { (a, b) ->
        block(a, b)
    }

inline fun <reified A, reified B, reified C> executor(
    crossinline block: (A, B, C) -> Unit
): ((RTuple<A, RTuple<B, C>>) -> Unit) =
    { (a, b, c) ->
        block(a, b, c)
    }

inline fun <reified A, reified B, reified C, reified D> executor(
    crossinline block: (A, B, C, D) -> Unit
): ((RTuple<A, RTuple<B, RTuple<C, D>>>) -> Unit) =
    { (a, b, c, d) ->
        block(a, b, c, d)
    }

inline fun <reified A, reified B, reified C, reified D, reified E> executor(
    crossinline block: (A, B, C, D, E) -> Unit
): ((RTuple<A, RTuple<B, RTuple<C, RTuple<D, E>>>>) -> Unit) =
    { (a, b, c, d, e) ->
        block(a, b, c, d, e)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F> executor(
    crossinline block: (A, B, C, D, E, F) -> Unit
): ((RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, F>>>>>) -> Unit) =
    { (a, b, c, d, e, f) ->
        block(a, b, c, d, e, f)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G> executor(
    crossinline block: (A, B, C, D, E, F, G) -> Unit
): ((RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, RTuple<F, G>>>>>>) -> Unit) =
    { (a, b, c, d, e, f, g) ->
        block(a, b, c, d, e, f, g)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H> executor(
    crossinline block: (A, B, C, D, E, F, G, H) -> Unit
): ((RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, RTuple<F, RTuple<G, H>>>>>>>) -> Unit) =
    { (a, b, c, d, e, f, g, h) ->
        block(a, b, c, d, e, f, g, h)
    }