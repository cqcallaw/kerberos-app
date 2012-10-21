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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.mit.kerberos.KerberosOperations;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class KerberosOperation {
	static {
		System.loadLibrary("kerberosapp");
	}

	public static final String LOG_TAG = "KerberosOperation";

	public static final int SUCCESS_MESSAGE = 0;
	public static final int LOG_MESSAGE = 10;
	public static final int PROMPTS_MESSAGE = 20;
	public static final int FAILURE_MESSAGE = 30;
	public static final int CANCEL_MESSAGE = 40;

	public static void log(Handler messageHandler, String message) {
		Log.d(LOG_TAG, message);
		Message logMessage = Message.obtain(messageHandler, LOG_MESSAGE,
				message);
		messageHandler.sendMessage(logMessage);
	}
	
	// Lock to prevent multiple concurrent Kerberos operations
	protected static final Lock operationLock = new ReentrantLock();

	protected static Thread operation = null;

	/**
	 * Helper function to handle the validation logic and threading of Kerberos operations
	 * @param operationArguments
	 * @param wrapper
	 * @param credentialsCache
	 * @param configurationFile
	 */
	protected synchronized static void execute(final String operationArguments,
			final KerberosOperationNativeWrapper wrapper,
			final CredentialsCacheFile credentialsCache,
			final File configurationFile) {
		
		Handler messageHandler = wrapper.getMessageHandler();
		
		if (credentialsCache == null) {
			log(messageHandler,
					"ERROR: Credentials cache file reference cannot be null.\n");
			messageHandler.sendEmptyMessage(FAILURE_MESSAGE);
			return;
		}

		if (!credentialsCache.getParentFile().exists()) {
			log(messageHandler,
					"ERROR: Credentials cache directory does not exist.\n");
			messageHandler.sendEmptyMessage(FAILURE_MESSAGE);
			return;
		}

		if (configurationFile == null) {
			log(messageHandler,
					"ERROR: Configuration file reference cannot be null.\n");
			messageHandler.sendEmptyMessage(FAILURE_MESSAGE);
			return;
		}

		if (!configurationFile.exists()) {
			log(messageHandler, "ERROR: Configuration file does not exist.\n");
			messageHandler.sendEmptyMessage(FAILURE_MESSAGE);
			return;
		}

		operation = new Thread() {
			public void run() {
				if (operationLock.tryLock()) {
					try {
						wrapper.nativeSetEnv("KRB5_CONFIG", configurationFile
								.getCanonicalPath().toString());
						wrapper.nativeSetEnv("KRB5CCNAME", credentialsCache
								.getCanonicalPath().toString());

						Log.d(LOG_TAG, "Going native...");
						int listResult = wrapper.executeNativeOperation(
								operationArguments, KerberosOperations
										.countWords(operationArguments));

						Log.d(LOG_TAG, "Native return code: " + listResult);
						if (listResult == SUCCESS_MESSAGE
								|| listResult == CANCEL_MESSAGE) {
							wrapper.messageHandler.sendEmptyMessage(listResult);
						} else {
							wrapper.messageHandler
									.sendEmptyMessage(FAILURE_MESSAGE);
						}
					} catch (Error e) {
						wrapper.log("ERROR: " + e.getMessage());
						wrapper.messageHandler
								.sendEmptyMessage(FAILURE_MESSAGE);
					} catch (IOException io) {
						wrapper.log("ERROR: " + io.getMessage());
						wrapper.messageHandler
								.sendEmptyMessage(FAILURE_MESSAGE);
					} finally {
						// we'll maintain a reference to this thread object
						// until the next operation...
						operationLock.unlock();
					}
				} else {
					Log.e(LOG_TAG,
							"Attempted to launch multiple concurrent native operations.");
				}
			}
		};

		operation.start();
	}
}