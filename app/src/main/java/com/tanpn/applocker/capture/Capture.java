package com.tanpn.applocker.capture;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tanpn.applocker.R;
import com.tanpn.applocker.sqlite.SQLAppUnLock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Capture extends AppCompatActivity {
    Preview preview;
    Camera camera;

    Context context;
    String mPackageName;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Bundle bun = getIntent().getExtras();
        mPackageName = bun.getString("package");
        Log.i("capture", "create");
        context = this;
        handler = new Handler();

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);


        new Thread(){
            @Override
            public void run() {
                super.run();

                SystemClock.sleep(800);
                while (camera == null){
                    SystemClock.sleep(500);
                }
                camera.takePicture(null, null, jpegCallback);
                Log.i("capture", "capture");

            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{

                camera = Camera.open(1);
                camera.startPreview();
                preview.setCamera(camera);

                Camera.Parameters parameters = camera.getParameters();
                parameters.set("orientation", "portrait");
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
            } catch (RuntimeException ex){
                //Toast.makeText(ctx, "camrea not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }


    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
           // resetCam();
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, String> {


        @Override
        protected String doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/AppLocker");
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                refreshGallery(outFile);

                return outFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            // luu vao sql
            new SQLAppUnLock(context).insert(mPackageName,s );

            Log.i("capture", "done");

            handler.postDelayed(new closeApp(), 1000);
            // delay 300ms roi close app
        }
    }

    class closeApp implements  Runnable{

        @Override
        public void run() {
            close();
        }
    }

    private void close(){

        // trở về màn hình home
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
