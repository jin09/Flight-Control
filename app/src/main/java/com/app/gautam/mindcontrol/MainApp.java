package com.app.gautam.mindcontrol;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
/**
 * Created by gautam on 30-01-2017.
 */
public class MainApp extends AppCompatActivity {
    private static final String TAG = "MAINAPP";
    TextView textView;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference syncref = databaseReference.child("last");
    BluetoothSPP bt;
    TextView textStatus,motorSpeeds;
    EditText etMessage;
    Menu menu;
    int m1=0,m2=0,m3=0,m4=0;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //main code starts here
        bt = new BluetoothSPP(this);
        textStatus = (TextView)findViewById(R.id.textStatus);
        etMessage = (EditText)findViewById(R.id.etMessage);
        motorSpeeds = (TextView)findViewById(R.id.motorSpeeds);
        textView = (TextView) findViewById(R.id.textView3);
        pb = (ProgressBar) findViewById(R.id.progressBar2);

        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connected");
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_connection, menu);
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textStatus.setText("Status : Connected to " + name);
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_disconnection, menu);
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                motorSpeeds.setText(message);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_connection, menu);
        return true;
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
        syncref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                //textView.setText(text);
                int result = Integer.parseInt(text);
                pb.setProgress(result);
                if(result > 70){
                    ThrottleUp();
                }
                if(result < 50){
                    ThrottleDown();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_android_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_ANDROID);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_device_connect) {
            bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
			/*
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();*/
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else if(id == R.id.menu_disconnect) {
            if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
                bt.disconnect();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void ThrottleUp(){
        m1+=5;
        m2+=5;
        m3+=5;
        m4+=5;
        if(m1 > 60){
            m1 = 60;
            m2 = 60;
            m3 = 60;
            m4 = 60;
        }
        bt.send(m1+","+m2+","+m3+","+m4,false);
    }

    public void ThrottleDown(){
        m1-=10;
        m2-=10;
        m3-=10;
        m4-=10;
        if(m1 < 0){
            m1 = 0;
            m2 = 0;
            m3 = 0;
            m4 = 0;
        }
        bt.send(m1+","+m2+","+m3+","+m4,false);
    }

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(etMessage.getText().length() != 0) {
                    String text=etMessage.getText().toString();

                    m1 = Integer.parseInt(text.split(",")[0]);
                    m2 = Integer.parseInt(text.split(",")[1]);
                    m3 = Integer.parseInt(text.split(",")[2]);
                    m4 = Integer.parseInt(text.split(",")[3]);

                    bt.send(etMessage.getText().toString(), false);
                    etMessage.setText("");
                }
            }
        });


        //throttle up
        Button throttleUp = (Button) findViewById(R.id.button);
        throttleUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1+=10;
                m2+=10;
                m3+=10;
                m4+=10;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


        //throttle zero
        Button throttleZero = (Button) findViewById(R.id.button2);
        throttleZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=0;
                m2=0;
                m3=0;
                m4=0;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


/**
        //throttle hundred
        Button throttle100 = (Button) findViewById(R.id.t100);
        throttle100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=100;
                m2=100;
                m3=100;
                m4=100;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });

        //roll exp
        Button rollexp = (Button) findViewById(R.id.rexp);
        rollexp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=80;
                m2=80;
                m3=110;
                m4=110;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


        //pitch exp
        Button pitchexp = (Button) findViewById(R.id.pexp);
        pitchexp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=80; //c
                m2=110; //b
                m3=110; //a
                m4=80; //d
                Toast.makeText(getApplicationContext(),"pitch but",Toast.LENGTH_SHORT).show();
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


        //yawn exp
        Button yawnexp = (Button) findViewById(R.id.yexp);
        yawnexp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=80; //c
                m2=120; //b
                m3=80; //a
                m4=120; //d
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });

*/
        //throttle down
        Button throttleDown = (Button) findViewById(R.id.button3);
        throttleDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1-=10;
                m2-=10;
                m3-=10;
                m4-=10;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


        //calibrate
        Button calibrate = (Button) findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=0;
                m2=0;
                m3=0;
                m4=0;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });

        //stop
        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m1=0;
                m2=0;
                m3=0;
                m4=0;
                bt.send(m1+","+m2+","+m3+","+m4,false);
            }
        });


    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

}
