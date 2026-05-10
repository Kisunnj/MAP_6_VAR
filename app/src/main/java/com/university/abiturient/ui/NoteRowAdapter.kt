package com.university.abiturient.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.abiturient.databinding.ItemNoteRowBinding
import com.university.abiturient.model.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NoteRow(
    val note: Note,
    val column1: String,
    val column2: String,
    val column3: String,
    val imagePath: String?
)

class NoteRowAdapter(
    private var rows: List<NoteRow> = emptyList(),
    private val onClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteRowAdapter.VH>() {

    fun submitList(newRows: List<NoteRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNoteRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    class VH(
        private val binding: ItemNoteRowBinding,
        private val onClick: (Note) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: NoteRow) {
            binding.textCol1.text = row.column1
            binding.textCol2.text = row.column2
            binding.textCol3.text = row.column3

            val path = row.imagePath
            if (path != null && File(path).exists()) {
                val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
                binding.iconNote.setImageBitmap(BitmapFactory.decodeFile(path, opts))
                binding.iconNote.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            } else {
                binding.iconNote.setImageResource(com.university.abiturient.R.drawable.ic_note_text)
                binding.iconNote.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            }

            binding.root.setOnClickListener { onClick(row.note) }
        }
    }

    companion object {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun fromNotes(notes: List<Note>): List<NoteRow> = notes.map { note ->
            NoteRow(
                note = note,
                column1 = note.title.ifBlank { "(без заголовка)" },
                column2 = note.body.lineSequence().firstOrNull()?.take(80) ?: "",
                column3 = dateFormat.format(Date(note.createdAt)),
                imagePath = note.imagePath
            )
        }
    }
}
