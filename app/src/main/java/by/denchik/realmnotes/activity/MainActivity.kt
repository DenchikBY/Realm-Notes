package by.denchik.realmnotes.activity

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import by.denchik.realmnotes.R
import by.denchik.realmnotes.extensions.changeStatusBarColor
import by.denchik.realmnotes.models.COLOR
import by.denchik.realmnotes.models.Note
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    val ui = UI()
    val realm = Realm.getDefaultInstance()!!
    lateinit var notes: RealmResults<Note>
    val selectedNotes = mutableSetOf<Int>()
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        ui.firstToolbar.title = resources.getString(R.string.notes)
        firstToolbarMenu(ui.firstToolbar.menu)
        secondToolbarMenu(ui.secondToolbar.menu)
        notes = realm.where(Note::class.java).findAllSorted("date", Sort.DESCENDING)
        ui.countTextView.text = notes.size.toString()
        ui.recycler.adapter = RecyclerViewAdapter()
        notes.addChangeListener {
            ui.recycler.adapter.notifyDataSetChanged()
            ui.countTextView.text = notes.size.toString()
        }
        ui.secondToolbar.setNavigationOnClickListener {
            secondToolbarBackClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.removeAllChangeListeners()
        realm.close()
    }

    override fun onBackPressed() {
        if (selectedNotes.isNotEmpty()) {
            secondToolbarBackClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
            (ui.recycler.layoutManager as StaggeredGridLayoutManager).spanCount = 3
            //ui.recycler.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
            //ui.recycler.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    fun firstToolbarMenu(menu: Menu) {
        menu.add(R.string.add).apply {
            setIcon(R.drawable.ic_add)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                startActivity<AddActivity>()
                return@setOnMenuItemClickListener true
            }
        }
    }

    fun secondToolbarMenu(menu: Menu) {
        menu.add(R.string.select_all).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                for (i in 0..(notes.size - 1)) {
                    selectItem(i, false)
                    ui.secondToolbar.title = selectedNotes.size.toString()
                }
                return@setOnMenuItemClickListener true
            }
        }
        menu.add(R.string.delete).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                realm.executeTransaction {
                    for (i in selectedNotes.sortedDescending()) {
                        notes.deleteFromRealm(i)
                    }
                }
                ui.firstToolbar.visibility = View.VISIBLE
                ui.secondToolbar.visibility = View.GONE
                ui.countTextView.text = notes.size.toString()
                selectedNotes.clear()
                return@setOnMenuItemClickListener true
            }
        }
    }

    fun secondToolbarBackClick() {
        selectedNotes.toList().forEach { deselectItem(it, false) }
        selectedNotes.clear()
        ui.firstToolbar.visibility = View.VISIBLE
        ui.secondToolbar.visibility = View.GONE
    }

    fun selectItem(position: Int, displayCounter: Boolean = true) {
        if (selectedNotes.isEmpty()) {
            ui.firstToolbar.visibility = View.GONE
            ui.secondToolbar.visibility = View.VISIBLE
            window.changeStatusBarColor(R.color.material_grey_600)
        }
        selectedNotes.add(position)
        val holder = ui.recycler.findViewHolderForAdapterPosition(position) as RecyclerViewAdapter.ViewHolder
        holder.layout.backgroundColor = Color.GRAY
        if (displayCounter) ui.secondToolbar.title = selectedNotes.size.toString()
    }

    fun deselectItem(position: Int, displayCounter: Boolean = true) {
        selectedNotes.remove(position)
        val note = notes[position]
        val holder = ui.recycler.findViewHolderForAdapterPosition(position) as RecyclerViewAdapter.ViewHolder
        if (note.color != null && note.color.isNotEmpty()) {
            holder.layout.backgroundColor = COLOR.valueOf(note.color).value
        } else {
            holder.layout.backgroundColor = COLOR.WHITE.value
        }
        if (displayCounter) ui.secondToolbar.title = selectedNotes.size.toString()
        if (selectedNotes.isEmpty()) {
            ui.firstToolbar.visibility = View.VISIBLE
            ui.secondToolbar.visibility = View.GONE
            window.changeStatusBarColor(R.color.colorPrimaryDark)
        }
    }

    class UI : AnkoComponent<MainActivity> {
        lateinit var countTextView: TextView
        lateinit var recycler: RecyclerView
        lateinit var firstToolbar: Toolbar
        lateinit var secondToolbar: Toolbar
        override fun createView(ui: AnkoContext<MainActivity>) = ui.apply {
            verticalLayout {
                firstToolbar = toolbar(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                    elevation = dip(4).toFloat()
                    backgroundResource = R.color.colorPrimary
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                }
                secondToolbar = toolbar(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
                    elevation = dip(4).toFloat()
                    backgroundResource = R.color.material_grey_300
                    popupTheme = R.style.ThemeOverlay_AppCompat_Light
                    visibility = View.GONE
                    navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back, null)
                }
                nestedScrollView {
                    verticalLayout {
                        linearLayout {
                            horizontalPadding = dip(15)
                            topPadding = dip(5)
                            textView {
                                textResource = R.string.notes_count
                                textSize = sp(8).toFloat()
                                textColor = Color.BLACK
                            }
                            countTextView = textView {
                                textSize = sp(8).toFloat()
                                textColor = Color.BLACK
                                leftPadding = dip(5)
                            }
                        }
                        recycler = recyclerView {
                            isFocusable = false
                            bottomPadding = dip(15)
                            horizontalPadding = dip(7.5F)
                            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                            itemAnimator = DefaultItemAnimator()
                            isNestedScrollingEnabled = false
                        }
                    }
                }
            }
        }.view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = NoteUI().createView(AnkoContext.create(baseContext, parent))
            return ViewHolder(v)
        }

        override fun getItemCount() = notes.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val note = notes[position]
            if (note.title.isEmpty()) {
                holder.title.visibility = View.GONE
            } else {
                holder.title.text = note.title
            }
            if (note.text.isEmpty()) {
                holder.text.visibility = View.GONE
            } else {
                holder.text.text = if (note.text.length > 100) "${note.text.substring(0, 99)}..." else note.text
            }
            holder.date.text = dateFormatter.format(note.date)
            if (note.color != null && note.color.isNotEmpty()) {
                holder.layout.backgroundColor = COLOR.valueOf(note.color).value
            }
            holder.layout.setOnClickListener {
                if (selectedNotes.isNotEmpty()) {
                    if (selectedNotes.contains(position)) {
                        deselectItem(position)
                    } else {
                        selectItem(position)
                    }
                } else {
                    startActivity<AddActivity>("id" to note.id)
                }
            }
            holder.layout.setOnLongClickListener {
                selectItem(position)
                return@setOnLongClickListener true
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ids = NoteUI.ids
            val title = itemView.find<TextView>(ids.title)
            val text = itemView.find<TextView>(ids.text)
            val date = itemView.find<TextView>(ids.date)
            val layout = itemView.find<CardView>(ids.layout)
        }

    }

    class NoteUI : AnkoComponent<ViewGroup> {
        companion object ids {
            val title = 1
            val text = 2
            val date = 3
            val layout = 4
        }
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            return with(ui) {
                verticalLayout {
                    lparams(width = matchParent)
                    cardView {
                        id = ids.layout
                        backgroundColor = Color.WHITE
                        lparams(width = matchParent, height = wrapContent) {
                            horizontalMargin = dip(7.5F)
                            verticalMargin = dip(7.5F)
                        }
                        verticalLayout {
                            padding = dip(10)
                            textView {
                                id = ids.title
                                textSize = sp(6).toFloat()
                                setTypeface(null, Typeface.BOLD)
                            }
                            textView {
                                id = ids.text
                                textSize = sp(5).toFloat()
                            }
                            textView {
                                id = ids.date
                                textSize = sp(3).toFloat()
                            }
                        }.applyRecursively { view -> when(view) {
                            is TextView -> view.textColor = Color.BLACK
                        }}
                    }
                }
            }
        }
    }

}
