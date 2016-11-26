package by.denchik.realmnotes.extensions

import io.realm.Realm
import io.realm.RealmModel

inline fun <reified T : RealmModel> Realm.nextId(primary: String = "id"): Long {
    val max = where(T::class.java).max(primary)
    return max?.toLong()?.inc() ?: 1
}
