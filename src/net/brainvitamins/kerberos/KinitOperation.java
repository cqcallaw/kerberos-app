package net.brainvitamins.kerberos;

/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

	/**
	 * Private constructor to maintain state
	 * 
	 * @param messageHandler
	 */
	private KinitOperation(Handler messageHandler) {
		super(messageHandler);
	}

	public static void execute(String principalName, Handler messageHandler) {
		execute(principalName, Utilities.getDefaultCredentialsCache(),
				messageHandler);
	}

	public static void execute(String principalName,
			CredentialsCacheFile credentialsCache, Handler messageHandler) {

		KinitOperation operation = new KinitOperation(messageHandler);

		if (!credentialsCache.getParentFile().exists()) {
			operation
					.log("ERROR: Credentials cache directory does not exist.\n");
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
			return;
		}

		try {
			String kinitArguments = "-V -c "
					+ credentialsCache.getCanonicalPath() + " " + principalName;
			launchNativeKinit(kinitArguments, operation);
		} catch (Error e) {
			operation.log("ERROR: " + e.getMessage());
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
		} catch (IOException io) {
			operation.log("ERROR: " + io.getMessage());
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
		}
	}

	public static void execute(String principalName, KeytabFile keytab,
			Handler logMessageHandler) {
		execute(principalName, keytab, Utilities.getDefaultCredentialsCache(),
				logMessageHandler);
	}

	public static void execute(String principalName, KeytabFile keytab,
			CredentialsCacheFile credentialsCache, Handler messageHandler) {
		KinitOperation operation = new KinitOperation(messageHandler);

		// so much copy pasta...
		if (!credentialsCache.getParentFile().exists()) {
			operation
					.log("ERROR: Credentials cache directory does not exist.\n");
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
			return;
		}

		try {
			String kinitArguments = "-V -c "
					+ credentialsCache.getCanonicalPath() + " -k -t "
					+ keytab.getAbsolutePath() + " " + principalName;

			launchNativeKinit(kinitArguments, operation);
		} catch (Error e) {
			operation.log("ERROR: " + e.getMessage());
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
		} catch (IOException io) {
			operation.log("ERROR: " + io.getMessage());
			operation.messageHandler
					.sendEmptyMessage(AUTHENTICATION_FAILURE_MESSAGE);
		}
	}

	private static void launchNativeKinit(final String kinitArguments,
			final KinitOperation operation) {
		new Thread() {
			public void run() {
				if (kinitLock.tryLock()) {
					Log.d(LOG_TAG, "Going native...");
					try {
						int authenticationResult = operation.nativeKinit(
								kinitArguments,
								KerberosOperations.countWords(kinitArguments));

						if (authenticationResult == 0) {
							operation.messageHandler
									.sendEmptyMessage(AUTHENTICATION_SUCCESS_MESSAGE);
						} else {
							operation.messageHandler
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

		Message promptMessage = Message.obtain(messageHandler, PROMPTS_MESSAGE,
				callbackArray);

		messageHandler.sendMessage(promptMessage);

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
