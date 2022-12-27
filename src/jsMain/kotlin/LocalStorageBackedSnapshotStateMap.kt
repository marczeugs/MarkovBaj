import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.browser.window
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.w3c.dom.get

class LocalStorageBackedSnapshotStateMap<KeyType, ValueType>(
    private val localStorageKey: String,
    private val backingSnapshotStateMap: SnapshotStateMap<KeyType, ValueType>,
    private val mapSerializer: SerializationStrategy<Map<KeyType, ValueType>>
)
    : MutableMap<KeyType, ValueType> by backingSnapshotStateMap
{
    operator fun set(key: KeyType, value: ValueType) {
        backingSnapshotStateMap[key] = value
        window.localStorage.setItem(localStorageKey, Json.encodeToString(mapSerializer, backingSnapshotStateMap))
    }
}

inline fun <reified KeyType, ValueType> LocalStorageBackedSnapshotStateMap(localStorageKey: String): LocalStorageBackedSnapshotStateMap<KeyType, ValueType> {
    val backingMap: SnapshotStateMap<KeyType, ValueType> =
        window.localStorage[localStorageKey]?.let { serializedMap ->
            mutableStateMapOf(*Json.decodeFromString<Map<KeyType, ValueType>>(serializedMap).entries.map { it.key to it.value }.toTypedArray())
        } ?: run {
            mutableStateMapOf()
        }

    return LocalStorageBackedSnapshotStateMap(localStorageKey, backingMap, serializer())
}