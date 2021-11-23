package com.example.kotlin_server_app;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;

// Runnable 은 Multi Thread 를 구현하기 위해 run() 만을 가진 interface
public class SerialSocket implements Runnable {
    // SerialPortServiceClass_UUID
    private static final UUID BLUETOOTH_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private boolean connected; // 연결 상태 체크
    private BluetoothSocket socket; // 데이터 송수신을 위한 소켓
    private SerialListener listener; // 이벤트 잡기 위함 (?)

    private final BluetoothDevice device;

    // Context 는 시스템의 정보에 접근하거나, 시스템에서 제공하는 API 를 호출 할 수 있도록 함.
    private final Context context;

    // 종료신호를 대기하고 있는 Receiver
    // 그래서 연결할 때 register, 끊을 때 unregister.
    private final BroadcastReceiver disconnectBroadcastReceiver;

    // 생성자 (?)
    SerialSocket(Context context, BluetoothDevice device) {
        if (context instanceof Activity) // instanceof = 객체타입을 확인하는데 사용.
            throw new InvalidParameterException("expected non UI context");
        this.context = context;
        this.device = device;
        disconnectBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null)
                    listener.onSerialIoError(new IOException("background disconnect"));
                // ** 조금 바꿔도 될 것 같기도 **
                disconnect(); // 연결을 당장 끊지않으면, 다시 연결될 때까지 대기함.
            }
        };
    }

    String getName() {
        // null 이면 address return
        return device.getName() != null ? device.getName() : device.getAddress();
    }

    void connect(SerialListener listener) throws IOException {
        this.listener = listener;
        context.registerReceiver(disconnectBroadcastReceiver, new IntentFilter(Constants.INTENT_ACTION_DISCONNECT));

        // Thread 사용
        Executors.newSingleThreadExecutor().submit(this);
    }

    void disconnect() {
        listener = null; // 리스너를 없애서 남은 데이터와 오류를 무시해버림.
        // connected = false; // run() 에서 어처피 재정의 해서 주석처리한 듯.

        // 소켓 종료
        if(socket != null) {
            try {
                socket.close();
            } catch (Exception ignored){}
            socket = null;
        }
        try {
            context.unregisterReceiver(disconnectBroadcastReceiver);
        } catch (Exception ignored) {}
    }

    // connect & read
    @Override
    public void run() {
        // 연결을 시도하고 되면 connected = true로, 안되면 종료함.
        // 연결 여부를 listener 에게 전달.
        try {
            socket = device.createRfcommSocketToServiceRecord(BLUETOOTH_SPP);
            socket.connect();
            if (listener != null)
                listener.onSerialConnect();
        } catch (Exception e) {
            if (listener != null)
                listener.onSerialConnectError(e); // 에러 전달
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
            return;
        }
        connected = true;

        // 계속 읽어서 buffer에 저장함.
        try {
            byte[] buffer = new byte[1024];
            int len;

            // ** 연결을 끊는 event 추가하거나, 특정 시간동안 하는 것으로 바꿔야 할 듯. **
            while (true) {
                len = socket.getInputStream().read(buffer);
                byte[] data = Arrays.copyOf(buffer, len);
                if (listener != null){
                    listener.onSerialRead(data);
                }
            }
        } catch (Exception e) {
            connected = false;
            if (listener != null)
                // 연결은 됐으니까 IO Error 발생한 것
                listener.onSerialIoError(e);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
            socket = null;
        }
    }
}
