package com.university.abiturient

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.abiturient.databinding.ActivityQueryListBinding
import com.university.abiturient.database.AbiturientDb
import com.university.abiturient.model.Exam
import com.university.abiturient.ui.ApplicantRow
import com.university.abiturient.ui.ApplicantRowAdapter

class LowGradesActivity : AppCompatActivity() {
    private lateinit var db: AbiturientDb
    private lateinit var binding: ActivityQueryListBinding
    private lateinit var adapter: ApplicantRowAdapter
    private var exams: List<Exam> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AbiturientDb(this)
        adapter = ApplicantRowAdapter()
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        exams = db.getAllExams()
        val labels = exams.map { "${it.code} — ${it.subjectName} (${it.date})" }
        binding.spinnerExam.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)

        binding.hint.text = getString(R.string.hint_low_grades)
        binding.btnRun.text = getString(R.string.show_results)

        binding.btnRun.setOnClickListener {
            if (exams.isEmpty()) return@setOnClickListener
            val pos = binding.spinnerExam.selectedItemPosition
            if (pos < 0 || pos >= exams.size) return@setOnClickListener
            val examId = exams[pos].id
            val list = db.getApplicantsWithLowGradeOnExam(examId, threshold = 7.0)
            val rows = list.map { (a, grade) ->
                ApplicantRow(
                    facultyId = a.facultyId,
                    column1 = a.fullName,
                    column2 = getString(R.string.col_grade_on_exam, grade),
                    column3 = "${getString(R.string.col_certificate_avg, a.certificateAvg)} · ${db.getFaculty(a.facultyId)?.name ?: "—"}"
                )
            }
            adapter.submitList(rows)
            binding.emptyState.text =
                if (rows.isEmpty()) getString(R.string.empty_low_grades) else ""
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
