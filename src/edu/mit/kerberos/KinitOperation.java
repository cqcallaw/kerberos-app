package edu.mit.kerberos;

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

import android.os.Handler;
import android.util.Log;

public class KinitOperation extends KerberosOperation {

	public static final String LOG_TAG = "KinitOperation";

	public static KinitOperationNativeWrapper wrapper;

	public synchronized static void execute(String principalName,
			final CredentialsCacheFile credentialsCache,
			final File configurationFile, final Handler messageHandler) {

		execute("-V " + principalName, new KinitOperationNativeWrapper(
				messageHandler), credentialsCache, configurationFile);
	}

	public synchronized static void cancel() {
		if (operation != null) {
			operation.interrupt();
		} else {
			Log.d(LOG_TAG, "Cancel attempt with no running operations.");
		}
	}

	public static void execute(String principalName, KeytabFile keytab,
			final CredentialsCacheFile credentialsCache,
			File configurationFile, Handler messageHandler) {
		execute("-V -k -t " + keytab.toString(),
				new KinitOperationNativeWrapper(messageHandler),
				credentialsCache, configurationFile);
	}
}
