package by.denchik.realmnotes.activity

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import by.denchik.realmnotes.R
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

class MainActivity : AppCompatActivity() {

    val ui = UI()
    val realm = Realm.getDefaultInstance()!!
    lateinit var notes: RealmResults<Note>
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui.setContentView(this)
        supportActionBar?.title = resources.getString(R.string.notes)
        notes = realm.where(Note::class.java).findAllSorted("date", Sort.DESCENDING)
        ui.countTextView.text = notes.size.toString()
        ui.recycler.adapter = RecyclerViewAdapter()
        notes.addChangeListener {
            ui.recycler.adapter.notifyDataSetChanged()
            ui.countTextView.text = notes.size.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.removeAllChangeListeners()
        realm.close()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.add).apply {
            setIcon(R.drawable.ic_add)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            setOnMenuItemClickListener {
                startActivity<AddActivity>()
                return@setOnMenuItemClickListener true
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    class UI : AnkoComponent<MainActivity> {
        lateinit var countTextView: TextView
        lateinit var recycler: RecyclerView
        override fun createView(ui: AnkoContext<MainActivity>) = ui.apply {
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
                        isNestedScrollingEnabled = false
                    }
                }
            }
        }.view
    }

    inner class RecyclerViewAdapter() : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

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
                holder.text.text = note.text
            }
            holder.date.text = dateFormatter.format(note.date)
            if (note.color != null && note.color.isNotEmpty()) {
                holder.layout.backgroundColor = COLOR.valueOf(note.color).value
            }
            holder.itemView.setOnClickListener {
                startActivity<AddActivity>("id" to note.id)
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
