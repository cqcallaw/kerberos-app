package net.brainvitamins.kerberos;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class KerberosProvider extends ContentProvider {
	// ref:
	// https://github.com/commonsguy/cw-advandroid/tree/master/ContentProvider/Files/src/com/commonsware/android/cp/files

	public static final Uri CONTENT_URI = Uri
			.parse("content://net.brainvitamins.kerberos.provider/credentials_cache");

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new RuntimeException("Operation not supported");
	}

	@Override
	public String getType(Uri uri) {
		return "application/octet-stream";
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {

		if (uri.getPath().equals("/credentials_cache")) {
			CredentialsCacheFile f = new CredentialsCacheFile(getContext()
					.getFilesDir());

			if (f.exists()) {
				return (ParcelFileDescriptor.open(f,
						ParcelFileDescriptor.MODE_READ_ONLY));
			}

			throw new FileNotFoundException(f.getPath());
		} else if (uri.getPath().equals("/configuration")) {
			File f = new File(getContext().getFilesDir() + File.pathSeparator
					+ "krb5.conf");

			if (f.exists()) {
				return (ParcelFileDescriptor.open(f,
						ParcelFileDescriptor.MODE_READ_ONLY));
			}

			throw new FileNotFoundException(f.getPath());

		}

		else {
			throw new FileNotFoundException(uri.toString());
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new RuntimeException("Operation not supported");
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new RuntimeException("Operation not supported");
	}

}
