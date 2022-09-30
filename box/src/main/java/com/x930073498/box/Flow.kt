package com.x930073498.box

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

@PublishedApi
internal object NoValue


inline fun <T,  reified V> T.subscribe(
    property: KProperty1<T, V>,
    option: QueryOption = QueryOption.Standard
): Flow<V> where T : BoxProvider {
    property.isAccessible = true
    val delegate = property.getDelegate(this)
    if (delegate !is Subscribable) {
        throw IllegalArgumentException("property must is Subscribable")
    }
    return propertyBox()
        .queryKey(property, option)
        .onStart {
            property.get(this@subscribe)
        }
        .flowOn(Dispatchers.IO)
        .filterIsInstance()
}


inline fun <T, reified P1, reified P2, R> T.subscribe(
    property1: KProperty1<T, P1>,
    property2: KProperty1<T, P2>,
    option: QueryOption = QueryOption.Standard,
    noinline transform: suspend (P1, P2) -> R
) where T : BoxProvider = channelFlow {
    val flow1 = subscribe(property1, option)
    val flow2 = subscribe(property2, option)

    var result1: Any? = NoValue
    var result2: Any? = NoValue
    launch {
        flow1.collect {
            result1 = it
            if (result2 != NoValue) {
                send(transform(it, result2 as P2))
            }
        }
    }
    launch {
        flow2.collect {
            result2 = it
            if (result1 != NoValue) {
                send(transform(result1 as P1, it))
            }
        }
    }

}

inline fun <T, reified P1, reified P2> T.subscribe(
    property1: KProperty1<T, P1>,
    property2: KProperty1<T, P2>,
    option: QueryOption = QueryOption.Standard
) where T : BoxProvider = subscribe(property1, property2, option) { first, second ->
    first to second
}
