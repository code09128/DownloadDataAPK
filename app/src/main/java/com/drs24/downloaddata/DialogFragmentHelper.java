package com.drs24.downloaddata;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by dustin on 2020/3/9.
 */

public class DialogFragmentHelper extends DialogFragment {
    private ProgressBar progressBar;//下載新版APP時ProgressBar
    private TextView txtProgress;//下載新版APP時顯示文字百分比

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//取消Dialog title列
        getDialog().setCanceledOnTouchOutside(false);//不能點擊Dialog以外區域
        View v = inflater.inflate(R.layout.download_apk_dialog, container, false);
        progressBar = (ProgressBar) v.findViewById(R.id.download_progressBar);
        txtProgress = (TextView) v.findViewById(R.id.txtProgress);
        txtProgress.setText("0%");
        return v;
    }

    @SuppressLint("SetTextI18n")
    void setProgress(int progress){//更新進度條
        progressBar.setProgress(progress);
        txtProgress.setText(progress + "%");
    }
}
