package frontier.skpc.value.standard

import frontier.ske.*
import frontier.ske.java.util.unwrap
import frontier.ske.service.require
import frontier.ske.service.user.userStorageService
import frontier.skpc.value.IValueCompleter
import frontier.skpc.value.ValueCompleter
import frontier.skpc.value.ValueParser
import frontier.skpc.value.completer
import org.spongepowered.api.CatalogType
import org.spongepowered.api.command.args.ArgumentParseException
import org.spongepowered.api.service.permission.PermissionService
import org.spongepowered.api.service.permission.SubjectCollection

object ValueCompleters {

    val empty = completer { _, _ ->
        emptyList()
    }

    val boolean: ValueCompleter<Any?> =
        choices { listOf("true", "t", "yes", "y", "1", "false", "f", "no", "n", "0") }

    val player: ValueCompleter<Any?> = { _, _, _ ->
        server.onlinePlayers.map { it.name }
    }

    val user: ValueCompleter<Any?> = { _, _, _ ->
        userStorageService.all.mapNotNull { it.name.unwrap() }
    }

    val world: ValueCompleter<Any?> = { _, _, _ ->
        server.worlds.map { it.name }
    }

    val worldProperties: ValueCompleter<Any?> = { _, _, _ ->
        server.allWorldProperties.map { it.worldName }
    }

    val plugin: ValueCompleter<Any?> = { _, _, _ ->
        pluginManager.plugins.map { it.id }
    }

    inline fun <reified T : CatalogType> catalogType(): ValueCompleter<Any?> = { _, _, _ ->
        gameRegistry.getAllOf<T>().map { it.id }
    }

    inline fun <reified T : Enum<T>> enum(): ValueCompleter<Any?> {
        val values = T::class.java.enumConstants.map { it.name.toLowerCase() }

        return { _, _, _ ->
            values
        }
    }

    val subject = completer { _, _, collection: SubjectCollection ->
        collection.loadedSubjects.map { it.identifier }
    }

    fun subject(collectionIdentifier: String): ValueCompleter<Any?> {
        val collection = serviceManager.require<PermissionService>().getCollection(collectionIdentifier).unwrap()

        return { _, _, _ ->
            collection?.loadedSubjects?.map { it.identifier }.orEmpty()
        }
    }

    inline fun <P> choices(crossinline keys: () -> Collection<String>): ValueCompleter<P> =
        { _, _, _ ->
            keys().toList()
        }

    fun <P> choices(map: Map<String, *>): ValueCompleter<P> =
        choices(map::keys)

    inline fun <P> remaining(crossinline completer: ValueCompleter<P>,
                             crossinline parser: ValueParser<P, *>): ValueCompleter<P> =
        IValueCompleter { src, args, previous ->
            while (args.hasNext()) {
                val snapshot = args.snapshot
                try {
                    parser(src, args, previous)
                } catch (e: ArgumentParseException) {
                    args.applySnapshot(snapshot)
                    return@IValueCompleter completer(src, args, previous)
                }
            }
            emptyList()
        }

    inline fun <P> repeated(crossinline completer: ValueCompleter<P>,
                            crossinline parser: ValueParser<P, *>, count: Int): ValueCompleter<P> =
        IValueCompleter { src, args, previous ->
            for (i in 1..count) {
                val snapshot = args.snapshot
                try {
                    parser(src, args, previous)
                } catch (e: ArgumentParseException) {
                    args.applySnapshot(snapshot)
                    return@IValueCompleter completer(src, args, previous)
                }
            }
            emptyList()
        }
}