package com.example.anshul.panicbuttonapp;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    static ArrayList<String> temp = new ArrayList<>();
    EditText text;
    EditText textR;

    int touches = 0;

    public static  String  tagId = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Log.d("NFCAPP", " On create called in the main activity");

        if (nfcAdapter == null) {
            Log.d("NFCAPP", "NFC not supported in the device. Exiting");
            return;
        }

        Intent intent = new Intent(this, this.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Button button = findViewById(R.id.addNumber);
        text = findViewById(R.id.editText);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (temp.size() != 0 && text.getText().toString() != " ") {
                    for (int r = 0; r < temp.size(); r++) {
                        if (text.getText().toString() == temp.get(r)) {
                            Toast.makeText(getApplicationContext(), "Enter unique number", Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            temp.add(text.getText().toString());
                            System.out.println(text.getText().toString());
                            Toast.makeText(getApplicationContext(), "Number Stored, enter new number if desired", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                }
                if (temp.size() == 0 && text.getText().toString() != " ") {
                    temp.add(text.getText().toString());
                    System.out.println(text.getText().toString());
                    Toast.makeText(getApplicationContext(), "Number Stored, enter new number if desired", Toast.LENGTH_LONG).show();

                }
                if (text.getText().toString() == " ") {
                    Toast.makeText(getApplicationContext(), "Enter a number", Toast.LENGTH_SHORT).show();

                }

            }

        });




    }

    void sendSMSB(String[] numberArr, String message){
        for(int i = 0; i<numberArr.length; i++) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numberArr[i], null, message, null, null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("NFCAPP", "Got new Intent");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        String receivedTagId = bin2hex(tag.getId());

        tagId = receivedTagId;

        PackageManager pm = this.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            System.out.println("Supported");
        } else {
            System.out.println("nope");
        }

        Log.d("NFCAPP", "The tag contains : " + tag);
        Log.d("NFCAPP", "The tag ID : " + receivedTagId);
        Log.d("NFCAPP", "The tag tech list : " + tag.getTechList());

        String[] perms = {"android.permission.SEND_SMS","android.permission.CALL_PHONE"};

        int permsRequestCode = 200;

        this.requestPermissions(perms,permsRequestCode);

        touches++;

            if (receivedTagId.equals(tagId) && touches == 1 || touches > 2) {
                System.out.println(touches+" Touches");
                String messageStr = "I am uncomfortable in my current location, please help me,:";
                for(int bol = 0; bol < temp.size(); bol++){
                    messageStr += temp.get(bol) + ',';
                    System.out.println(messageStr);
                }
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("6467620371", null, messageStr, null, null);

                System.out.println("Message Set");

            } else if(receivedTagId.equals(tagId) && touches == 2){
                if(this.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    String[] perm = {"android.permission.CALL_PHONE"};
                    this.requestPermissions(perm,200);
                }else{
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:7323106608"));
                    startActivity(callIntent);
                }
            }
    }
    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }
}

