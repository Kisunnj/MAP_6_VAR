package com.university.abiturient

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.university.abiturient.databinding.ActivityNoteEditBinding
import com.university.abiturient.database.NotesDb
import com.university.abiturient.util.PhotoStorage
import java.io.File

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private lateinit var db: NotesDb
    private var noteId: Long = 0L
    private var imagePath: String? = null
    private var pendingPhotoFile: File? = null
    private var pendingPhotoUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingPhotoFile
        val uri = pendingPhotoUri
        pendingPhotoFile = null
        pendingPhotoUri = null
        if (!success || file == null) {
            PhotoStorage.deleteIfExists(file?.absolutePath)
            uri?.let { revokeUriPermission(it) }
            return@registerForActivityResult
        }
        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(this, R.string.photo_failed, Toast.LENGTH_SHORT).show()
            PhotoStorage.deleteIfExists(file.absolutePath)
            return@registerForActivityResult
        }
        attachPhotoPath(file.absolutePath)
        uri?.let { revokeUriPermission(it) }
    }

    private val pickFromGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        val path = PhotoStorage.copyFromGalleryUri(this, uri)
        if (path == null) {
            Toast.makeText(this, R.string.photo_pick_failed, Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        attachPhotoPath(path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = NotesDb(this)
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, 0L)

        if (noteId != 0L) {
            val note = db.getNote(noteId)
            if (note == null) {
                finish()
                return
            }
            title = getString(R.string.title_edit_note)
            binding.inputTitle.setText(note.title)
            binding.inputBody.setText(note.body)
            imagePath = note.imagePath
            showImagePreview(imagePath)
        } else {
            title = getString(R.string.title_new_note)
        }

        binding.btnTakePhoto.setOnClickListener { dispatchTakePictureIntent() }
        binding.btnPickGallery.setOnClickListener { pickFromGallery.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener { removePhoto() }
        binding.btnSave.setOnClickListener { saveNote() }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile = PhotoStorage.createImageFile(this)
        val photoUri = PhotoStorage.uriForFile(this, photoFile)
        pendingPhotoFile = photoFile
        pendingPhotoUri = photoUri

        val probe = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val hasCamera = packageManager.resolveActivity(probe, 0) != null
        if (!hasCamera) {
            Toast.makeText(this, R.string.no_camera_app_hint, Toast.LENGTH_LONG).show()
            pendingPhotoFile = null
            pendingPhotoUri = null
            PhotoStorage.deleteIfExists(photoFile.absolutePath)
            return
        }

        PhotoStorage.grantCameraUriPermissions(this, probe, photoUri)
        takePicture.launch(photoUri)
    }

    private fun attachPhotoPath(newPath: String) {
        val oldPath = imagePath
        imagePath = newPath
        PhotoStorage.deleteIfExists(oldPath)
        showImagePreview(imagePath)
    }

    private fun removePhoto() {
        PhotoStorage.deleteIfExists(imagePath)
        imagePath = null
        binding.imagePreview.setImageDrawable(null)
        binding.imagePreview.setBackgroundResource(R.drawable.bg_photo_placeholder)
    }

    private fun revokeUriPermission(uri: Uri) {
        try {
            revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        } catch (_: SecurityException) {
        }
    }

    private fun showImagePreview(path: String?) {
        if (path != null && File(path).exists()) {
            binding.imagePreview.setBackgroundResource(0)
            binding.imagePreview.setImageURI(PhotoStorage.uriForFile(this, File(path)))
        } else {
            binding.imagePreview.setImageDrawable(null)
            binding.imagePreview.setBackgroundResource(R.drawable.bg_photo_placeholder)
        }
    }

    private fun saveNote() {
        val title = binding.inputTitle.text?.toString()?.trim().orEmpty()
        val body = binding.inputBody.text?.toString()?.trim().orEmpty()
        if (body.isEmpty()) {
            binding.inputBody.error = getString(R.string.error_body_required)
            return
        }

        if (noteId == 0L) {
            db.insertNote(
                title = title.ifBlank { getString(R.string.untitled) },
                body = body,
                imagePath = imagePath
            )
        } else {
            db.updateNote(
                id = noteId,
                title = title.ifBlank { getString(R.string.untitled) },
                body = body,
                imagePath = imagePath
            )
        }
        setResult(RESULT_OK)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (noteId != 0L) {
            menuInflater.inflate(R.menu.menu_note_edit, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                db.getNote(noteId)?.imagePath?.let { PhotoStorage.deleteIfExists(it) }
                db.deleteNote(noteId)
                setResult(RESULT_OK)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
    }
}
