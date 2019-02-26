//package com.example.ana.myapplication;
//
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.View;
//import android.widget.TextView;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.UUID;
//
//import static android.os.SystemClock.sleep;
//
//
//public class MainActivity extends Activity {
//    public BluetoothAdapter mBluetoothAdapter;
//    private BluetoothServerSocket mmServerSocket;
//    private static final UUID MY_UUID_SECURE = UUID.fromString("f0937a74-f0e3-11e8-8eb2-f2801f1b9fd1");
//
//    TextView text;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        text=(TextView)findViewById(R.id.textView1);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.button1:
//
//                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                if (mBluetoothAdapter == null) {
//                    text.setText("Does not support bluetooth");
//                    return;
//                }
//
//                Intent discoverableIntent = new
//                        Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
//                changeT("Discoverable!!");
//                while(mmServerSocket==null);
//                AcceptThread();
//                while(mmServerSocket==null);
//                run();
//
//        }
//    }
//
//    public void changeT(String str)
//    {
//        text.setText(str);
//    }
//
//    public void AcceptThread() {
//        BluetoothServerSocket tmp = null;
//        try {
//            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MYYAPP", MY_UUID_SECURE);
//
//        } catch (IOException e) { }
//        mmServerSocket = tmp;
//    }
//
//    public void run() {
//        BluetoothSocket socket = null;
//        while (true) {
//            try {
//                socket = mmServerSocket.accept();
//                changeT("listening");
//            } catch (IOException e) {
//                break;
//            }
//            if (socket != null) {
//                changeT("doneeeee");
//                try {
//                    mmServerSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
//        }
//    }
//}






package com.example.ana.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends Activity {

    private static final int DISCOVERABLE_REQUEST_CODE = 0x1;
    private boolean CONTINUE_READ_WRITE = true;
    TextView text;
    String a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text=(TextView)findViewById(R.id.textView1);
        //Always make sure that Bluetooth server is discoverable during listening...
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, DISCOVERABLE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.util.Log.e("TrackingFlow", "Creating thread to start listening...");
        new Thread(reader).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket != null){
            try{
                is.close();
                os.close();
                socket.close();
            }catch(Exception e){}
            CONTINUE_READ_WRITE = false;
        }
    }

    private BluetoothSocket socket;
    private InputStream is;
    private OutputStreamWriter os;
    private Runnable reader = new Runnable() {
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            UUID uuid = UUID.fromString("f0937a74-f0e3-11e8-8eb2-f2801f1b9fd1");
            try {
                BluetoothServerSocket serverSocket = adapter.listenUsingRfcommWithServiceRecord("BLTServer", uuid);
                android.util.Log.e("TrackingFlow", "Listening...");
                socket = serverSocket.accept();
                android.util.Log.e("TrackingFlow", "Socket accepted...");
                is = socket.getInputStream();
                os = new OutputStreamWriter(socket.getOutputStream());
                new Thread(writter).start();

                int bufferSize = 1024;
                int bytesRead = -1;
                byte[] buffer = new byte[bufferSize];
                //Keep reading the messages while connection is open...
                while(CONTINUE_READ_WRITE){
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = is.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize-1] != 0)){
                            result = result + new String(buffer, 0, bytesRead);
                            bytesRead = is.read(buffer);
                        }
                        result = result + new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }
                    android.util.Log.e("TrackingFlow", "Read: " + sb.toString());
                    //Show message on UIThread
                    JSONObject readerJSON = null;
                    try {
                        readerJSON = new JSONObject(sb.toString());
                        //get only one item from the JSON
                        a = readerJSON.getString("a");
                    } catch (JSONException e) {
                        text.setText(e.getStackTrace().toString());
                    }
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
                            text.setText(a);
                        }
                    });
                }
            } catch (IOException e) {e.printStackTrace();}
        }
    };
        public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            default:
                break;
        }
    }
    private Runnable writter = new Runnable() {

        @Override
        public void run() {
            int index = 0;
            while(CONTINUE_READ_WRITE){
                try {
                    os.write("Message From Server" + (index++) + "\n");
                    os.flush();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
