@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

interface BoxRegistry {
    fun <K, V> BoxProvider.getBox(factory: BoxFactory<K, V>): Box<K, V>

    fun clear()
}

private class DefaultBoxRegistry : BoxRegistry {
    private val map = mutableMapOf<BoxFactory<*, *>, Box<*, *>>()
    override fun <K, V> BoxProvider.getBox(factory: BoxFactory<K, V>): Box<K, V> {
        return map.getOrPut(factory) {
            with(factory) {
                create()
            }
        } as Box<K, V>
    }


    override fun clear() {
        map.values.forEach {
            it.clear()
        }
        map.clear()
    }
}

interface BoxProvider {
    companion object : BoxProvider by BoxProvider()

    val registry: BoxRegistry

}

fun BoxRegistry(): BoxRegistry = DefaultBoxRegistry()

interface BoxFactory<K, V> {
    fun BoxProvider.create(): Box<K, V> = Box()
}

fun BoxProvider(): BoxProvider = DefaultBoxProvider()
private class DefaultBoxProvider : BoxProvider {
    override val registry: BoxRegistry by lazy {
        BoxRegistry()
    }
}


sealed interface QueryOption {
    object Standard : QueryOption
    object OnlyCreate : QueryOption
    object OnlySet : QueryOption
}

fun <K, V> Box(): Box<K, V> = DefaultBox()
operator fun <K, V, T> Box<K, V>.get(key: K, clazz: Class<T>): T? {
    val value = get(key)
    return if (clazz.isInstance(value)) clazz.cast(value) else null
}

inline fun <K, reified T> Box<K, *>.getInstance(key: K): T? = get(key, T::class.java)

operator fun <K, V> Box<K, V>.set(key: K, value: V) = put(key, value)

interface Box<K, V> {
    val size: Int
    val keys: Set<K>
    fun containsKey(key: K): Boolean
    operator fun get(key: K): V?
    fun clear()
    fun put(key: K, value: V)
    fun remove(key: K): V?
    fun <R> query(
        option: QueryOption = QueryOption.Standard,
        keysChangeNotifyFilter: suspend (Collection<K>) -> Boolean = { true },
        builder: suspend Box<K, V>.() -> R
    ): Flow<R>

}


private class DefaultBox<K, V> : Box<K, V> {

    sealed interface Action<K> {
        data class Key<K>(val key: K) : Action<K>
        data class Keys<K>(val keys: Collection<K>) : Action<K>
    }

    private val changeFlow = MutableSharedFlow<Action<K>>(
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val map = ConcurrentHashMap<K, V>()
    override val size: Int
        get() = map.size

    override fun containsKey(key: K): Boolean {
        if (key == null) return false
        return map.containsKey(key)
    }


    override fun get(key: K): V? {
        if (key == null) return null
        return map[key]
    }


    override val keys: MutableSet<K>
        get() = map.keys

    override fun clear() {
        val keys = map.keys().toList()
        map.clear()
        changeFlow.tryEmit(Action.Keys(keys))
    }

    override fun put(key: K, value: V) {
        if (key == null) return
        if (value == null) return
        map[key] = value
        changeFlow.tryEmit(Action.Key(key))
    }


    override fun remove(key: K): V? {
        if (key == null) return null
        val result = map.remove(key)
        if (result != null)
            changeFlow.tryEmit(Action.Key(key))
        return result
    }

    override fun <R> query(
        option: QueryOption,
        keysChangeNotifyFilter: suspend (Collection<K>) -> Boolean,
        builder: suspend Box<K, V>.() -> R
    ): Flow<R> {
        val realFlow = changeFlow.map {
            when (it) {
                is Action.Keys -> {
                    it.keys
                }
                is Action.Key -> {
                    listOf(it.key)
                }
            }
        }.filter(keysChangeNotifyFilter)
        return when (option) {
            QueryOption.OnlyCreate -> {
                flow {
                    emit(builder())
                }
            }
            QueryOption.OnlySet -> {
                realFlow.map {
                    builder()
                }
            }
            QueryOption.Standard -> {
                merge(flow { emit(builder()) },realFlow.map { builder() })
            }
        }
//        return callbackFlow {
//
//            when (option) {
//                QueryOption.OnlySet -> {
//                    launch {
//                        realFlow.collect {
//                            send(builder())
//                        }
//                    }
//                }
//                QueryOption.OnlyCreate -> {
//                    send(builder())
//                }
//                QueryOption.Standard -> {
//                    send(builder())
//                    launch {
//                        realFlow.collect {
//                            send(builder())
//                        }
//                    }
//                }
//            }
//            awaitClose()
//        }.flowOn(Dispatchers.IO)
    }

}

fun <K, V> Box<K, V>.queryKey(key: K, option: QueryOption = QueryOption.Standard): Flow<V?> =
    query(option = option, keysChangeNotifyFilter = { it.contains(key) }) { get(key) }
        .flowOn(Dispatchers.IO)

fun <K, V> BoxProvider.getBox(factory: BoxFactory<K, V>): Box<K, V> =
    with(registry) { getBox(factory) }

fun <K, V> Box<K, Any?>.getOrCreate(key: K, builder: Box<K, Any?>.() -> V): V {
    var data = get(key) as? V
    if (data != null) return data
    data = builder()
    put(key, data)
    return data
}