package edu.mit.kerberos;

import android.os.Handler;

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

/**
 * 
 * A pseudo-operation that simply sets an environment variable.
 * 
 * It is not necessary to perform this operation before invoking the Java
 * wrappers for the standard Kerberos operations (kinit et al.): the requisite
 * environment variables for the standard Kerberos operations are set by their
 * respective Java wrappers
 * 
 */
public class SetEnvOperation extends KerberosOperation {

	public static final String LOG_TAG = "SetEnvOperation";

	public synchronized static void execute(final String variableName,
			final String value, final Handler messageHandler) {

		final SetEnvOperationNativeWrapper wrapper = new SetEnvOperationNativeWrapper(
				messageHandler);

		operation = new Thread() {
			public void run() {
				if (operationLock.tryLock()) {
					try {
						wrapper.setEnv(variableName, value);
					} catch (Error e) {
						wrapper.log("ERROR: " + e.getMessage());
						wrapper.messageHandler
								.sendEmptyMessage(FAILURE_MESSAGE);
					} finally {
						// we'll maintain a reference to this thread object
						// until the next operation...
						operationLock.unlock();
					}
				}
			}
		};

		operation.start();
	}
}