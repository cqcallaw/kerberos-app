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

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class KerberosOperation {
	public static final String LOG_TAG = "KinitOperation";

	public static final int LOG_MESSAGE = 10;
	public static final int PROMPTS_MESSAGE = 20;
	public static final int AUTHENTICATION_FAILURE_MESSAGE = 30;
	public static final int AUTHENTICATION_SUCCESS_MESSAGE = 40;

	protected final Handler messageHandler;

	public native int nativeSetEnv(String variableName, String value);

	public KerberosOperation(Handler messageHandler) {
		super();
		this.messageHandler = messageHandler;
	}

	public void log(String message) {
		Log.d(LOG_TAG, message);
		Message logMessage = Message.obtain(messageHandler, LOG_MESSAGE,
				message);
		messageHandler.sendMessage(logMessage);
	}

	static {
		System.loadLibrary("kerberosapp");
	}
}