package com.x930073498.box

import android.content.SharedPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch


private class SharedPreferencesBox(private val sharedPreferences: SharedPreferences) :
    Box<String, String> {
    override val size: Int
        get() = sharedPreferences.all.size
    override val keys: Set<String>
        get() = sharedPreferences.all.keys

    override fun containsKey(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    override fun put(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun remove(key: String): String? {
        val result = sharedPreferences.getString(key, null)
        sharedPreferences.edit().remove(key).apply()
        return result
    }

    override fun <R> query(
        option: QueryOption,
        keysChangeNotifyFilter: suspend (Collection<String>) -> Boolean,
        builder: suspend Box<String, String>.() -> R
    ): Flow<R> {
        return callbackFlow {
            val channel = Channel<String?>()
            launch {
                for (data in channel) {
                    if (data == null) {
                        send(builder())
                    } else {
                        if (keysChangeNotifyFilter(listOf(data))) send(builder())
                    }
                }
            }
            val listener: SharedPreferences.OnSharedPreferenceChangeListener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    channel.trySend(key)
                }
            when (option) {
                QueryOption.OnlySet -> {
                    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                }
                QueryOption.OnlyCreate -> {
                    send(builder())
                }
                QueryOption.Standard -> {
                    send(builder())
                    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                }
            }
            awaitClose {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }

    }

}