package com.university.abiturient.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.university.abiturient.databinding.ItemApplicantRowBinding

data class ApplicantRow(
    val facultyId: Long,
    val column1: String,
    val column2: String,
    val column3: String
)

class ApplicantRowAdapter(
    private var rows: List<ApplicantRow> = emptyList()
) : RecyclerView.Adapter<ApplicantRowAdapter.VH>() {

    fun submitList(newRows: List<ApplicantRow>) {
        rows = newRows
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemApplicantRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(rows[position])
    }

    override fun getItemCount(): Int = rows.size

    class VH(private val binding: ItemApplicantRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: ApplicantRow) {
            binding.iconFaculty.setImageResource(FacultyIconMapper.iconForFacultyId(row.facultyId))
            binding.textCol1.text = row.column1
            binding.textCol2.text = row.column2
            binding.textCol3.text = row.column3
        }
    }
}
