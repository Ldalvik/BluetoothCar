package root.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class MainActivity extends Activity {

    private int leftSpeed = 250;
    private int rightSpeed = 250;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private Handler h;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SeekBar leftWheel = findViewById(R.id.leftWheel);
        SeekBar rightWheel = findViewById(R.id.rightWheel);
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        sb.append(strIncom);
                        int endOfLineIndex = sb.indexOf("/");
                        if (endOfLineIndex > 0) {
                            String sbprint = sb.substring(0, endOfLineIndex);
                            sb.delete(0, sb.length());
                            toast(sbprint);
                        }
                        break;
                }
            };
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("00:06:66:86:5E:6D");
        try {
            socket = connect(device);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        c = new ConnectedThread(socket);
        c.start();

        leftWheel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                leftSpeed = progress;
                    String cmd = Utils.command(leftSpeed, rightSpeed);
                    c.write(cmd);
            }
            public void onStartTrackingTouch(SeekBar seekBar){}
            public void onStopTrackingTouch(SeekBar seekBar){}
        });

        rightWheel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rightSpeed = progress;
                    String cmd = Utils.command(leftSpeed, rightSpeed);
                    c.write(cmd);
            }
            public void onStartTrackingTouch(SeekBar seekBar){}
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
    }

    private BluetoothSocket connect(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
            return (BluetoothSocket) m.invoke(device, MY_UUID);
        } catch (Exception e) {
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void horn(View v){
            c.write("horn/");
    }

    private void toast(final CharSequence text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    h.obtainMessage(1, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
            }
        }
    }
}
