package com.example.docscanner;

import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF;
import static com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class ThirdActivity extends AppCompatActivity {

    Button button;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        button=findViewById(R.id.btnScan);
        imageView=findViewById(R.id.imageView);

        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(false)
                .setPageLimit(10)
                .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
                .setScannerMode(SCANNER_MODE_FULL)
                .build();

        GmsDocumentScanner scanner = GmsDocumentScanning.getClient(options);
        ActivityResultLauncher<IntentSenderRequest> scannerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartIntentSenderForResult(),
                        results -> {
                            if (results.getResultCode() == RESULT_OK) {
                                GmsDocumentScanningResult result = GmsDocumentScanningResult.fromActivityResultIntent(results.getData());
                                for (GmsDocumentScanningResult.Page page : result.getPages()) {
                                    //Uri imageUri = pages.get(0).getImageUri();
                                    Uri imageUri1=page.getImageUri();
                                    imageView.setImageURI(imageUri1);
                                    Log.d("mytag",""+imageUri1);
                                }

                                GmsDocumentScanningResult.Pdf pdf = result.getPdf();
                                Uri pdfUri = pdf.getUri();
                                saveFile(pdfUri);
                                //File file=new File(String.valueOf(pdfUri));
                                //openPdf(Uri.parse(file.getParent()));
                                //savePdfToDevice(pdfUri);
                                int pageCount = pdf.getPageCount();
                                Log.d("mytag",""+pageCount);



                            }
                        });

//        scanner.getStartScanIntent(this)
//                .addOnSuccessListener(intentSender ->
//                        scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                        Log.d("mytag","onfailure "+e.getMessage());
//                    }
//                });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scanner.getStartScanIntent(ThirdActivity.this)
                        .addOnSuccessListener(intentSender ->
                                scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d("mytag","onfailure "+e.getMessage());
                            }
                        });
            }
        });
    }
    private void savePdfToDevice(Uri pdfUri) {
        // Retrieve the InputStream of the PDF from the ContentResolver
        try {
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            if (inputStream != null) {
                // Create a File to save the PDF
                File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "scanned_document.pdf");

                // Write the InputStream content to the File
                OutputStream outputStream = new FileOutputStream(pdfFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();

                // Add the PDF file to MediaStore
                ContentResolver contentResolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFile.getName());
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
                contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues);

                // Notify the user that the PDF has been saved
                Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                Log.d("mytag", "DF saved to: " + pdfFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("mytag", "IOException: " + e.getMessage());
        }
    }

    private void openPdf(Uri pdfUri) {
        // Create an Intent with ACTION_VIEW and set the URI of the PDF file
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");

        // Set flags to grant temporary permission to external apps to access the content
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Try to start the activity to view the PDF
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle case when no suitable app is installed to view PDF files
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }

        // Finish the activity
        finish();
    }

    public void saveFile(Uri uri){
        try {
            File mediaStorageDir = new File(getExternalMediaDirs()[0], "myfiles");
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.e("Error", "Directory not created");
                return;
            }

            Calendar calendar = Calendar.getInstance();
           // String time = convertTimeToCustomString(calendar.getTimeInMillis());
            String customFileName = "document_"; //; // Assuming customFileName is defined

            File myFile = new File(mediaStorageDir, customFileName + ".pdf");
            FileOutputStream fileOutputStream = new FileOutputStream(myFile);

            // Get the InputStream from the Uri
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Read content from the InputStream and write it to the FileOutputStream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            // Close the InputStream and FileOutputStream
            inputStream.close();
            fileOutputStream.close();

            // Now you have saved your PDF file, you can set pdfFileToOpen to myFile
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the IOException
        }
    }
}