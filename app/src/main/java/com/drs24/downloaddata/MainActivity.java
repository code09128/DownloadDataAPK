package com.drs24.downloaddata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    DownloadManager DM;
    DownloadManager.Request request;
    private long LatestDownloadID;
    String URL;
    DialogFragmentHelper newFragment;
    static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);

        try {//取得APP目前的versionName
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadNewVersion();
            }
        });
    }

    private void DownloadNewVersion() {
        newFragment = new DialogFragmentHelper();
        newFragment.show(getSupportFragmentManager(), "download apk");
        DM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        URL = "https://www.scmh.org.tw/intranet/ShowCloudFiles/app-debug.apk";
        Uri uri = Uri.parse(URL);
        request = new DownloadManager.Request(uri);
        request.setMimeType("application/vnd.android.package-archive");//設置MIME為Android APK檔
        //Android 6.0以上需要判斷使用者是否願意開啟儲存(WRITE_EXTERNAL_STORAGE)的權限
        CheckStoragePermission();
    }

    private void DownloadManagerEnqueue() {
        //創建目錄
        Environment.getExternalStorageState(new File(Environment.DIRECTORY_DOWNLOADS));
        //設定APK儲存位置
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-debug.apk");

        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver(getApplicationContext());
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));//註冊DOWNLOAD_COMPLETE-BroadcastReceiver

        DownloadObserver downloadObserver = new DownloadObserver(null);
        getContentResolver().registerContentObserver(CONTENT_URI, true, downloadObserver);//註冊ContentObserver

        LatestDownloadID = DM.enqueue(request);

        SharedPreferencesHelper sp = new SharedPreferencesHelper(getApplicationContext());
        sp.setDownloadID(LatestDownloadID);//儲存DownloadID
    }

    private void CheckStoragePermission() {//Android 6.0檢查是否開啟儲存(WRITE_EXTERNAL_STORAGE)的權限，若否，出現詢問視窗
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            //Can add more as per requirement
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            },20);
        } else {
            DownloadManagerEnqueue();
        }
    }

    @Override//Android 6.0以上 接收使用者是否允許使用儲存權限
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 20) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                DownloadManagerEnqueue();
            } else {
                CheckStoragePermission();
            }
        }
    }

    class DownloadObserver extends ContentObserver {

        DownloadObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(LatestDownloadID);
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final Cursor cursor = dm.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                final int totalColumn = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                final int currentColumn = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int totalSize = cursor.getInt(totalColumn);
                int currentSize = cursor.getInt(currentColumn);
                float percent = (float) currentSize / (float) totalSize;
                final int progress = Math.round(percent * 100);
                runOnUiThread(new Runnable() {//確保在UI Thread執行
                    @Override
                    public void run() {
                        newFragment.setProgress(progress);
                    }
                });
            }
        }
    }
}
