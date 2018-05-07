package com.latihan.adib.adultcontentdetection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("YOUR-MICROSOFT-COGNITIVE-KEY","https://westcentralus.api.cognitive.microsoft.com/vision/v1.0");
    ByteArrayInputStream inputStream;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnProses = (Button)findViewById(R.id.btnProses);
        Button btnGetPic = (Button)findViewById(R.id.btnGetPic);

        final RadioButton rdAdult = (RadioButton)findViewById(R.id.isAdult);
        final RadioButton rdRacy = (RadioButton)findViewById(R.id.isRacy);
        final RadioButton rdGood = (RadioButton)findViewById(R.id.isGood);

        btnGetPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //IMAGE CAPTURE CODE
                startActivityForResult(intent, 0);
            }
        });

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

                    @Override
                    protected String doInBackground(InputStream... params) {
                        try {
                            publishProgress("Recognizing...");
                            String[] features = {"Adult"};
                            String[] details = {};

                            AnalysisResult result = visionServiceClient.analyzeImage(params[0], features, details);

                            String strResult = new Gson().toJson(result);
                            return strResult;

                        } catch (Exception e) {
                            return e.getMessage();
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mDialog.dismiss();
                        AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                        TextView txtDesc = (TextView)findViewById(R.id.txtDesc);
                        StringBuilder text = new StringBuilder();
                        text.append("File Type : "+result.metadata.format+" "+"Width: "+result.metadata.width
                        +" Height: "+result.metadata.height);

                        txtDesc.setText(text);

                        if(result.adult.isAdultContent == true) {
                            rdAdult.setChecked(true);
                            rdRacy.setChecked(false);
                            rdGood.setChecked(false);
                        }
                        else if(result.adult.isRacyContent == true) {
                            rdRacy.setChecked(true);
                            rdAdult.setChecked(false);
                            rdGood.setChecked(false);
                        }
                        else{
                            rdGood.setChecked(true);
                            rdRacy.setChecked(false);
                            rdAdult.setChecked(false);
                        }
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        mDialog.setMessage(values[0]);
                    }
                };

                visionTask.execute(inputStream);
            }
        });

    }
}
