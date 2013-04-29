package edu.mit.kerberos;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;


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
		implements KerberosCallbackArraySource {

	public native int nativeKinit(String argv, int argc);

	boolean callbacksProcessed = false;

	public KinitOperationNativeWrapper(Handler messageHandler) {
		super(messageHandler);
	}

	public synchronized int kinitPrompter(String name, String banner,
			Callback[] callbacks) throws UnsupportedCallbackException,
			IOException {

		KerberosCallbackArray callbackArray = new KerberosCallbackArray(
				callbacks, this);

		Message promptMessage = Message.obtain(messageHandler,
				KerberosOperation.PROMPTS_MESSAGE, callbackArray);

		messageHandler.sendMessage(promptMessage);

		while (!callbacksProcessed) {
			try {
				wait();
			} catch (InterruptedException e) {
				return KerberosOperation.CANCEL_MESSAGE;
			}
		}

		return 0;
	}

	public synchronized void signalCallbackProcessFinished() {
		callbacksProcessed = true;
		notifyAll();
	}

	@Override
	public int executeNativeOperation(String arguments, int argumentCount) {
		return nativeKinit(arguments, argumentCount);
	}
}