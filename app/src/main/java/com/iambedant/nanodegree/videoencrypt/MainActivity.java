package com.iambedant.nanodegree.videoencrypt;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.iambedant.xcript.Encrypter;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 11;
    Encrypter encrypter = new Encrypter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button encryptVideo = (Button) findViewById(R.id.encryptVideo);
        Button decryptVideo = (Button) findViewById(R.id.decryptVideo);

        encryptVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });


        decryptVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri storeUri = Uri.parse("file:///storage/emulated/0/video%253A5558");
                File out = new File(outputPath);
                if (out.exists()) {
                    out.delete();
                }
                encrypter.decryptFile(storeUri, outputPath, key);
            }
        });
    }


    public void performFileSearch() {

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(i, REQUEST_TAKE_GALLERY_VIDEO);
    }

    String key;
    String outputPath;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {

                Uri selectedUri = data.getData();
                Uri storeUri = Uri.parse("file:///storage/emulated/0/video%253A5558");
                outputPath = mf_szGetRealPathFromURI(selectedUri);
                key = encrypter.encryptFile(mf_szGetRealPathFromURI(selectedUri), storeUri);
            }
        }
    }

    public String mf_szGetRealPathFromURI(final Uri ac_Uri) {
        String result = "";
        boolean isok = false;

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(ac_Uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
            isok = true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isok ? result : "";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
