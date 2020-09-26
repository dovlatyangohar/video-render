package com.example.videorenderapp;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;

import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.videorenderapp.model.MediaObject;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    VideoPlayerAdapter adapter;
    private List<MediaObject> mediaObjects = new ArrayList<>();
    private Gson gson;

    String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();


        gson = new Gson();
        MediaObject mediaObject;
        mediaObjects = new ArrayList<>();
        String json = loadJSONFromAsset();

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mediaJsonObject = jsonArray.getJSONObject(i);
                mediaObject = new Gson().fromJson(mediaJsonObject.toString(), MediaObject.class);
                mediaObjects.add(mediaObject);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        recyclerView = findViewById(R.id.recyclerView);

        initRecyclerView();


    }


    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.media_placeholder)
                .error(R.drawable.media_placeholder);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new VideoPlayerAdapter(mediaObjects, initGlide());

        adapter.setThumbnailClickListener((mediaObject, position) -> {
            if (isInternetConnected() && isFileAvailable(mediaObject.getMediaFileName())) {
                Log.i("MEDIAFILNAME", mediaObject.getMediaFileName());
                openVideoPlayerActivity(mediaObject);
            }
            if (isInternetConnected() && !isFileAvailable(mediaObject.getMediaFileName())) {
                openVideoPlayerActivity(mediaObject);
                Toast.makeText(MainActivity.this, "Playing from the internet..", Toast.LENGTH_SHORT).show();

            }
            if (!isInternetConnected() && isFileAvailable(mediaObject.getMediaFileName())) {
                openVideoPlayerActivity(mediaObject);
                Toast.makeText(MainActivity.this, "Playing offline", Toast.LENGTH_SHORT).show();
            }
            if (!isInternetConnected() && !isFileAvailable(mediaObject.getMediaFileName())) {
                Toast.makeText(MainActivity.this, "Make sure you are connected to the internet..", Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setDownloadClickListener((mediaObject, position) -> {


            if (mediaObject.isDownloaded()) {
                openDeleteMediaDialog(mediaObject, adapter);
            }else {
                openDownloadDialog(mediaObject, adapter);
            }

        });

        recyclerView.setAdapter(adapter);
    }


    private void openDownloadDialog(MediaObject mediaObject, VideoPlayerAdapter adapter) {

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Downloading media")
                .setMessage("Are you sure you want to download this media?")
                .setCancelable(false)
                .setPositiveButton("Download", (dialogClickListener, which) -> {
                    downloadMedia(mediaObject);

                })
                .setNegativeButton("Cancel", (dialogClickListener, which) -> dialogClickListener.dismiss())
                .create();
        dialog.show();

    }

    private void openDeleteMediaDialog(MediaObject mediaObject, VideoPlayerAdapter adapter) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Remove media from downloads")
                .setMessage("Are you sure you want to remove this media from downloads?")
                .setCancelable(false)
                .setPositiveButton("Delete", (dialogClickListener, which) -> {
                    File dir = getFilesDir();
                    File file = new File(dir, mediaObject.getMediaFileName());
                    boolean deleted = file.delete();
                    if (deleted) {
                        Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
                        mediaObjects.get(adapter.getTargetPosition()).setDownloaded(false);
                        adapter.notifyItemChanged(adapter.getTargetPosition());
                    }

                })
                .setNegativeButton("Cancel", (dialogClickListener, which) -> dialogClickListener.dismiss())
                .create();
        dialog.show();
    }


    private void downloadMedia(MediaObject mediaObject) {

        final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
        downloadTask.execute(mediaObject.getMediaURL());
        Toast.makeText(MainActivity.this, "Downloading..", Toast.LENGTH_LONG).show();


    }

    private void openVideoPlayerActivity(MediaObject object) {

        String mediaJson = gson.toJson(object);
        Intent i = new Intent(MainActivity.this, VideoPlayerActivity.class);
        i.putExtra("mediaObject", object);
        i.putExtra("mediaJson", mediaJson);
        startActivity(i);
    }


    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream inputStream = this.getAssets().open("media.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }


        @Override
        protected String doInBackground(String... sUrl) {

            InputStream input = null;
            OutputStream output = null;

            HttpURLConnection connection = null;
            if (!isInternetConnected()) {

                return "Switch on to the internet to download file";
            }

            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                fileName = sUrl[0].substring(sUrl[0].lastIndexOf("/") + 1);
                if (isFileAvailable(fileName)) {
                    return fileName + " file has already been downloaded";
                }


                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file

                byte[] data = new byte[4096];
                long total = 0;
                int count;

                input = connection.getInputStream();

                output = openFileOutput(fileName, Context.MODE_PRIVATE);

                while ((count = input.read(data)) != -1) {

                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }


            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false

        }

        @Override
        protected void onPostExecute(String result) {

            mWakeLock.release();


            if (result != null) {
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(context, "File " + fileName + " downloaded", Toast.LENGTH_SHORT).show();

            }


            mediaObjects.get(adapter.getTargetPosition()).setDownloaded(true);

            Log.i("Downloaded", adapter.getTargetPosition() + "");
            adapter.notifyItemChanged(adapter.getTargetPosition());


        }
    }

    private boolean isFileAvailable(String fileName) {
        final String location = getApplication().getFilesDir().getPath() + "/" + fileName;

        File file = new File(location);
        return file.exists();

    }


    public boolean isInternetConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

}



