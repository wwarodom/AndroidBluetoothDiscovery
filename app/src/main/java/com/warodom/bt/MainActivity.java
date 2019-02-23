package com.warodom.bt;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btnDiscovery ;
    Button btnStop;
    Button btnStatus;
    Button btnExit;
    TextView  txtView;
    BluetoothAdapter mBtAdapter;
    boolean stop =  false;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                System.out.println("Device name: " + deviceName);
                System.out.println("Device MAC Addr: " + deviceHardwareAddress);
                txtView.setText(txtView.getText() + "\n" + deviceName + " : " + deviceHardwareAddress + "  RSSI: " + rssi);
//                Toast.makeText(getApplicationContext(),
//                        deviceName + " : " + deviceHardwareAddress + "  RSSI: " + rssi , Toast.LENGTH_SHORT).show();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // When discovery is finished , start discovery again
                if (!stop) {
                    mBtAdapter.startDiscovery();
                    Toast.makeText(getApplicationContext(), "Restart discovery" , Toast.LENGTH_SHORT).show();
                }
                else {
                    mBtAdapter.cancelDiscovery();
                    Toast.makeText(getApplicationContext(), "Stop discovery" , Toast.LENGTH_SHORT).show();
                }

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDiscovery = findViewById(R.id.btnDiscovery);
        btnStatus = findViewById(R.id.btnStatus);
        btnStop = findViewById(R.id.btnStop);
        btnExit = findViewById(R.id.btnExit);
        txtView = findViewById(R.id.txtView);

        txtView.setMovementMethod(new ScrollingMovementMethod());

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this,
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                1);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }


        btnDiscovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 mBtAdapter.startDiscovery();

                // Register for broadcasts when a device is discovered.
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

                registerReceiver(receiver, filter);
                mBtAdapter.startDiscovery();
                stop = false;

//                Intent discoverableIntent =
//                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//                startActivity(discoverableIntent);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBtAdapter.isDiscovering())
                    mBtAdapter.cancelDiscovery();
                Toast.makeText(getApplicationContext(),"Stop discovery", Toast.LENGTH_SHORT).show();
                txtView.setText("");
                stop = true;
            }
        });

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBtAdapter.isDiscovering())
                    Toast.makeText(getApplicationContext(),"Discovering...", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),"Stop discovery...", Toast.LENGTH_SHORT).show();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }
}
