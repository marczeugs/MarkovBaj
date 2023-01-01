
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.browser.window
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.w3c.dom.get

class LocalStorageBackedSnapshotStateMap<KeyType, ValueType>(
    private val localStorageKey: String,
    private val backingSnapshotStateMap: SnapshotStateMap<KeyType, ValueType>,
    private val mapSerializer: KSerializer<Map<KeyType, ValueType>>
)
    : MutableMap<KeyType, ValueType> by backingSnapshotStateMap
{
    operator fun set(key: KeyType, value: ValueType) {
        window.localStorage[localStorageKey]?.let { serializedMap ->
            backingSnapshotStateMap.putAll(Json.decodeFromString(mapSerializer, serializedMap))
        }

        backingSnapshotStateMap[key] = value

        window.localStorage.setItem(localStorageKey, Json.encodeToString(mapSerializer, backingSnapshotStateMap))
    }
}

inline fun <reified KeyType, ValueType> LocalStorageBackedSnapshotStateMap(localStorageKey: String): LocalStorageBackedSnapshotStateMap<KeyType, ValueType> {
    val backingMap: SnapshotStateMap<KeyType, ValueType> =
        window.localStorage[localStorageKey]?.let { serializedMap ->
            mutableStateMapOf<KeyType, ValueType>().apply {
                putAll(Json.decodeFromString<Map<KeyType, ValueType>>(serializedMap))
            }
        } ?: run {
            mutableStateMapOf()
        }

    return LocalStorageBackedSnapshotStateMap(localStorageKey, backingMap, serializer())
}