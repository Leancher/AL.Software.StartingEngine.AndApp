package com.andrew.matiz.matiz;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static Config.Config.COMMAND_ADD;
import static Config.Config.COMMAND_START_10;
import static Config.Config.COMMAND_START_15;
import static Config.Config.COMMAND_STOP;
import static Config.Config.COMMAND_UPDATE;
import static Config.Config.COMMAND_AUTO_START_OM;
import static Config.Config.COMMAND_AUTO_START_OFF;
import static Config.Config.REQUEST_ACTIVITY_INFO;
import static Config.Config.REQUEST_ACTIVITY_SETTINGS;
import static com.andrew.matiz.matiz.R.drawable.bt_auto_temp_off;
import static com.andrew.matiz.matiz.R.drawable.bt_auto_temp_on;
import static com.andrew.matiz.matiz.R.drawable.bt_empty;
import static com.andrew.matiz.matiz.R.drawable.bt_empty_wide;
import static com.andrew.matiz.matiz.R.drawable.bt_start_10;
import static com.andrew.matiz.matiz.R.drawable.bt_start_15;

public class ActivityMain extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    public final static String DATA_TO_ACTIVITY = "result";
    public final static String COMMAND_FROM_ACTIVITY = "data";

    public final static String BROADCAST_ACTION = "ServiceCoreBroadcast";

    int current_state=0;
    int autostart_state=0;

    Button btUpdate,btStart10,btStart15,btSet,btInfo,btStartAuto;
    TextView txtBat,txtInside,txtEngine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        //Усчтанавливаем ориентацию экрана
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        InitLayout();
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS,}, 0);

        CheckButtonClick();
    }
    private void CheckButtonClick(){
        try {
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendCommand(COMMAND_UPDATE);
            }
        });
        btStart10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (current_state==0){
                   SendCommand(COMMAND_START_10);
               }
               if (current_state==1){
                   SendCommand(COMMAND_STOP);
               }
            }
        });

        btStart15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_state==0){
                    SendCommand(COMMAND_START_15);
                }
                if (current_state==1){
                    SendCommand(COMMAND_ADD);
                }
            }
        });
        btStartAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autostart_state==0) SendCommand(COMMAND_AUTO_START_OM);
                if (autostart_state==1) SendCommand(COMMAND_AUTO_START_OFF);

                if (autostart_state==0){
                    btStartAuto.setBackgroundResource(bt_auto_temp_on);
                    autostart_state=1;
                }
                else {
                    btStartAuto.setBackgroundResource(bt_auto_temp_off);
                    autostart_state=0;
                }
            }
        });

        btInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(ActivityMain.this, ActivityInform.class);
                startActivityForResult(intent, REQUEST_ACTIVITY_INFO);
            }
        });

        btSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(ActivityMain.this,ActivitySettings.class);
                startActivityForResult(intent, REQUEST_ACTIVITY_SETTINGS);
            }
        });

        }catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
            Toast.makeText(ActivityMain.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
/*    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK){

        }
    }*/

    public void SendCommand(String command){
        Intent intent;
        // Создаем Intent для вызова сервиса,
        // кладем туда параметр времени и код задачи
        intent = new Intent(this, ServiceCore.class).putExtra(COMMAND_FROM_ACTIVITY, command);
        // стартуем сервис
        startService(intent);
    }
    private void InitLayout(){
        txtBat=(TextView) findViewById(R.id.textBat);
        txtEngine=(TextView) findViewById(R.id.textEngine);
        txtInside=(TextView) findViewById(R.id.textInside);
        btUpdate =(Button) findViewById(R.id.btUpdate);
        btStart10=(Button) findViewById(R.id.btStart10);
        btStart15=(Button) findViewById(R.id.btStart15);
        btStartAuto=(Button) findViewById(R.id.btStartAuto);
        btSet=(Button) findViewById(R.id.btSet);
        btInfo=(Button) findViewById(R.id.btInfo);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(broadcastService, new IntentFilter(BROADCAST_ACTION));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastService);
    }
    //Создаем Broadcast для свзяи с сервисом
    BroadcastReceiver broadcastService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Извдекаем данные из Интент по клюсевому слову
            String dataFromService = intent.getStringExtra(DATA_TO_ACTIVITY);
            Log.d(LOG_TAG, "onReceive: " + dataFromService);
            processResultData(dataFromService);
        }
    };
    private void processResultData(String txtMessage){
        boolean isMatiz=false;
        String buffer="";
        int numberSymbol1=0;
        int numberSymbol2=0;
        int lenghtStr=txtMessage.length();
        boolean isContain=txtMessage.contains(";");
        if (isContain){
            numberSymbol1=txtMessage.indexOf(";");
            buffer=txtMessage.substring(0,numberSymbol1);
            if(buffer.equals("add")){
                btStart10.setText("Двигатель работает, добавлено 5 минут \n\n Остановить");
                btStart15.setText("Добавить 5 минут");
                current_state=1;
                isMatiz=true;
            }
            if(buffer.equals("stop")){
                btStart10.setBackgroundResource(bt_start_10);
                btStart10.setText("");
                btStart15.setBackgroundResource(bt_start_15);
                btStart15.setText("");
                current_state=0;
                isMatiz=true;
            }
            if (buffer.equals("prm")){
                isMatiz=true;
            }
            if(buffer.equals("ok")){
                btStart10.setBackgroundResource(bt_empty_wide);
                btStart10.setText("Двигатель запущен на 10 минут \n\n Остановить");
                btStart15.setBackgroundResource(bt_empty);
                btStart15.setText("Добавить 5 минут");
                current_state=1;
                isMatiz=true;
            }
            if(buffer.equals("er01")){
                btStart10.setText("Двигатель не запустился");
                btStart15.setText("15 мин");
                current_state=0;
                isMatiz=true;
            }
            if(buffer.equals("er02")){
                Toast.makeText(this,"Двигатель уже запущен",Toast.LENGTH_LONG).show();
                btStart10.setText("Двигатель работает");
                btStart15.setText("Добавить 5 минут");
                current_state=1;
                isMatiz=true;
            }
            if (isMatiz==false) return;

            numberSymbol2=txtMessage.indexOf(";",numberSymbol1+1);
            if (numberSymbol2==-1) return;

            txtBat.setText(txtMessage.substring(numberSymbol1+1,numberSymbol2));

            numberSymbol1=txtMessage.indexOf(";",numberSymbol2+1);
            buffer=txtMessage.substring(numberSymbol2+1,numberSymbol1);
            txtInside.setText(buffer);

            numberSymbol2=txtMessage.indexOf(";",numberSymbol1+1);
            buffer=txtMessage.substring(numberSymbol1+1,numberSymbol2);

            txtEngine.setText(buffer);
        }
    }
}
