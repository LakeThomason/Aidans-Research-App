package com.example.lakethomason.bluetoothinteractiontest;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Created by lakethomason on 1/11/2018.
 */

public class EasyToast {
    private Activity _activity;
    private Toast myToast;

    public EasyToast(Activity activity) {
        _activity = activity;
    }

    public void makeToast(String text){
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = Toast.makeText(_activity, text, Toast.LENGTH_SHORT);
        myToast.show();
    }

    public void makeToastOnUIThread(String text) {
        final String message = text;
        _activity.runOnUiThread(new Runnable() {
            public void run() {
                makeToast(message);
            }
        });
    }
}
