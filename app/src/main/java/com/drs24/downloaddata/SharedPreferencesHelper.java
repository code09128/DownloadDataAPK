package com.drs24.downloaddata;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dustin on 2020/3/9.
 */

class SharedPreferencesHelper {
    private final String DownloadID = "LoginID";
    private SharedPreferences settings;
    private SharedPreferences.Editor PE;

    @SuppressLint("CommitPrefEdits")
    SharedPreferencesHelper(Context context) {
        String SP_Name = "AutomaticallyOpenApk_SP";
        settings = context.getSharedPreferences(SP_Name, 0);
        PE = settings.edit();
    }

    void setDownloadID(long id) {//儲存DownloadID
        PE.putLong(DownloadID,id);
        PE.commit();
    }

    long getDownloadID() {
        return settings.getLong(DownloadID, -1);
    }//取得DownloadID
}
