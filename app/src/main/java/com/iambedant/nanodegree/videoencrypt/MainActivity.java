package com.iambedant.nanodegree.videoencrypt;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.iambedant.xcript.Encrypter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private static final String TAG = "UsbDevices";

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 11;
    Encrypter encrypter;
    PendingIntent permissionIntent;
    UsbManager manager;


    //connection variables

    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    private Uri finalUri;
    UsbDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            init();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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
                Uri storeUri = Uri.parse("file:///storage/emulated/0/DCIM/Camera/enc_v.swf");
                File file = new File(outputPath);

                int size = (int) file.length();
                byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    Log.d(TAG, "buff");
                    startTransfer(bytes);


                    if (file.exists()) {
                        file.delete();
                    }
                    encrypter.decryptFile(storeUri, outputPath, key);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


            }
        });

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        device = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);


//        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//        Iterator it = deviceList.entrySet().iterator();
//        while(it.hasNext()){
//            Map.Entry pair = (Map.Entry)it.next();
//            Log.d("UsbDevices", pair.getKey() + " = " + pair.getValue());
//        }

        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
    }

    private void startTransfer(final byte[] bytes) {
        if (device != null) {
            Log.d(TAG, "device:" + device.getDeviceName());

            if (manager.hasPermission(device)) {
                Log.d(TAG, "permisiion:" + true);

                UsbInterface intf = device.getInterface(0);
                final UsbEndpoint endpoint = intf.getEndpoint(0);

                final UsbDeviceConnection connection = manager.openDevice(device);
                connection.claimInterface(intf, forceClaim);

                final UsbRequest usbRequest = new UsbRequest();

                usbRequest.initialize(connection, endpoint);
                final ByteBuffer buf = ByteBuffer.wrap(bytes);
                usbRequest.queue(buf, bytes.length);
                usbRequest.setClientData(this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "bulkTransfer has Started");

                        connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
                        Log.d(TAG, "bulkTransfer has ended" + bytes.length);
                    }
                }, 0);
            }
        }
    }

    private void init() throws NoSuchAlgorithmException {
        encrypter = new Encrypter();
    }


    public void performFileSearch() {

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        );
        startActivityForResult(i, REQUEST_TAKE_GALLERY_VIDEO);
    }

    String key;
    String outputPath;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {

                Uri selectedUri = data.getData();
                Uri storeUri = Uri.parse("file:///storage/emulated/0/DCIM/Camera/enc_v.swf");
                outputPath = mf_szGetRealPathFromURI(selectedUri);
                key = encrypter.encryptFile(outputPath, storeUri);
                Log.d(TAG, "encrypted");
            }
        }
    }

    public String mf_szGetRealPathFromURI(final Uri ac_Uri) {
        String result = "";
        boolean isok = false;

        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = getContentResolver().query(ac_Uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
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

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(TAG, "we have the permission " + device);
                            //call method to set up device communication
                            manager.requestPermission(device, permissionIntent);
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };
}
