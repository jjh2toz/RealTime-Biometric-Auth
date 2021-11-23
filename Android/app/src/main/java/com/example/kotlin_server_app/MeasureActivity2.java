package com.example.kotlin_server_app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.example.kotlin_server_app.Utils.addEntry;
import static com.example.kotlin_server_app.Utils.hexToVoltage;
import static com.example.kotlin_server_app.Utils.writeAllData;

// Fragment 쪽 코드
public class MeasureActivity2 extends AppCompatActivity implements ServiceConnection, SerialListener {
    private ArrayList<Float> VoltageList = new ArrayList<Float>();

    private enum Connected { False, Pending, True }

    private SerialService service;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean receive_flag = false;

    private final Pattern pattern = Pattern.compile("AA\\s..\\s..\\sAA");
    static String receivedData;
    private LineChart chart;
    private LineData line_data;

    String filePath;

    AlertDialog waitingDialog;

    int time; // API 결과에 따라 10초
    int start_time;

    Timer timer, start_timer;
    TimerTask t, start_t;

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://223.194.46.83:25900")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_measure);

        time = 630;
        start_time = 4;

        // Dialog
        waitingDialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setMessage("블루투스 연결 중")
                .setContext(this)
                .build();

        filePath = this.getApplicationContext().getFilesDir() + "/";

        waitingDialog.show();
        this.bindService(new Intent(this, SerialService.class), this, Context.BIND_AUTO_CREATE);

        // chart
        chart = findViewById(R.id.chart);
        line_data = new LineData();
        chart.setBackgroundColor(Color.rgb(250,250,250));
        chart.setDescription(null);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getLegend().setEnabled(false);   // Hide the legend
        YAxis lyl = chart.getAxisLeft();

        // 보여질 위치
        lyl.setAxisMaximum(4f);
        lyl.setAxisMinimum(-4f);

        chart.setData(line_data);

        // timer
        TextView timeText = (TextView) findViewById(R.id.timer);
        TextView startTimeText = (TextView) findViewById(R.id.start_timer);

        start_timer = new Timer();
        start_t = new TimerTask() {
            @Override
            public void run() {
                start_time--;
                MeasureActivity2.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (start_time == 0) startTimeText.setText("");
                        else startTimeText.setText(String.valueOf(start_time));
                    }
                });
                if (start_time == 0) {
                    receive_flag = true;
                    receivedData = "";
                    start_timer.cancel();
                    timer.schedule(t, 0, 1000);
                }
            }
        };

        timer = new Timer();
        t = new TimerTask() {
            @Override
            public void run(){
                time--;

                if (time < 60) {
                    String timeString = String.valueOf(time) + "sec";
                    MeasureActivity2.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeText.setText(timeString);
                        }
                    });
                    if (time == 0) {
                        receive_flag = false;
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String getTime = dateFormat.format(date);
                        String fileName = "ECG_" + getTime + ".csv";
                        //String fileName = "ECG.csv";
                        writeAllData(filePath, fileName, VoltageList);

                        // 소켓 연결 종료 - 종료할지 연결 유지할지 고민.
                        disconnect();
                        Intent intent = new Intent(getBaseContext(), Filepass_newuser.class); // 인증 페이지로 이동
                        startActivity(intent);


                        timer.cancel();
                    }
                }
                else {
                    int min = time / 60;
                    int sec = time % 60;
                    String timeString = String.valueOf(min) + "min " + sec +"sec";
                    MeasureActivity2.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeText.setText(timeString);
                        }
                    });
                }
            }
        };

    }

    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(getIntent().getStringExtra("address"));
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(this.getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

//    public void OnClickBtnStart(View view){
//        receive_flag = true;
//        receivedData = "";
//    }
//
//    public void OnClickBtnEnd(View view){ receive_flag = false;
//    }

//    public void OnClickBtnSave(View view){
//        writeAllData(filePath, fileName, VoltageList);
//        Toast.makeText(this.getApplicationContext(), "저장완료", Toast.LENGTH_LONG).show();
//
//        // 소켓 연결 종료 - 종료할지 연결 유지할지 고민.
//        disconnect();
//        Intent intent = new Intent(getBaseContext(), TempActivity.class);
//        startActivity(intent);
//    }

    private void receive(byte[] data){
        if (receive_flag){
            float voltage;
            String hex_data = Utils.toHexString(data);

            receivedData += hex_data;
            receivedData += " ";

            Matcher matcher = pattern.matcher(receivedData);
            while (matcher.find()){
                voltage = hexToVoltage(receivedData.substring(matcher.start(), matcher.end()));
                addEntry(chart, voltage);
                VoltageList.add(voltage);
            }

            int cnt = receivedData.length() / 12;
            receivedData = receivedData.substring(cnt * 12);
        }
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        this.stopService(new Intent(this, SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            this.startService(new Intent(this, SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);

        if(initialStart) {
            initialStart = false;
            this.runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    @Override
    public void onSerialConnect() {
        connected = Connected.True;
        waitingDialog.dismiss();
        start_timer.schedule(start_t, 0, 1000);
    }

    @Override
    public void onSerialConnectError(Exception e) {
        disconnect();
        waitingDialog.dismiss();
        Toast.makeText(this.getApplicationContext(), "블루투스 연결 실패", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        disconnect();
    }
}