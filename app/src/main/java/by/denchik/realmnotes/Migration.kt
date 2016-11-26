package by.denchik.realmnotes

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import io.realm.RealmSchema

class Migration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val schema = realm.schema
        val up = mapOf(
                2 to { schema: RealmSchema -> schema.get("Note").addField("color", Int::class.java) },
                3 to { schema: RealmSchema -> schema.get("Note").removeField("color").addField("color", String::class.java) }
        )
        val down = mapOf(
                1 to { schema: RealmSchema -> schema.get("Note").removeField("color") }
        )
        if (newVersion > oldVersion) {
            for (i in (oldVersion + 1)..newVersion) {
                up[i.toInt()]?.invoke(schema)
            }
        } else if (newVersion < oldVersion) {
            for (i in (oldVersion - 1)..newVersion) {
                down[i.toInt()]?.invoke(schema)
            }
        }
    }

}
