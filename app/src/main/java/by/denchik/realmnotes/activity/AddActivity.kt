package by.denchik.realmnotes.activity

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.*
import by.denchik.realmnotes.R
import by.denchik.realmnotes.extensions.nextId
import by.denchik.realmnotes.extensions.t
import by.denchik.realmnotes.models.COLOR
import by.denchik.realmnotes.models.Note
import io.realm.Realm
import org.jetbrains.anko.*

class AddActivity : Activity() {

    val ui = UI()
    val realm = Realm.getDefaultInstance()!!
    var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra("id", 0)
        note = realm.where(Note::class.java).equalTo("id", id).findFirst()
        ui.setContentView(this)
        setActionBar(ui.toolbar)
        actionBar.apply {
            setDisplayHomeAsUpEnabled(true)
            title = resources.getString(t(note == null, R.string.add_note, R.string.edit_note))
        }
        if (note != null) {
            ui.titleEditText.setText(note?.title)
            ui.textEditText.setText(note?.text)
            (ui.colorRadioGroup.getChildAt(COLOR.valueOf(note?.color as String).ordinal) as RadioButton).isChecked = true
        } else {
            (ui.colorRadioGroup.getChildAt(0) as RadioButton).isChecked = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> save()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        save()
        super.onBackPressed()
    }

    private fun save() {
        if (ui.titleEditText.text.isNotEmpty() || ui.textEditText.text.isNotEmpty()) {
            realm.executeTransaction {
                if (note != null) {
                    realm.copyToRealmOrUpdate(note?.apply {
                        title = ui.titleEditText.text.toString()
                        text = ui.textEditText.text.toString()
                        color = COLOR.values()[ui.colorRadioGroup.checkedRadioButtonId].name
                    })
                } else {
                    realm.insert(Note().apply {
                        id = realm.nextId<Note>()
                        title = ui.titleEditText.text.toString()
                        text = ui.textEditText.text.toString()
                        color = COLOR.values()[ui.colorRadioGroup.checkedRadioButtonId].name
                    })
                }
            }
        }
        finish()
    }

    class UI : AnkoComponent<AddActivity> {
        lateinit var titleEditText: EditText
        lateinit var textEditText: EditText
        lateinit var colorRadioGroup: RadioGroup
        lateinit var toolbar: Toolbar
        override fun createView(ui: AnkoContext<AddActivity>) = ui.apply {
            verticalLayout {
                toolbar = toolbar(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                    elevation = dip(4).toFloat()
                    backgroundResource = R.color.colorPrimary
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                }
                titleEditText = editText {
                    hintResource = R.string.note_title
                }
                textEditText = editText {
                    hintResource = R.string.note_text
                    gravity = Gravity.TOP and Gravity.START
                    lines = 5
                }//.lparams(width = matchParent, height = matchParent)
                colorRadioGroup = radioGroup {
                    orientation = LinearLayout.HORIZONTAL
                    for (color in COLOR.values()) {
                        radioButton {
                            id = color.ordinal
                            text = color.name.toLowerCase().capitalize()
                            if (color != COLOR.WHITE) textColor = color.value
                        }
                    }
                }.lparams(width = matchParent, height = wrapContent)
            }
        }.view
    }

}
