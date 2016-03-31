package com.download.tools;

import android.util.Log;

@SuppressWarnings("rawtypes")
public class LogCat {

	public static boolean isDebug = true; // 是否开启了debug模式,在menifast.xml中定义.
	public static final String DIVIDER = " ---> "; // 日志追踪默认的位置
	public static final String TAG = "CHINA_RESTAURANT";
	
	public static String getDefMsg(String msg) {
		return LogCat.DIVIDER + msg;
	}

	public static String getLocationMsg(String location, String msg) {
		return location + LogCat.DIVIDER + msg;
	}

	public static void v(Class c, String location, String msg) {
		if (isDebug && msg != null) {

			if (c == null && location == null) {
				Log.v(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.v(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.v(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.v(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void v(String location, String msg) {
		v(null, location, msg);
	}

	public static void v(Class c, String msg) {
		v(c, null, msg);
	}

	public static void d(Class c, String location, String msg) {
		if (isDebug && msg != null) {

			if (c == null && location == null) {
				Log.d(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.d(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.d(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.d(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void d(String location, String msg) {
		d(null, location, msg);
	}

	public static void d(Class c, String msg) {
		d(c, null, msg);
	}

	public static void i(Class c, String location, String msg) {
		if (isDebug && msg != null) {

			if (c == null && location == null) {
				Log.i(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.i(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.i(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.i(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void i(String location, String msg) {
		i(null, location, msg);
	}

	public static void i(Class c, String msg) {
		i(c, null, msg);
	}

	public static void w(Class c, String location, String msg) {
		if (isDebug && msg != null) {

			if (c == null && location == null) {
				Log.w(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.w(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.w(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.w(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void w(String location, String msg) {
		w(null, location, msg);
	}

	public static void w(Class c, String msg) {
		w(c, null, msg);
	}

	public static void e(Class c, String location, String msg) {
		if (isDebug && msg != null) {

			if (c == null && location == null) {
				Log.e(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.e(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.e(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.e(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void e(String location, String msg) {
		e(null, location, msg);
	}

	public static void e(Class c, String msg) {
		e(c, null, msg);
	}
	
	public static void e(String msg) {
		e(null, TAG, msg);
	}
	
	

	public static void e(Class c, String location, Throwable e) {
		if (isDebug) {
			if (c == null && location == null) {
				Log.e(TAG, getDefMsg(null), e);
			} else if (c == null && location != null) {
				Log.e(TAG, getLocationMsg(location, null), e);
			} else if (c != null && location == null) {
				Log.e(c.getSimpleName(), getDefMsg(null), e);
			} else if (c != null && location != null) {
				Log.e(c.getSimpleName(), getLocationMsg(location, null), e);
			}
		}
	}

	public static void a(Class c, String location, String msg) {
		if (isDebug && msg != null) {
			if (c == null && location == null) {
				Log.wtf(TAG, getDefMsg(msg));
			} else if (c == null && location != null) {
				Log.wtf(TAG, getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				Log.wtf(c.getSimpleName(), getDefMsg(msg));
			} else if (c != null && location != null) {
				Log.wtf(c.getSimpleName(), getLocationMsg(location, msg));
			}
		}
	}

	public static void a(String location, String msg) {
		a(null, location, msg);
	}

	public static void a(Class c, String msg) {
		a(c, null, msg);
	}

	public static void out(Class c, String location, String msg) {
		if (isDebug && msg != null) {
			if (c == null && location == null) {
				System.out.println(TAG + getDefMsg(msg));
			} else if (c == null && location != null) {
				System.out.println(TAG + getLocationMsg(location, msg));
			} else if (c != null && location == null) {
				System.out.println(c.getSimpleName() + getDefMsg(msg));
			} else if (c != null && location != null) {
				System.out.println(c.getSimpleName() + getLocationMsg(location, msg));
			}
		}
	}

	public static void out(String location, String msg) {
		out(null, location, msg);
	}

	public static void out(String msg) {
		out(null, msg);
	}
}
