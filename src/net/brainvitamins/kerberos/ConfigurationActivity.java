package net.brainvitamins.kerberos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import edu.mit.kerberos.R;

public class ConfigurationActivity extends Activity {

	private static final String LOG_TAG = "ConfigurationActivity";

	private static File localConfigurationFile;
	private EditText configurationEditor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_configuration);

		localConfigurationFile = new File(getFilesDir() + File.separator
				+ "krb5.conf");

		// make sure localConfigurationFile has a default contents
		if (!localConfigurationFile.exists()) {
			try {
				Log.d(LOG_TAG, "Initializing local configuration file "
						+ localConfigurationFile.getCanonicalPath().toString());
				InputStream is = getAssets().open("krb5.conf");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();

				FileOutputStream fos = new FileOutputStream(
						localConfigurationFile);
				fos.write(buffer);
				fos.close();

			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}

		configurationEditor = (EditText) findViewById(R.id.configuration_editor);

		// ref: http://stackoverflow.com/a/326440/577298
		String result = "";
		try {
			FileInputStream stream = new FileInputStream(localConfigurationFile);
			FileChannel channel = stream.getChannel();
			MappedByteBuffer buffer = channel.map(
					FileChannel.MapMode.READ_ONLY, 0, channel.size());
			result = Charset.forName("UTF-8").decode(buffer).toString();
			stream.close();
		} catch (FileNotFoundException e) {
			result = "ERROR: configuration file "
					+ localConfigurationFile.getAbsolutePath() + " not found.";
			configurationEditor.setFocusable(false);
		} catch (IOException e) {
			result = "ERROR: exception reading configuration file "
					+ localConfigurationFile.getAbsolutePath() + ": "
					+ e.getMessage();
			configurationEditor.setFocusable(false);
		}

		// TODO: test focusable--text should be copy-able, but not modifiable.
		configurationEditor.setText(result);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_configuration, menu);
		return true;
	}

	public void saveConfiguration(View view) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(localConfigurationFile);
		Log.d(LOG_TAG, "Outputting configuration: "
				+ configurationEditor.getText().toString());
		out.print(configurationEditor.getText().toString());
		out.close();
	}
}
