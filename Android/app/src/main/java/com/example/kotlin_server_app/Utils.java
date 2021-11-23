package com.example.kotlin_server_app;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

final class Utils {
    static String toHexString(final byte[] buf) {
        return toHexString(buf, 0, buf.length);
    }

    static String toHexString(final byte[] buf, int begin, int end) {
        StringBuilder sb = new StringBuilder(3 * (end - begin));
        toHexString(sb, buf, begin, end);
        return sb.toString();
    }

    static void toHexString(StringBuilder sb, final byte[] buf, int begin, int end) {
        for (int pos = begin; pos < end; pos++) {
            if (sb.length() > 0)
                sb.append(' ');
            int c;

            c = (buf[pos] & 0xff) / 16;
            if (c >= 10) c += 'A' - 10;
            else c += '0';
            sb.append((char) c);
            c = (buf[pos] & 0xff) % 16;
            if (c >= 10) c += 'A' - 10;
            else c += '0';
            sb.append((char) c);
        }
    }

    static float hexToVoltage(String s) {
        String data = s.substring(3, 5) + s.substring(6, 8);
        int dec = Integer.parseInt(data, 16);
        return (-10 + (float)dec / 65535 * 20);
    }

    static void addEntry(LineChart chart, float float_data) {
        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), float_data), 0);
            data.notifyDataChanged();

            // 하기 외 다른 설정들은 문서 참고
            chart.notifyDataSetChanged();
            chart.moveViewToX(data.getEntryCount());
            chart.setVisibleXRangeMaximum(300);

            chart.setDrawGridBackground(false);
        }
    }

    private static ILineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, null);

        // 선 색상 설정
        set.setColor(Color.rgb(198, 63, 63));

        set.setDrawValues(false);
        set.setDrawCircles(false);

        return set;
    }

    static void writeAllData(String filePath, String fileName, ArrayList<Float> dataList) {
        File file = new File(filePath + "/" + fileName);

        File dir = new File(filePath);
        if (!dir.exists()) dir.mkdir();

        try (FileWriter fw = new FileWriter(file, false);
             CSVWriter cw = new CSVWriter(fw)) {
            for (Float voltage : dataList) {
                cw.writeNext(new String[]{String.valueOf(voltage)});
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }
}
