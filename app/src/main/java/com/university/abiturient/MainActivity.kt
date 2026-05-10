package com.university.abiturient

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.university.abiturient.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLowGrades.setOnClickListener {
            startActivity(Intent(this, LowGradesActivity::class.java))
        }
        binding.btnSorted.setOnClickListener {
            startActivity(Intent(this, SortedApplicantsActivity::class.java))
        }
        binding.btnSemiPassing.setOnClickListener {
            startActivity(Intent(this, SemiPassingActivity::class.java))
        }
        binding.btnNotesExtra.setOnClickListener {
            startActivity(Intent(this, NotesMainActivity::class.java))
        }
    }
}
