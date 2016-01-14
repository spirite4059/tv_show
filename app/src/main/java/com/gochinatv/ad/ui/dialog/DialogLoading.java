package com.gochinatv.ad.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.gochinatv.ad.R;


/**
 * Created by zfy on 2015/11/19.
 */
public class DialogLoading extends Dialog {


    public DialogLoading(Context context) {
        super(context, R.style.ThemeDialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progres);
    }
}


