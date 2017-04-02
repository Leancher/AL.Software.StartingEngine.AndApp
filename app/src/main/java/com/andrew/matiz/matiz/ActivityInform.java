package com.andrew.matiz.matiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import static Config.Config.INFORM;

public class ActivityInform extends AppCompatActivity{
    Button btOK;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_info);
        btOK=(Button)findViewById(R.id.btOK);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent();
                intent.putExtra(INFORM, "info");
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

}
