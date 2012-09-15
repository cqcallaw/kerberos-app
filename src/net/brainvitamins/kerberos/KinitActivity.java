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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.PasswordCallback;

import net.brainvitamins.state.Edge;
import net.brainvitamins.state.FiniteStateGraph;
import net.brainvitamins.state.Vertex;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.mit.kerberos.R;

public class KinitActivity extends Activity {

	private static final String LOG_TAG = "KinitActivity";

	private static final Vertex start = new Vertex("START");
	private static final Vertex requestingAuthentication = new Vertex(
			"REQUEST AUTHENTICATION");
	private static final Vertex queryingUser = new Vertex("USER CONVERSATION");
	private static final Vertex sentCredentials = new Vertex("SENT CREDENTIALS");
	private static final Vertex failure = new Vertex("FAILURE");

	private KerberosCallbackArray callbackArray;

	final Edge toStart = new Edge(start, new Runnable() {
		public void run() {
			authenticateButton.setText(R.string.label_start_authentication);

			authenticateButton.setEnabled(true);
			conversationLayout.removeAllViews();
			principalField.requestFocus();

			authenticateButton
					.setOnClickListener(toRequestingAuthenticationListener);

			principalField.setEnabled(true);
		}
	});

	final Edge toFailure = new Edge(failure, new Runnable() {
		public void run() {
			authenticateButton.setText(R.string.label_retry_authentication);

			authenticateButton.setEnabled(true);
			conversationLayout.removeAllViews();
			principalField.requestFocus();

			authenticateButton
					.setOnClickListener(toRequestingAuthenticationListener);

			principalField.setEnabled(true);
		}
	});

	final Edge toRequestingAuthentication = new Edge(requestingAuthentication,
			new Runnable() {
				public void run() {
					TextView logView = (TextView) findViewById(R.id.log);
					logView.setText("");

					authenticateButton
							.setText(R.string.label_initializing_authentication);
					authenticateButton.setEnabled(false);
					authenticateButton
							.setOnClickListener(toRequestingAuthenticationListener);

					principalField.setEnabled(false);

					String principal = principalField.getText().toString();

					Log.d("KerberosActivity", "Starting kinitAsync operation.");
					KinitOperation.execute(principal, messageHandler);
				}
			});

	final Edge toQueryingUser = new Edge(queryingUser, new Runnable() {
		public void run() {
			authenticateButton.setText(R.string.label_complete_authentication);
			authenticateButton.setEnabled(true);
			authenticateButton.setOnClickListener(toSentCredentialsListener);

			javax.security.auth.callback.Callback[] callbacks = callbackArray
					.getCallbacks();

			// setup UI elements
			for (javax.security.auth.callback.Callback callback : callbacks) {
				PasswordCallback asPasswordCallback = (PasswordCallback) callback;

				EditText promptEditField = new EditText(KinitActivity.this);
				promptEditField.setLayoutParams(passwordPromptLayoutParameters);
				promptEditField.setHint(asPasswordCallback.getPrompt());
				if (!asPasswordCallback.isEchoOn())
					promptEditField.setInputType(InputType.TYPE_CLASS_TEXT
							| InputType.TYPE_TEXT_VARIATION_PASSWORD);

				conversationLayout.addView(promptEditField);
			}

			((EditText) conversationLayout.getChildAt(0)).requestFocus();
		}
	});

	final Edge toSentCredentials = new Edge(sentCredentials, new Runnable() {
		public void run() {
			authenticateButton.setText(R.string.label_authenticating);
			authenticateButton.setEnabled(false);

			javax.security.auth.callback.Callback[] callbacks = callbackArray
					.getCallbacks();

			for (int i = 0; i < callbacks.length; i++) {
				PasswordCallback callback = (PasswordCallback) callbacks[i];
				EditText callbackEditText = (EditText) conversationLayout
						.getChildAt(i);

				callback.setPassword(callbackEditText.getText().toString()
						.toCharArray());
				// callback.setPassword("password".toCharArray());
			}

			callbackArray.getSource().signalCallbackProcessFinished();
		}
	});

	@SuppressWarnings("serial")
	private final FiniteStateGraph stateGraph = new FiniteStateGraph(
			new HashMap<Vertex, Set<Edge>>() {
				{
					put(start, new HashSet<Edge>() {
						{
							add(toRequestingAuthentication);
						}
					});

					put(requestingAuthentication, new HashSet<Edge>() {
						{
							add(toFailure);
							add(toQueryingUser);
						}
					});

					put(queryingUser, new HashSet<Edge>() {
						{
							add(toFailure);
							add(toSentCredentials);
						}
					});

					put(sentCredentials, new HashSet<Edge>() {
						{
							add(toStart);
							add(toFailure);
						}
					});

					put(failure, new HashSet<Edge>() {
						{
							add(toRequestingAuthentication);
						}
					});
				}
			}, start);

	private Button authenticateButton;
	private EditText principalField;
	private LinearLayout conversationLayout;

	private static LayoutParams passwordPromptLayoutParameters = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kinit);

		authenticateButton = (Button) findViewById(R.id.authentication);
		principalField = (EditText) findViewById(R.id.principal);
		conversationLayout = (LinearLayout) findViewById(R.id.conversation_layout);

		authenticateButton
				.setOnClickListener(toRequestingAuthenticationListener);

		// TODO: handle lifetime (onPause)
		// TODO: handle cancellation
		// TODO: store last principal used as a setting
		// TODO: investigate the possibility of replacing krb5.conf or
		// generating it from Android settings
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_kinit, menu);
		return true;
	}

	// UI event handlers
	public View.OnClickListener toRequestingAuthenticationListener = new View.OnClickListener() {
		public void onClick(View v) {
			stateGraph.transition(requestingAuthentication);
		}
	};

	public View.OnClickListener toSentCredentialsListener = new View.OnClickListener() {
		public void onClick(View v) {
			stateGraph.transition(sentCredentials);
		}
	};

	// library event handlers
	Handler messageHandler = new Handler() {
		public void handleMessage(Message message) {
			if (message.what == KerberosOperation.LOG_MESSAGE) {
				log((String) message.obj);
			} else if (message.what == KerberosOperation.AUTHENTICATION_SUCCESS_MESSAGE) {
				stateGraph.transition(start);
			} else if (message.what == KerberosOperation.AUTHENTICATION_FAILURE_MESSAGE) {
				stateGraph.transition(failure);
			} else if (message.what == KerberosOperation.PROMPTS_MESSAGE) {
				callbackArray = (KerberosCallbackArray) message.obj;

				for (javax.security.auth.callback.Callback callback : callbackArray
						.getCallbacks()) {
					if (!(callback instanceof PasswordCallback)) {
						log("Unrecognized callback type sent to kinit UI: "
								+ callback.getClass().toString());

						stateGraph.transition(start);
						return;
					}
				}

				stateGraph.transition(queryingUser);
			} else {
				Log.d(LOG_TAG, "Unrecognized message from Kerberos operation: "
						+ message);
			}
		}
	};

	private void log(String input) {
		TextView tv = (TextView) findViewById(R.id.log);
		tv.append(input);
	}
}
