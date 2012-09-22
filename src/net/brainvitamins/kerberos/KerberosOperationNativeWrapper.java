package net.brainvitamins.kerberos;

import net.brainvitamins.kerberos.KerberosOperation;
import android.os.Handler;

public class KerberosOperationNativeWrapper {
	public native int nativeSetEnv(String variableName, String value);

	protected final Handler messageHandler;

	public Handler getMessageHandler() {
		return messageHandler;
	}

	public void log(String message) {
		KerberosOperation.log(messageHandler, message);
	}

	public KerberosOperationNativeWrapper(Handler messageHandler) {
		super();
		this.messageHandler = messageHandler;
	}
}
