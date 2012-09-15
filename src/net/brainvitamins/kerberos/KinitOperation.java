package net.brainvitamins.kerberos;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.mit.kerberos.KerberosOperations;

public class KinitOperation extends KerberosOperation implements
		AuthenticationDialogHandler {

	public static final String LOG_TAG = "KinitRunner";

	private static final Lock kinitLock = new ReentrantLock();

	// no simple way to integrate this with the MIT-licensed
	// KerberosOperations...
	private native int nativeKinit(String argv, int argc);

	boolean callbacksProcessed = false;

	protected final Handler promptHandler;

	/**
	 * Private constructor to maintain state
	 * 
	 * @param logMessageHandler
	 * @param promptHandler
	 */
	private KinitOperation(Handler logMessageHandler, Handler promptHandler) {
		super(logMessageHandler);
		this.promptHandler = promptHandler;
	}

	public static void execute(String principalName, Handler logMessageHandler,
			Handler promptHandler) {
		execute(principalName, Utilities.getDefaultCredentialsCache(),
				logMessageHandler, promptHandler);
	}

	public static void execute(String principalName,
			CredentialsCacheFile credentialsCache, Handler logMessageHandler,
			Handler promptHandler) {
		String kinitArguments = "-V -c " + credentialsCache.getAbsolutePath()
				+ " " + principalName;

		try {
			launchNativeKinit(kinitArguments, logMessageHandler, promptHandler);
		} catch (Error e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	public static void execute(String principalName, KeytabFile keytab,
			Handler logMessageHandler, Handler promptHandler) {
		execute(principalName, keytab, Utilities.getDefaultCredentialsCache(),
				logMessageHandler, promptHandler);
	}

	public static void execute(String principalName, KeytabFile keytab,
			CredentialsCacheFile credentialsCache, Handler logMessageHandler,
			Handler promptHandler) {
		String kinitArguments = "-V -c " + credentialsCache.getAbsolutePath()
				+ " -k -t " + keytab.getAbsolutePath() + " " + principalName;

		try {
			launchNativeKinit(kinitArguments, logMessageHandler, promptHandler);
		} catch (Error e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	private static void launchNativeKinit(final String kinitArguments,
			final Handler logMessageHandler, final Handler promptHandler) {
		new Thread() {
			public void run() {
				if (kinitLock.tryLock()) {
					Log.d(LOG_TAG, "Going native...");
					try {
						int authenticationResult = new KinitOperation(
								logMessageHandler, promptHandler).nativeKinit(
								kinitArguments,
								KerberosOperations.countWords(kinitArguments));

						if (authenticationResult == 0) {
							logMessageHandler
									.sendEmptyMessage(AUTHENTICATION_SUCCESS_MESSAGE);
						} else {
							logMessageHandler
									.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
						}
						kinitLock.unlock();
					} catch (Error e) {
						Log.i(LOG_TAG, e.getMessage());
						kinitLock.unlock();
						return;
					}
				} else {
					Log.i(LOG_TAG,
							"Attempted to launch multiple concurrent kinit calls.");
				}
			}
		}.start();
	}

	public synchronized String[] kinitPrompter(String name, String banner,
			Callback[] callbacks) throws UnsupportedCallbackException,
			IOException {
		String[] result = new String[callbacks.length];

		KerberosCallbackArray callbackArray = new KerberosCallbackArray(
				callbacks, this);

		Message promptMessage = Message.obtain(promptHandler,
				GET_CREDENTIALS_MESSAGE, callbackArray);

		promptHandler.sendMessage(promptMessage);

		while (!callbacksProcessed) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return result;
			}
		}

		for (int i = 0; i < callbacks.length; i++) {
			Callback callback = callbacks[i];
			if (!(callback instanceof PasswordCallback)) {
				throw new UnsupportedCallbackException(callback);
			} else {
				result[i] = new String(
						((PasswordCallback) callback).getPassword());
			}
		}

		return result;
	}

	public synchronized void signalCallbackProcessFinished() {
		callbacksProcessed = true;
		notifyAll();
	}
}
