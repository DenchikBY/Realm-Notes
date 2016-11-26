package by.denchik.realmnotes.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Note : RealmObject() {

    @PrimaryKey var id = 0L

    var title = ""

    var text = ""

    var date = Date()

    var color: String = COLOR.WHITE.name

}
