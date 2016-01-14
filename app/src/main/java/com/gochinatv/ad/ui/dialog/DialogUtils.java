package com.gochinatv.ad.ui.dialog;

import android.content.Context;

/**
 * Created by fq_mbp on 15/12/24.
 */
public class DialogUtils {

    /**
     * loading dialog
     */
    public static DialogLoading showLoading(Context context) {
        if (context == null)
            return null;
        DialogLoading dialogLoading = new DialogLoading(context);
        dialogLoading.show();
        return dialogLoading;
    }

}
