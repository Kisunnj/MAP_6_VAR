package com.university.abiturient.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.university.abiturient.model.Applicant
import com.university.abiturient.model.Exam
import com.university.abiturient.model.Faculty
import com.university.abiturient.model.combinedAverage

class AbiturientDb(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE faculty (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                places INTEGER NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE applicant (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                faculty_id INTEGER NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
                full_name TEXT NOT NULL,
                certificate_avg REAL NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE exam (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                faculty_id INTEGER NOT NULL REFERENCES faculty(id) ON DELETE CASCADE,
                code TEXT NOT NULL,
                exam_date TEXT NOT NULL,
                subject_name TEXT NOT NULL
            );
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE grade (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                exam_id INTEGER NOT NULL REFERENCES exam(id) ON DELETE CASCADE,
                applicant_id INTEGER NOT NULL REFERENCES applicant(id) ON DELETE CASCADE,
                value REAL NOT NULL,
                UNIQUE(exam_id, applicant_id)
            );
            """.trimIndent()
        )
        seedDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS grade")
        db.execSQL("DROP TABLE IF EXISTS exam")
        db.execSQL("DROP TABLE IF EXISTS applicant")
        db.execSQL("DROP TABLE IF EXISTS faculty")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    fun getAllFaculties(): List<Faculty> {
        readableDatabase.rawQuery("SELECT id, name, places FROM faculty ORDER BY name", null)
            .use { c ->
                val list = mutableListOf<Faculty>()
                while (c.moveToNext()) {
                    list.add(Faculty(c.getLong(0), c.getString(1), c.getInt(2)))
                }
                return list
            }
    }

    fun getFaculty(id: Long): Faculty? {
        readableDatabase.rawQuery(
            "SELECT id, name, places FROM faculty WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return Faculty(c.getLong(0), c.getString(1), c.getInt(2))
        }
    }

    fun getAllExams(): List<Exam> {
        readableDatabase.rawQuery(
            """
            SELECT id, faculty_id, code, exam_date, subject_name
            FROM exam ORDER BY exam_date, code
            """.trimIndent(),
            null
        ).use { c ->
            val list = mutableListOf<Exam>()
            while (c.moveToNext()) {
                list.add(
                    Exam(
                        c.getLong(0),
                        c.getLong(1),
                        c.getString(2),
                        c.getString(3),
                        c.getString(4)
                    )
                )
            }
            return list
        }
    }

    fun getApplicantsWithLowGradeOnExam(
        examId: Long,
        threshold: Double = 7.0
    ): List<Pair<Applicant, Double>> {
        val sql = """
            SELECT a.id, a.faculty_id, a.full_name, a.certificate_avg, g.value
            FROM applicant a
            INNER JOIN grade g ON g.applicant_id = a.id
            WHERE g.exam_id = ? AND g.value < ?
            ORDER BY a.full_name
        """.trimIndent()
        readableDatabase.rawQuery(sql, arrayOf(examId.toString(), threshold.toString())).use { c ->
            val list = mutableListOf<Pair<Applicant, Double>>()
            while (c.moveToNext()) {
                val a = Applicant(
                    c.getLong(0),
                    c.getLong(1),
                    c.getString(2),
                    c.getDouble(3)
                )
                list.add(a to c.getDouble(4))
            }
            return list
        }
    }

    private fun averageExamGradeForApplicant(applicantId: Long): Double? {
        readableDatabase.rawQuery(
            "SELECT AVG(value) FROM grade WHERE applicant_id = ?",
            arrayOf(applicantId.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            if (c.isNull(0)) return null
            val avg = c.getDouble(0)
            return if (avg.isNaN()) null else avg
        }
    }

    fun getAllApplicantsWithExamAvg(): List<Pair<Applicant, Double?>> {
        readableDatabase.rawQuery(
            "SELECT id, faculty_id, full_name, certificate_avg FROM applicant ORDER BY full_name",
            null
        ).use { c ->
            val list = mutableListOf<Pair<Applicant, Double?>>()
            while (c.moveToNext()) {
                val a = Applicant(
                    c.getLong(0),
                    c.getLong(1),
                    c.getString(2),
                    c.getDouble(3)
                )
                list.add(a to averageExamGradeForApplicant(a.id))
            }
            return list
        }
    }

    fun getApplicantsSortedByCombinedAverageDescending(): List<Triple<Applicant, Double?, Double>> {
        return getAllApplicantsWithExamAvg()
            .map { (a, examAvg) ->
                Triple(a, examAvg, a.combinedAverage(examAvg))
            }
            .sortedByDescending { it.third }
    }

    fun getApplicantsSemiPassing(
        semiMin: Double = Scoring.SEMI_PASS_MIN,
        semiMax: Double = Scoring.SEMI_PASS_MAX
    ): List<Triple<Applicant, Double?, Double>> {
        return getAllApplicantsWithExamAvg()
            .map { (a, examAvg) ->
                val combined = a.combinedAverage(examAvg)
                Triple(a, examAvg, combined)
            }
            .filter { (_, _, combined) -> combined >= semiMin && combined < semiMax }
            .sortedByDescending { it.third }
    }

    private fun seedDemo(db: SQLiteDatabase) {
        val f1 = insertFaculty(db, "Факультет математики", 30)
        val f2 = insertFaculty(db, "Факультет информатики", 25)
        val f3 = insertFaculty(db, "Факультет физики", 20)

        val a1 = insertApplicant(db, f1, "Иванов Иван Иванович", 4.2)
        val a2 = insertApplicant(db, f1, "Петрова Мария Сергеевна", 4.8)
        val a3 = insertApplicant(db, f2, "Сидоров Пётр Алексеевич", 3.9)
        val a4 = insertApplicant(db, f2, "Козлова Анна Дмитриевна", 4.5)
        val a5 = insertApplicant(db, f3, "Николаев Денис Олегович", 3.6)
        val a6 = insertApplicant(db, f3, "Орлова Елена Викторовна", 4.0)
        val a7 = insertApplicant(db, f2, "Волков Артём Павлович", 3.8)
        val a8 = insertApplicant(db, f1, "Морозова Светлана Игоревна", 3.9)

        val e1 = insertExam(db, f1, "MATH-01", "2025-07-10", "Математика")
        val e2 = insertExam(db, f1, "MATH-02", "2025-07-12", "Физика")
        val e3 = insertExam(db, f2, "CS-01", "2025-07-11", "Информатика")
        val e4 = insertExam(db, f3, "PHY-01", "2025-07-13", "Физика (вступительная)")

        insertGrade(db, e1, a1, 6.5)
        insertGrade(db, e1, a2, 8.0)
        insertGrade(db, e1, a3, 5.0)
        insertGrade(db, e2, a1, 7.0)
        insertGrade(db, e2, a2, 9.0)
        insertGrade(db, e3, a3, 6.0)
        insertGrade(db, e3, a4, 8.5)
        insertGrade(db, e3, a5, 5.5)
        insertGrade(db, e4, a5, 6.8)
        insertGrade(db, e4, a6, 7.2)
        insertGrade(db, e3, a7, 3.2)
        insertGrade(db, e1, a8, 3.6)
    }

    private fun insertFaculty(db: SQLiteDatabase, name: String, places: Int): Long {
        val cv = ContentValues().apply {
            put("name", name)
            put("places", places)
        }
        return db.insert("faculty", null, cv)
    }

    private fun insertApplicant(
        db: SQLiteDatabase,
        facultyId: Long,
        fullName: String,
        certificateAvg: Double
    ): Long {
        val cv = ContentValues().apply {
            put("faculty_id", facultyId)
            put("full_name", fullName)
            put("certificate_avg", certificateAvg)
        }
        return db.insert("applicant", null, cv)
    }

    private fun insertExam(
        db: SQLiteDatabase,
        facultyId: Long,
        code: String,
        date: String,
        subject: String
    ): Long {
        val cv = ContentValues().apply {
            put("faculty_id", facultyId)
            put("code", code)
            put("exam_date", date)
            put("subject_name", subject)
        }
        return db.insert("exam", null, cv)
    }

    private fun insertGrade(db: SQLiteDatabase, examId: Long, applicantId: Long, value: Double) {
        val cv = ContentValues().apply {
            put("exam_id", examId)
            put("applicant_id", applicantId)
            put("value", value)
        }
        db.insert("grade", null, cv)
    }

    companion object {
        private const val DATABASE_NAME = "abiturient.db"
        private const val DATABASE_VERSION = 1
    }
}
