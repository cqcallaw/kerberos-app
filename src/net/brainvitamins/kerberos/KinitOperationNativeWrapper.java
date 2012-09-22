package net.brainvitamins.kerberos;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import net.brainvitamins.kerberos.KerberosOperationNativeWrapper;

import android.os.Handler;
import android.os.Message;

/**
 * Protected wrapper to enable the stateful interaction with the native library
 * 
 * Attempting to use message handlers directly in native code messes with the
 * Android thread dispatching mechanisms
 * 
 */
class KinitOperationNativeWrapper extends KerberosOperationNativeWrapper
		implements AuthenticationDialogHandler {

	public native int nativeKinit(String argv, int argc);

	boolean callbacksProcessed = false;

	public KinitOperationNativeWrapper(Handler messageHandler) {
		super(messageHandler);
	}

	public synchronized String[] kinitPrompter(String name, String banner,
			Callback[] callbacks) throws UnsupportedCallbackException,
			IOException {
		String[] result = new String[callbacks.length];

		// populate result with default values
		for (int i = 0; i < result.length; i++) {
			result[i] = "";
		}

		KerberosCallbackArray callbackArray = new KerberosCallbackArray(
				callbacks, this);

		Message promptMessage = Message.obtain(messageHandler,
				KerberosOperation.PROMPTS_MESSAGE, callbackArray);

		messageHandler.sendMessage(promptMessage);

		while (!callbacksProcessed) {
			try {
				wait();
			} catch (InterruptedException e) {
				messageHandler
						.sendEmptyMessage(KerberosOperation.AUTHENTICATION_CANCEL_MESSAGE);
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