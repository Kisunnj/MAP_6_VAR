package com.university.abiturient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.university.abiturient.databinding.ActivitySimpleListBinding
import com.university.abiturient.database.AbiturientDb
import com.university.abiturient.database.Scoring
import com.university.abiturient.ui.ApplicantRow
import com.university.abiturient.ui.ApplicantRowAdapter

class SemiPassingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySimpleListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.description.text = getString(
            R.string.desc_semi_passing,
            Scoring.SEMI_PASS_MIN,
            Scoring.SEMI_PASS_MAX
        )

        val db = AbiturientDb(this)
        val list = db.getApplicantsSemiPassing()
        val rows = list.map { (a, examAvg, combined) ->
            val examPart = examAvg?.let { getString(R.string.col_exam_avg, it) }
                ?: getString(R.string.no_exam_grades)
            ApplicantRow(
                facultyId = a.facultyId,
                column1 = a.fullName,
                column2 = getString(R.string.col_combined_avg, combined),
                column3 = "${getString(R.string.col_certificate_short, a.certificateAvg)} · $examPart"
            )
        }
        val adapter = ApplicantRowAdapter(rows)
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
