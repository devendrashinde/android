package com.example.dshinde.myapplication_xmlpref.activities
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.dshinde.myapplication_xmlpref.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class BackupRestoreMediaFilesActivity : AppCompatActivity() {

    private lateinit var folderPickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var selectFolderButton: Button

    private var totalFiles = 0
    private var copiedFiles = 0
    private var selectedOperation = "Backup" // default

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_media_files) // Make sure to have progressBar & progressText in layout

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        selectFolderButton = findViewById(R.id.selectFolderButton)
        selectFolderButton.setOnClickListener { selectTargetFolder() }
        folderPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { treeUri ->
                        contentResolver.takePersistableUriPermission(
                            treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        var appStorageDir = getExternalFilesDir(null);
                        if( appStorageDir == null) {
                            progressText.text = "Application Storage not available"
                        } else {
                            startBackupRestore(appStorageDir, treeUri)
                        }
                    }
                }
            }

        val radioGroup = findViewById<RadioGroup>(R.id.operationTypeGroup)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId == R.id.restoreRadio) {
                selectFolderButton.text = "Select Source Folder"
                selectedOperation = "Restore"
            } else {
                selectFolderButton.text = "Select Destination Folder"
                selectedOperation = "Backup"
            }
        }
    }
    private fun startBackupRestore(appStorageDir: File, selectedDir: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            // Count total files first
            if (selectedOperation == "Backup") {
                DocumentFile.fromTreeUri(applicationContext, selectedDir)?.let { totalFiles = countFilesSAF(it) }
            } else {
                totalFiles = countFiles(appStorageDir)
            }
            copiedFiles = 0

            withContext(Dispatchers.Main) {
                progressBar.max = totalFiles
                progressBar.progress = 0
                progressText.text = "Copying 0 / $totalFiles files..."
            }

            if (selectedOperation == "Restore") {
                startRestore(selectedDir, appStorageDir)
            } else {
                startBackup(appStorageDir, selectedDir)
            }

            withContext(Dispatchers.Main) {
                progressText.text = "Copy complete! ($copiedFiles files)"
            }
        }
    }

    private fun selectTargetFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }
        folderPickerLauncher.launch(intent)
    }

    private suspend fun startRestore(sourceTreeUri: Uri, destDir: File) {
        val pickedDir = DocumentFile.fromTreeUri(this, sourceTreeUri)
        if (pickedDir != null) {
            copyRecursiveFromSAF(pickedDir, destDir)
        }
    }

    private suspend fun copyRecursiveFromSAF(source: DocumentFile, destDir: File) {
        if (source.isDirectory) {
            val newDir = File(destDir, source.name ?: "Unnamed")
            if (!newDir.exists()) newDir.mkdirs()

            source.listFiles().forEach { child ->
                copyRecursiveFromSAF(child, newDir)
            }
        } else {
            copyFileFromSAF(source, destDir)
        }
    }

    private suspend fun copyFileFromSAF(sourceFile: DocumentFile, destDir: File) {
        withContext(Dispatchers.IO) {
            try {
                val destFile = File(destDir, sourceFile.name ?: "Unnamed")
                if (destFile.exists()) {
                    copiedFiles++
                    withContext(Dispatchers.Main) {
                        progressBar.progress = copiedFiles
                        progressText.text = "Skipping ${sourceFile.name} ($copiedFiles / $totalFiles)"
                    }
                    return@withContext // Skip if exists
                }

                contentResolver.openInputStream(sourceFile.uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output, bufferSize = 8192)
                    }
                }
                // Update progress
                copiedFiles++
                withContext(Dispatchers.Main) {
                    progressBar.progress = copiedFiles
                    progressText.text = "Copying $copiedFiles / $totalFiles files..."
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    private suspend fun startBackup(sourceDir: File, targetDir: Uri) {
        DocumentFile.fromTreeUri(this, targetDir)?.let { backUpAppsMediaFiles(sourceDir, it) }
    }

    private suspend fun backUpAppsMediaFiles(source: File, targetDir: DocumentFile) {
        source.listFiles()?.forEach { child ->
            copyRecursive(child, targetDir)
        }
    }

    private suspend fun copyRecursive(source: File, targetDir: DocumentFile) {
        if (source.isDirectory) {
            val newDir = targetDir.findFile(source.name)?.takeIf { it.isDirectory }
                ?: targetDir.createDirectory(source.name)!!
            source.listFiles()?.forEach { child ->
                copyRecursive(child, newDir)
            }
        } else {
            copyFile(source, targetDir)
        }
    }

    private suspend fun copyFile(source: File, targetDir: DocumentFile) {
        try {
            val existingFile = targetDir.findFile(source.name)

            // If file exists (any size), skip
            if (existingFile != null) {
                copiedFiles++
                withContext(Dispatchers.Main) {
                    progressBar.progress = copiedFiles
                    progressText.text = "Skipping ${source.name} ($copiedFiles / $totalFiles)"
                }
                return
            }
            // create and copy dir/file
            val newFile = targetDir.findFile(source.name)
                ?: targetDir.createFile("application/octet-stream", source.name)!!
            contentResolver.openOutputStream(newFile.uri)?.use { out ->
                FileInputStream(source).use { input ->
                    input.copyTo(out, bufferSize = 8192)
                }
            }

            // Update progress
            copiedFiles++
            withContext(Dispatchers.Main) {
                progressBar.progress = copiedFiles
                progressText.text = "Copying $copiedFiles / $totalFiles files..."
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun countFiles(dir: File): Int {
        var count = 0
        dir.listFiles()?.forEach { file ->
            count += if (file.isDirectory) countFiles(file) else 1
        }
        return count
    }

    private fun countFilesSAF(dir: DocumentFile): Int {
        var count = 0

        if (!dir.isDirectory) return 0

        dir.listFiles().forEach { file ->
            count += if (file.isDirectory) {
                countFilesSAF(file) // recurse into subfolder
            } else {
                1 // count the file
            }
        }

        return count
    }
}
