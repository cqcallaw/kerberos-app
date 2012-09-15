package net.brainvitamins.kerberos;

import javax.security.auth.callback.Callback;

public class KerberosCallbackArray {

	Callback[] callbacks;

	public Callback[] getCallbacks() {
		return callbacks;
	}

	AuthenticationDialogHandler source;

	public AuthenticationDialogHandler getSource() {
		return source;
	}

	public KerberosCallbackArray(Callback[] callbacks, KinitOperation source) {
		super();
		this.callbacks = callbacks;
		this.source = source;
	}
}
