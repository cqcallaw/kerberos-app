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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class KerberosOperation {
	static {
		System.loadLibrary("kerberosapp");
	}

	public static final String LOG_TAG = "KerberosOperation";

	public static final int LOG_MESSAGE = 10;
	public static final int PROMPTS_MESSAGE = 20;
	public static final int AUTHENTICATION_FAILURE_MESSAGE = 30;
	public static final int AUTHENTICATION_SUCCESS_MESSAGE = 40;
	public static final int AUTHENTICATION_CANCEL_MESSAGE = 50;

	// Lock to prevent multiple concurrent Kerberos operations
	protected static final Lock operationLock = new ReentrantLock();

	protected static Thread operation = null;

	public static void log(Handler messageHandler, String message) {
		Log.d(LOG_TAG, message);
		Message logMessage = Message.obtain(messageHandler, LOG_MESSAGE,
				message);
		messageHandler.sendMessage(logMessage);
	}
}