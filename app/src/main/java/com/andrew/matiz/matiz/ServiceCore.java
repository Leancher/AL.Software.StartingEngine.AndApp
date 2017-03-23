package com.andrew.matiz.matiz;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import static Config.Config.PHONE_NUMBER;

public class ServiceCore extends Service {

    final String LOG_TAG = "myLogs";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    String SENT_SMS = "SENT_SMS";
    String DELIVER_SMS = "DELIVER_SMS";
    Intent sent_intent=new Intent(SENT_SMS);
    Intent deliver_intent=new Intent(DELIVER_SMS);
    PendingIntent sent_pi,deliver_pi;

    String txtMessage,txtMessageTemp1;
    public void onCreate() {
        super.onCreate();
        //Регистриуем Broadcast для приема СМС
        IntentFilter intFilt = new IntentFilter(SMS_RECEIVED);
        intFilt.setPriority(100);
        registerReceiver(receiverSMS, intFilt);

        //Регистрируем Broadcast для слежения за отправкой и доставкой СМС
        sent_pi = PendingIntent.getBroadcast(ServiceCore.this,0,sent_intent,0);
        deliver_pi = PendingIntent.getBroadcast(ServiceCore.this,0,deliver_intent,0);

        registerReceiver(sentReceiver, new IntentFilter(SENT_SMS));
        registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS));
        Log.d(LOG_TAG, "MyService onCreate");

    }

    public void onDestroy() {
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver receiverSMS = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "SMS");
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
                    Log.d(LOG_TAG, txtMessage);
                    txtMessageTemp1=txtMessage;
                }
                try {
                    sendDataToActivity(txtMessageTemp1);
                }catch (Exception e){
                    Log.d(LOG_TAG, e.toString());
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    };
    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context,"Запрос отправлен",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context,"Ошибка отправки",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()){
                case Activity.RESULT_OK:
                    Toast.makeText(context,"Матиз получил запрос",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(context,"Матиз не получил запрос",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(LOG_TAG, "MyService onStartCommand");
        String command = intent.getStringExtra(MainActivity.COMMAND_FROM_ACTIVITY);
        Log.d(LOG_TAG, command);
        sendSMS(command);
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendDataToActivity(String data){
        //Созданм Интент
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        //Кладем в него данные
        intent.putExtra(MainActivity.DATA_TO_ACTIVITY, data);
        //Отправляем
        sendBroadcast(intent);
    }

    public void sendSMS(String command){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(PHONE_NUMBER, null, command, sent_pi, deliver_pi);
            //smsManager.sendTextMessage(PHONE_NUMBER, null, command, null, null);
        } catch (Exception e) {
            Log.d(LOG_TAG, e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
