package net.brainvitamins.kerberos;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class KerberosOperation {
	public static final String LOG_TAG = "KinitOperation";

	public static final int LOG_MESSAGE = 10;
	public static final int GET_CREDENTIALS_MESSAGE = 20;
	public static final int AUTHENTICATION_FAILURE_MESSAGE = 30;
	public static final int AUTHENTICATION_SUCCESS_MESSAGE = 40;

	protected final Handler logMessageHandler;

	public KerberosOperation(Handler logMessageHandler) {
		super();
		this.logMessageHandler = logMessageHandler;
	}

	public void log(String message) {
		Log.d(LOG_TAG, message);
		Message logMessage = Message.obtain(logMessageHandler, LOG_MESSAGE,
				message);
		logMessageHandler.sendMessage(logMessage);
	}

	static {
		System.loadLibrary("kerberosapp");
	}
}