package frontier.skpc.util

import frontier.skpc.CommandValueExecutor
import org.spongepowered.api.command.CommandResult

inline fun <reified A, reified B> executor(
    crossinline block: (A, B) -> CommandResult
): CommandValueExecutor<RTuple<A, B>> =
    { (a, b) ->
        block(a, b)
    }

inline fun <reified A, reified B, reified C> executor(
    crossinline block: (A, B, C) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, C>>> =
    { (a, b, c) ->
        block(a, b, c)
    }

inline fun <reified A, reified B, reified C, reified D> executor(
    crossinline block: (A, B, C, D) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, RTuple<C, D>>>> =
    { (a, b, c, d) ->
        block(a, b, c, d)
    }

inline fun <reified A, reified B, reified C, reified D, reified E> executor(
    crossinline block: (A, B, C, D, E) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, RTuple<C, RTuple<D, E>>>>> =
    { (a, b, c, d, e) ->
        block(a, b, c, d, e)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F> executor(
    crossinline block: (A, B, C, D, E, F) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, F>>>>>> =
    { (a, b, c, d, e, f) ->
        block(a, b, c, d, e, f)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G> executor(
    crossinline block: (A, B, C, D, E, F, G) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, RTuple<F, G>>>>>>> =
    { (a, b, c, d, e, f, g) ->
        block(a, b, c, d, e, f, g)
    }

inline fun <reified A, reified B, reified C, reified D, reified E, reified F, reified G, reified H> executor(
    crossinline block: (A, B, C, D, E, F, G, H) -> CommandResult
): CommandValueExecutor<RTuple<A, RTuple<B, RTuple<C, RTuple<D, RTuple<E, RTuple<F, RTuple<G, H>>>>>>>> =
    { (a, b, c, d, e, f, g, h) ->
        block(a, b, c, d, e, f, g, h)
    }