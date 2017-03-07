package com.andrew.matiz.matiz;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static Config.Config.COMMAND_ADD;
import static Config.Config.COMMAND_START_15;
import static Config.Config.COMMAND_STOP;
import static Config.Config.COMMAND_PARAM;
import static Config.Config.COMMAND_START_10;
import static Config.Config.COMMAND_START_20;
import static Config.Config.PHONE_NUMBER;

public class MainActivity extends AppCompatActivity {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    String SENT_SMS = "SENT_SMS";
    String DELIVER_SMS = "DELIVER_SMS";
    Intent sent_intent=new Intent(SENT_SMS);
    Intent deliver_intent=new Intent(DELIVER_SMS);
    PendingIntent sent_pi,deliver_pi;

    int current_state=0;
    String txtMessage = "";

    Button btParam,btStart10,btStart15,btStart20;
    String phone,message,btParamText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        InitLayout();
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS,}, 0);
        sent_pi = PendingIntent.getBroadcast(MainActivity.this,0,sent_intent,0);
        deliver_pi = PendingIntent.getBroadcast(MainActivity.this,0,deliver_intent,0);
        CheckButtonClick();
    }
    private void CheckButtonClick(){
        btParam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendCommand(COMMAND_PARAM);
            }
        });
        btStart10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_state==0){
                    SendCommand(COMMAND_START_10);
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
        btStart20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_state==0){
                    SendCommand(COMMAND_START_20);
                }
                if (current_state==1){
                    SendCommand(COMMAND_STOP);
                }
            }
        });
    }
    public void SendCommand(String command){
        //Toast.makeText(this,PHONE_NUMBER +" "+ command,Toast.LENGTH_LONG).show();
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(PHONE_NUMBER, null, command, sent_pi, deliver_pi);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void InitLayout(){
        btParam=(Button) findViewById(R.id.btParam);
        btStart10=(Button) findViewById(R.id.btStart10);
        btStart15=(Button) findViewById(R.id.btStart15);
        btStart20=(Button) findViewById(R.id.btStart20);
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(sentReceiver, new IntentFilter(SENT_SMS));
        registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS));
        IntentFilter intFilt = new IntentFilter(SMS_RECEIVED);
        intFilt.setPriority(100);
        registerReceiver(receiverSMS, intFilt);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliverReceiver);
        unregisterReceiver(receiverSMS);
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context,"Sented",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context,"Error sent",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context,"Delivered",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context,"Error deliver",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    BroadcastReceiver receiverSMS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Toast.makeText(context, "SMS", Toast.LENGTH_SHORT).show();


            //if (intent.getAction().equals(SMS_RECEIVED)){
            abortBroadcast();

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length != 0) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pdus.length; i++) {
                        String format = bundle.getString("format");
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        sb.append(messages[i].getMessageBody());
                    }
                    String sender = messages[0].getOriginatingAddress();
                    txtMessage = sb.toString();
                }
                process_receive_sms();
                //Toast.makeText(context, txtMessage, Toast.LENGTH_SHORT).show();
            }
            // }
        }
    };

    private void process_receive_sms(){
        String buffer="";
        int numberSymbol1=0;
        int numberSymbol2=0;
        int lenghtStr=txtMessage.length();
        boolean isContain=txtMessage.contains(";");
        if (isContain){
            numberSymbol1=txtMessage.indexOf(";");
            buffer=txtMessage.substring(0,numberSymbol1);
            if(buffer.equals("add")){
                btStart10.setText("Двигатель работает, добавлено 5 минут");
                btStart15.setText("Добавить 5 минут");
                btStart20.setText("Остановить");
                current_state=1;
            }
            if(buffer.equals("stop")){
                btStart10.setText("Запустить двигатель на 10 мин");
                btStart15.setText("15 мин");
                btStart20.setText("20 мин");
                current_state=0;
            }
//            if (buffer.equals("prm")){
//                numberSymbol2=txtMessage.indexOf(";",numberSymbol1+1);
//
//            }
            if(buffer.equals("ok")){
                btStart10.setText("Двигатель запущен на 10 минут");
                btStart15.setText("Добавить 5 минут");
                btStart20.setText("Остановить");
                current_state=1;
            }
            if(buffer.equals("er01")){
                btStart10.setText("Двигатель не запустился");
                btStart15.setText("15 мин");
                btStart20.setText("20 мин");
                current_state=0;
            }
            numberSymbol2=txtMessage.indexOf(";",numberSymbol1+1);
            if (numberSymbol2==-1) return;
            btParamText=txtMessage.substring(numberSymbol1+1,numberSymbol2);
            buffer=txtMessage.substring(numberSymbol2+1,lenghtStr);
            btParamText=btParamText+ "                        " + buffer;
            btParam.setText(btParamText);
        }
    }
}
