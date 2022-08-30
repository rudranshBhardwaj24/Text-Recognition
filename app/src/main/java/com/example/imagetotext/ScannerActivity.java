package com.example.imagetotext;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {
    private ImageView capture;
    private TextView resultTV;
    private Button cameraBtn, generateBtn;
    private Bitmap ImageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_scanner);
        capture = findViewById(R.id.idCapture);
        resultTV = findViewById(R.id.idTxt);
        cameraBtn = findViewById(R.id.idBtnCamera);
        generateBtn = findViewById(R.id.idBtnGenerate);

        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission())
                {
                    captureImage();
                }
                else
                {
                    requestPermisson();
                }
            }
        });

    }
    private boolean checkPermission()
    {
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermisson()
    {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[] {CAMERA}, PERMISSION_CODE);
    }

    private void captureImage()
    {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0)
        {
            boolean cameraPermission = grantResults[0]== PackageManager.PERMISSION_GRANTED;
            if(cameraPermission)
            {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                captureImage();
            }
            else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            ImageBitmap = (Bitmap) extras.get("data");
            capture.setImageBitmap(ImageBitmap);
        }
    }

    private void detectText()
    {
        InputImage image = InputImage.fromBitmap(ImageBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for(Text.TextBlock block: text.getTextBlocks())
                {
                    String blockText = block.getText();
                    Point [] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for(Text.Line line: block.getLines())
                    {
                        String lineText = line.getText();
                        Point [] lineCornerPoint = line.getCornerPoints();
                        Rect lineRext = line.getBoundingBox();
                        for(Text.Element element: line.getElements())
                        {
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultTV.setText(blockText);
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Failed to Detect Text", Toast.LENGTH_SHORT).show();
            }
        });
    }
}