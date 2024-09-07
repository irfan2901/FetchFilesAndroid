package com.example.fetchfiles

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var pickFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickFolderButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var treeUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    pickFolder()
                } else {
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()
                }
            }

        pickFolderLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        treeUri = uri
                        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
                        prefs.edit().putString("folderUri", uri.toString()).apply()
                        fetchMediaFiles()
                    }
                }
            }

        pickFolderButton = findViewById(R.id.pickFolderButton)
        recyclerView = findViewById(R.id.recyclerView)

        checkPermission()

        pickFolderButton.setOnClickListener { pickFolder() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
//                        pickFolder()
                    }

                    else -> {
                        permissionLauncher.launch(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    }
                }
            }

            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
//                        pickFolder()
                    }

                    else -> {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun pickFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        pickFolderLauncher.launch(intent)
    }

    private fun fetchMediaFiles() {
        val mediaFiles = mutableListOf<Uri>()
        val resolver = contentResolver

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val folderUriString = prefs.getString("folderUri", null) ?: return
        val folderUri = Uri.parse(folderUriString)

        val documentTree = DocumentFile.fromTreeUri(this, folderUri) ?: return
        val files = documentTree.listFiles()

        files.forEach { file ->
            if (file.isFile) {
                val fileUri = file.uri
                val mimeType = resolver.getType(fileUri) ?: ""

                when {
                    mimeType.startsWith("image/") -> mediaFiles.add(fileUri)
                    mimeType.startsWith("audio/") -> mediaFiles.add(fileUri)
                    mimeType.startsWith("video/") -> mediaFiles.add(fileUri)
                }
            }
        }
        setUpRecyclerView(mediaFiles)
    }

    private fun setUpRecyclerView(mediaFiles: MutableList<Uri>) {
        recyclerView.layoutManager = LinearLayoutManager(this)
        mediaAdapter = MediaAdapter(mediaFiles, this)
        recyclerView.adapter = mediaAdapter
    }
}