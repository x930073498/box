@file:Suppress("UNCHECKED_CAST")

package com.x930073498.box

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

interface BoxRegistry {
    fun <K, V> getBox(factory: BoxFactory<K, V>): Box<K, V>
    fun <K, V, T> getChangeFlow(
        factory: BoxFactory<K, V>,
        option: QueryOption = QueryOption.Standard,
        keysChangeNotifyFilter: suspend (Collection<K>) -> Boolean = {
            true
        },
        query: (Box<K, V>) -> T
    ): Flow<T>

    fun clear()
}

private class DefaultBoxRegistry : BoxRegistry {
    private val map = mutableMapOf<BoxFactory<*, *>, Box<*, *>>()
    override fun <K, V> getBox(factory: BoxFactory<K, V>): Box<K, V> {
        return map.getOrPut(factory) {
            factory.create()
        } as Box<K, V>
    }

    override fun <K, V, T> getChangeFlow(
        factory: BoxFactory<K, V>,
        option: QueryOption,
        keysChangeNotifyFilter: suspend (Collection<K>) -> Boolean,
        query: (Box<K, V>) -> T
    ): Flow<T> {
        return getBox(factory).query(option, keysChangeNotifyFilter, query)
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
    fun destroy() {
        registry.clear()
    }
}

fun BoxRegistry(): BoxRegistry = DefaultBoxRegistry()

interface BoxFactory<K, V> {
    fun create(): Box<K, V> = Box()
}

fun BoxProvider(): BoxProvider = DefaultBoxProvider()
private class DefaultBoxProvider : BoxProvider {
    override val registry: BoxRegistry by lazy {
        BoxRegistry()
    }
}


sealed interface QueryOption {
    object Standard : QueryOption
    object Single : QueryOption
    object OnlyChange : QueryOption
}

fun <K, V> Box() = Box.create<K, V>()
operator fun <K, V, T> Box<K, V>.get(key: K, clazz: Class<T>): T? {
    val value = get(key)
    return if (clazz.isInstance(value)) clazz.cast(value) else null
}

inline fun <K, reified T> Box<K, *>.getInstance(key: K): T? = get(key, T::class.java)


interface Box<K, V> : MutableMap<K, V> {
    companion object {
        fun <K, V> create(): Box<K, V> = DefaultBox()
    }

    fun <R> query(
        option: QueryOption = QueryOption.Standard,
        keysChangeNotifyFilter: suspend (Collection<K>) -> Boolean = { true },
        builder: suspend Box<K, V>.() -> R
    ): Flow<R>
}


private class Cache<K> {
    //keys
    //changeKeys1,changeKeys2

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

    override fun containsValue(value: V): Boolean {
        if (value == null) return false
        return map.containsValue(value)
    }

    override fun get(key: K): V? {
        if (key == null) return null
        return map[key]
    }

    override fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = map.entries
    override val keys: MutableSet<K>
        get() = map.keys
    override val values: MutableCollection<V>
        get() = map.values

    override fun clear() {
        val keys = map.keys().toList()
        map.clear()
        changeFlow.tryEmit(Action.Keys(keys))
    }

    override fun put(key: K, value: V): V? {
        if (key == null) return null
        if (value == null) return remove(key)
        val result = map.put(key, value)
        changeFlow.tryEmit(Action.Key(key))
        return result
    }

    override fun putAll(from: Map<out K, V>) {
        map.putAll(from)
        changeFlow.tryEmit(Action.Keys(from.keys))
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
        return callbackFlow {
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
            when (option) {
                QueryOption.OnlyChange -> {
                    launch {
                        realFlow.collect {
                            send(builder())
                        }
                    }
                }
                QueryOption.Single -> {
                    send(builder())
                }
                QueryOption.Standard -> {
                    send(builder())
                    launch {
                        realFlow.collect {
                            send(builder())
                        }
                    }
                }
            }
            awaitClose()
        }.flowOn(Dispatchers.IO)
    }

}

fun <K, V> Box<K, V>.queryKey(key: K, option: QueryOption = QueryOption.Standard): Flow<V?> =
    query(option = option, keysChangeNotifyFilter = { it.contains(key) }) { get(key) }
        .flowOn(Dispatchers.IO)

fun <K, V> BoxProvider.getBox(factory: BoxFactory<K, V>): Box<K, V> = registry.getBox(factory)

fun <K, V> Box<K, Any?>.getOrCreate(key: K, builder: Box<K, Any?>.() -> V): V {
    return getOrPut(key) {
        builder(this)
    } as V
}