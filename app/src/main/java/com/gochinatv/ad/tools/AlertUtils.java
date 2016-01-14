package com.gochinatv.ad.tools;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

public class AlertUtils {

	private static Toast toast;

	public static void alert(Context context, String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (toast == null) {
			toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	public static void alertAtTime(Context context, String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}

		if (toast == null) {
			toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	public static void alertLong(Context context, String msg) {
		if (TextUtils.isEmpty(msg)) {
			return;
		}
		if (toast == null) {
			toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_LONG);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}



	/**
	 * 显示提示信息
	 * 
	 * @param activity
	 *            Context上下文
	 * @param content
	 *            提示的内容
	 */
	public static void toastInfo(Context activity, String content) {
		if (TextUtils.isEmpty(content)) {
			return;
		}
		Toast toast = Toast.makeText(activity, content, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	public static void cancleToast() {
		if (toast != null) {
			toast.cancel();
		}
	}

}
