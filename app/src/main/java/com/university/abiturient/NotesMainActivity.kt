package com.university.abiturient

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.abiturient.databinding.ActivityNotesMainBinding
import com.university.abiturient.database.NotesDb
import com.university.abiturient.ui.NoteRowAdapter

class NotesMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesMainBinding
    private lateinit var db: NotesDb
    private lateinit var adapter: NoteRowAdapter
    private var showOnlyWithPhoto = false

    private val editNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) refreshList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_notes_extra)

        db = NotesDb(this)
        adapter = NoteRowAdapter { note ->
            editNoteLauncher.launch(
                Intent(this, NoteEditActivity::class.java)
                    .putExtra(NoteEditActivity.EXTRA_NOTE_ID, note.id)
            )
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.fabAdd.setOnClickListener {
            editNoteLauncher.launch(
                Intent(this, NoteEditActivity::class.java)
                    .putExtra(NoteEditActivity.EXTRA_NOTE_ID, 0L)
            )
        }

        binding.chipAll.setOnClickListener {
            showOnlyWithPhoto = false
            updateChips()
            refreshList()
        }
        binding.chipWithPhoto.setOnClickListener {
            showOnlyWithPhoto = true
            updateChips()
            refreshList()
        }

        updateChips()
        refreshList()
    }

    private fun updateChips() {
        binding.chipAll.isChecked = !showOnlyWithPhoto
        binding.chipWithPhoto.isChecked = showOnlyWithPhoto
    }

    private fun refreshList() {
        val notes = if (showOnlyWithPhoto) db.getNotesWithImage() else db.getAllNotes()
        adapter.submitList(NoteRowAdapter.fromNotes(notes))
        binding.emptyState.text =
            if (notes.isEmpty()) getString(R.string.empty_notes) else ""
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
