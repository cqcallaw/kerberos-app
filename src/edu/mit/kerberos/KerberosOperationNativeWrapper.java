package edu.mit.kerberos;

import edu.mit.kerberos.KerberosOperation;
import android.os.Handler;

public abstract class KerberosOperationNativeWrapper {
	protected native int setEnv(String variableName, String value);

	protected final Handler messageHandler;

	public abstract int executeNativeOperation(String arguments,
			int argumentCount);

	public Handler getMessageHandler() {
		return messageHandler;
	}

	public void log(String message) {
		KerberosOperation.log(messageHandler, message);
	}

	public KerberosOperationNativeWrapper(Handler messageHandler) {
		if (messageHandler == null)
			throw new IllegalArgumentException(
					"Argument messageHandler cannot be null.");

		this.messageHandler = messageHandler;
	}
}
