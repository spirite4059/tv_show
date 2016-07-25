package com.retrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.retrofit.download.DownloadStatusListener;
import com.retrofit.download.RetrofitDLUtils;

public class MainActivity extends AppCompatActivity {

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrofitDLUtils.getInstance().download(MainActivity.this, "", "", "", new DownloadStatusListener() {
                    @Override
                    public void onProgress(long progress, long total, boolean done) {

                    }

                    @Override
                    public void onError(String msg) {

                    }
                });
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        RetrofitDLUtils.getInstance().cancel();
    }
}
