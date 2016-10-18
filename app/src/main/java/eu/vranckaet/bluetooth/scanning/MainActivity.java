package eu.vranckaet.bluetooth.scanning;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver mReceiver; // For normal bluetooth scanning
    private BluetoothAdapter bluetoothAdapter; // For BLE scanning
    private BluetoothAdapter.LeScanCallback leScanCallback; // For BLE scanning

    private boolean bluetoothReceiverRegistered;
    private boolean scanning = false;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        content = (LinearLayout) findViewById(R.id.content);
        content.removeAllViews();

        startAllBluetoothScanning();
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return bluetoothAdapter;
    }

    private void onBluetoothDeviceDiscovered(BluetoothDevice device) {
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        View view1 = content.findViewWithTag(deviceAddress + "_0");
        View view2 = content.findViewWithTag(deviceAddress + "_1");
        if (view1 != null) {
            content.removeView(view1);
        }
        if (view2 != null) {
            content.removeView(view2);
        }

        TextView nameTextView = new TextView(MainActivity.this);
        nameTextView.setText("Name : " + deviceName);
        nameTextView.setTag(deviceAddress + "_0");
        content.addView(nameTextView, 0);

        TextView addressTextView = new TextView(MainActivity.this);
        addressTextView.setText("Addresss : " + deviceAddress);
        addressTextView.setTag(deviceAddress + "_1");
        content.addView(addressTextView, 0);

        Log.d("dirk", "Bluetooth device: " + deviceAddress + "(" + deviceName + ")");
    }

    @Override
    protected void onPause() {
        stopAllBluetoothScanning();
        super.onPause();
    }

    private void startAllBluetoothScanning() {
        content.removeAllViews();
        startBluetoothScanning();
        startBLEScanning();
        invalidateOptionsMenu();
        scanning = true;
    }

    private void stopAllBluetoothScanning() {
        stopBluetoothScanning();
        stopBLEScanning();
        invalidateOptionsMenu();
        scanning = false;
    }

    private void startBluetoothScanning() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Create a BroadcastReceiver for ACTION_FOUND
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Log.d("dirk", "Bluetooth device discovered...");
                    String action = intent.getAction();
                    // When discovery finds a device
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // Add the name and address to an array adapter to show in a ListView
                        onBluetoothDeviceDiscovered(device);
                    }
                }
            };

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
            bluetoothReceiverRegistered = true;
            Log.d("dirk", "registered the bluetooth receiver...");
        } else {
            Log.d("dirk", "No permission yet for " + Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    private void startBLEScanning() {
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.d("dirk", "Bluetooth LE device discovered...");
                onBluetoothDeviceDiscovered(device);
            }
        };
        getBluetoothAdapter().startLeScan(leScanCallback);
        Log.d("dirk", "BLE Scanning started");
    }

    private void stopBluetoothScanning() {
        if (mReceiver != null && bluetoothReceiverRegistered) {
            try {
                unregisterReceiver(mReceiver);
            } catch (Exception e) {
                // Not interested if this ever fails, at least we did what we have to do!
            }
            bluetoothReceiverRegistered = false;
        }
    }

    private void stopBLEScanning() {
        if (leScanCallback != null) {
            getBluetoothAdapter().stopLeScan(leScanCallback);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(-98); // stop
        menu.removeItem(-99); // start
        MenuItem menuItem;
        if (scanning) {
            menuItem = menu.add(-1, -98, 0, "STOP SCAN");

        } else {
            menuItem = menu.add(-1, -99, 0, "START SCAN");
        }
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == -98) {
            stopAllBluetoothScanning();
            return true;
        } else if (item.getItemId() == -99) {
            startAllBluetoothScanning();
            return true;
        }
        return false;
    }
}
