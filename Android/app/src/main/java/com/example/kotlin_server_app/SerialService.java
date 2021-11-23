package com.example.kotlin_server_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class SerialService extends Service implements SerialListener {
    // Binder에서 Service를 찾을 때 사용.
    class SerialBinder extends Binder {
        SerialService getService() { return SerialService.this; }
    }

    // enum = 열거형 상수
    private enum QueueType {Connect, ConnectError, Read, IoError}

    private static class QueueItem {
        QueueType type;
        byte[] data;
        Exception e;

        // 생성자
        QueueItem(QueueType type, byte[] data, Exception e) {
            this.type = type;
            this.data = data;
            this.e = e;
        }
    }

    // 연결 -> (둘 사이의 interface)
    // Worker thread - Main thread 연결해주는 것
    private final Handler mainLooper;

    // Service - Client 를 연결해주는 것
    private final IBinder binder;

    private final Queue<QueueItem> queue1, queue2;

    private SerialSocket socket;
    private SerialListener listener;
    private boolean connected;

    // Lifecycle
    // 생성자
    public SerialService() {
        // Main thread 의 Looper
        // * UI 작업은 반드시 Main.
        mainLooper = new Handler(Looper.getMainLooper());
        binder = new SerialBinder();

        // q1 은 main Looper에서 post했을 때
        queue1 = new LinkedList<>();
        // q2 는 그 전에 listener가 없을 때 넣는 듯 ?
        queue2 = new LinkedList<>();
    }

    @Override
    public void onDestroy(){
        cancelNotification();
        disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Api
    public void connect(SerialSocket socket) throws IOException {
        socket.connect(this);
        this.socket = socket;
        connected = true;
    }

    public void disconnect() {
        connected = false;
        cancelNotification();
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    // Main thread와 연결하는 함수
    public void attach(SerialListener listener) {
        if(Looper.getMainLooper().getThread() != Thread.currentThread())
            throw new IllegalArgumentException("not in main thread");
        cancelNotification();

        // use synchronized() to prevent new items in queue2
        // new items will not be added to queue1 because mainLooper.post and attach() run in main thread

        // Thread 동기화
        synchronized (this) {
            this.listener = listener;
        }

        // 동기화 후 main에 값을 다 넘겨주는 듯.
        // for 하나로 해결 못 하는지? 순서가 중요한지?
        for(QueueItem item : queue1) { // Queue 데이터 + 동작이 들어감.
            switch(item.type) {
                case Connect:
                    listener.onSerialConnect();
                    break;
                case ConnectError:
                    listener.onSerialConnectError(item.e);
                    break;
                case Read:
                    listener.onSerialRead(item.data); // 데이터 ?
                    break;
                case IoError:
                    listener.onSerialIoError(item.e);
                    break;
            }
        }

        for(QueueItem item : queue2) {
            switch(item.type) {
                case Connect:
                    listener.onSerialConnect();
                    break;
                case ConnectError:
                    listener.onSerialConnectError(item.e);
                    break;
                case Read:
                    listener.onSerialRead(item.data);
                    break;
                case IoError:
                    listener.onSerialIoError(item.e);
                    break;
            }
        }

        // 여기서 쓰고 버리지 말고 어느 공간에 저장하자?..
        // or 측정 시작 누르면 비우고 다시 시작하도록 하
        queue1.clear();
        queue2.clear();
    }

    public void detach() {
        listener = null;
    }

    private void cancelNotification() {
        stopForeground(true);
    }

    // SerialListener
    @Override
    public void onSerialConnect() {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialConnect();
                        } else {
                            queue1.add(new QueueItem(QueueType.Connect, null, null));
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.Connect, null, null));
                }
            }
        }
    }

    @Override
    public void onSerialConnectError(Exception e) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialConnectError(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.ConnectError, null, e));
                            cancelNotification();;
                            disconnect();
                        }
                    });
                }
            }
        }
    }

    // 이 것이 실행되면 TerminalFragment.receivce가 실행되서 데이터가 받아짐.
    @Override
    public void onSerialRead(byte[] data) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialRead(data);
                        } else {
                            queue1.add(new QueueItem(QueueType.Read, data, null));
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.Read, data, null));
                }
            }
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        if(connected) {
            synchronized (this) {
                if (listener != null) {
                    mainLooper.post(() -> {
                        if (listener != null) {
                            listener.onSerialIoError(e);
                        } else {
                            queue1.add(new QueueItem(QueueType.IoError, null, e));
                            cancelNotification();
                            disconnect();
                        }
                    });
                } else {
                    queue2.add(new QueueItem(QueueType.IoError, null, e));
                    cancelNotification();
                    disconnect();
                }
            }
        }
    }
}
