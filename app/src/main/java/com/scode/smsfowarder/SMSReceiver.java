package com.scode.smsfowarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {
    public static final String SMS_BUNDLE = "pdus";

    MainActivity inst = MainActivity.instance();

    private String lastMessage;
    private String lastSender;
    private String lastID;
    private String lastDate;



    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();
                String id = String.valueOf(smsMessage.getTimestampMillis());

                Date date = new Date(smsMessage.getTimestampMillis());
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                String smsDate = df.format(date);


                smsMessageStr = "Date: " + smsDate +  "\n" +
                        "From: " + address +  "\n" +
                        "Message: " +  smsBody+ "\n";

                lastMessage = smsBody;
                lastSender = address;
                lastID = id;
                lastDate = smsDate;

            }

            if(inst.getBoolean("phone_switch")){
                sendSMS(inst.getString("phone_number"), lastMessage);
                Toast.makeText(context, "Send SMS Completed!", Toast.LENGTH_SHORT).show();
            }

            if(inst.getBoolean("email_switch")){
                String msg = "Send Email Completed";

                inst.sendEmail(inst.getString("email"), lastMessage);
                //inst.sendBackgoundEmail(inst.getString("email"), lastMessage);

                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }

            if(inst.getBoolean("url_switch")){
                inst.postNewComment(context,inst.getString("url"), lastSender, lastMessage, lastID, lastDate);
                Toast.makeText(context, "Post Url Completed!", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();
            //Toast.makeText(context, lastSMS, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            inst.updateList(smsMessageStr);
        }
    }

    //Sends an SMS message to another device

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

}
