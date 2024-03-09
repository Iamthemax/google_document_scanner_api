package com.example.docscanner

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import android.content.IntentSender
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent=Intent(this,ThirdActivity::class.java)
        startActivity(intent)
    }
    fun scanDocument() {
        try {
            val options = GmsDocumentScannerOptions.Builder()
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF,GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setGalleryImportAllowed(false)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .setPageLimit(3)

//            GmsDocumentScanning.getClient(options.build())
//                .getStartScanIntent(this)
//                .addOnSuccessListener { intentSender: IntentSender ->
//                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
//                }
//                .addOnFailureListener() { e: Exception ->
//                    e.message?.let { Log.e("error", it) }
//                }

        }catch (e:Exception){
            e.stackTrace
        }
    }
    private fun handleActivityResult(activityResult: ActivityResult) {
        try {
            val resultCode = activityResult.resultCode
            val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            if (resultCode == Activity.RESULT_OK && result != null) {

                result.pages?.let { pages ->
                    for (page in pages) {
                        val imageUri = pages.get(0).getImageUri()
                    }
                }
                result.pdf?.uri?.path?.let { path ->
                    val externalUri = FileProvider.getUriForFile(this, packageName + ".provider", File(path))
                    val shareIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_STREAM, externalUri)
                            type = "application/pdf"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    startActivity(Intent.createChooser(shareIntent, "share pdf"))
                }
            }
        }catch (e:Exception){
            e.stackTrace
        }
    }
}