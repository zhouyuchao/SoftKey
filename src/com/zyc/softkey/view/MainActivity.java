package com.zyc.softkey.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.zyc.softkey.R;
import com.zyc.softkey.SoftKeyService;

public class MainActivity extends Activity {
    private EditText posX;
    private EditText posY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button startBtn = (Button) findViewById(R.id.startWin);
        startBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startWinService();
            }
        });
        
        Button stopBtn = (Button) findViewById(R.id.stopWin);
        stopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWinService();
            }
        });
        
        posX = (EditText) findViewById(R.id.posX);
        posY = (EditText) findViewById(R.id.posY);
    }

    private void startWinService(){
        Intent intent = new Intent();
        intent.setClass(this, SoftKeyService.class);
        intent.putExtra("posX", posX.getEditableText().toString());
        intent.putExtra("posY", posY.getEditableText().toString());
        startService(intent);
    }

    private void stopWinService(){
        Intent intent = new Intent(this, SoftKeyService.class);
        stopService(intent);
    }
}
