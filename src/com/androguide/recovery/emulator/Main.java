package com.androguide.recovery.emulator;

import java.io.File;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.androguide.recovery.emulator.helpers.EdifyParser;
import com.androguide.recovery.emulator.helpers.Help;
import com.androguide.recovery.emulator.helpers.MyPlayCard;
import com.fima.cardsui.views.CardUI;
import com.ipaulpro.afilechooser.utils.FileUtils;

public class Main extends Activity implements OnCheckedChangeListener,
		OnClickListener {

	private static final int REQUEST_CODE = 6384; // onActivityResult request
													// code
	private CheckBox cache, dalvik, data;
	private Button selectZip, flashNow;
	private TextView zipLoc;
	private Boolean wipeCache = false, wipeDalvik = false, wipeData = false;
	private File zipToFlash;
	private CardUI mCardView;

	public static String sd = Environment.getExternalStorageDirectory()
			.toString();
	public static String temp = sd + "/RecoveryEmulator/tmp";
	public static String updaterScript = "META-INF/com/google/android/updater-script";
	public static String homeDir = sd + "/RecoveryEmulator";
	private static Handler handler;
	Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		zipLoc = (TextView) findViewById(R.id.zipLocation);
		cache = (CheckBox) findViewById(R.id.wipeCache);
		dalvik = (CheckBox) findViewById(R.id.wipeDalvik);
		data = (CheckBox) findViewById(R.id.wipeData);

		selectZip = (Button) findViewById(R.id.pickZip);
		flashNow = (Button) findViewById(R.id.flashNow);

		mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setSwipeable(true);

		cache.setOnCheckedChangeListener(this);
		dalvik.setOnCheckedChangeListener(this);
		data.setOnCheckedChangeListener(this);

		selectZip.setOnClickListener(this);
		flashNow.setOnClickListener(this);

		MyPlayCard card = new MyPlayCard(
				"Do Not Flash ROMs",
				"Flashing ROMs with Flash Gordon isn\'t currently possible,"
						+ " because certain system files crash the device if they are replaced while it is running,"
						+ " this will cut the installation halfway and result in a bootloop.",
				"#e00707", "#222222", false, false);
		mCardView.addCard(card);
		mCardView.refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton cb, boolean b) {
		switch (cb.getId()) {

		case R.id.wipeCache:
			if (b = true)
				wipeCache = true;
			break;

		case R.id.wipeDalvik:
			if (b = true)
				wipeDalvik = true;
			break;

		case R.id.wipeData:
			if (b = true)
				wipeData = true;
			break;

		default:
			return;
		}

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.pickZip:
			showChooser();
			break;

		case R.id.flashNow:
			flashNow();
			isWhichWipe();
			break;

		default:
			return;
		}
	}

	private void flashNow() {

		final ProgressDialog flashDialog = new ProgressDialog(this);
		flashDialog.setIndeterminate(true);
		flashDialog.setMessage("Flashing...");
		flashDialog.setTitle("Please Wait");
		flashDialog.setCanceledOnTouchOutside(false);
		flashDialog.show();

		new Thread() {
			public void run() {
				EdifyParser.readUpdaterScript(temp + "/" + updaterScript);
				CMDProcessor cmd = new CMDProcessor();
				cmd.su.runWaitFor("busybox mount -o remount,rw /system");
				cmd.su.runWaitFor("sh " + temp + "/" + "flash_gordon.sh");
				cmd.su.runWaitFor("busybox mount -o remount,ro /system");
				handler.sendEmptyMessage(0);
			}
		}.start();

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				flashDialog.dismiss();
				createNotification(1234);
				super.handleMessage(msg);
			}
		};
	}

	private void showChooser() {
		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target,
				getString(R.string.chooser_title));
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
		}
	}

	private void isWhichWipe() {

		if (wipeCache == true)
			Help.wipeCache();
		if (wipeDalvik == true)
			Help.wipeDalvik();
		if (wipeData == true)
			Help.wipeData();

	}

	private void createNotification(long when) {

		String notificationContent = "The changes you made require a reboot.";
		String notificationTitle = "Reboot Required";
		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.ic_launcher);
		int smalIcon = R.drawable.ic_launcher;

		Intent intent = new Intent(getApplicationContext(),
				RebootActivity.class);

		intent.setData(Uri.parse("content://" + when));

		PendingIntent pendingIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationManager notificationManager = (NotificationManager) getApplicationContext()
				.getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
				getApplicationContext()).setWhen(when)
				.setContentText(notificationContent)
				.setContentTitle(notificationTitle)
				.setContentInfo("Tap to Reboot").setSmallIcon(smalIcon)
				.setAutoCancel(true).setTicker(notificationTitle)
				.setLargeIcon(largeIcon).setContentIntent(pendingIntent);

		notificationManager.notify((int) when,
				notificationBuilder.getNotification());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					final Uri uri = data.getData();

					try {
						final File file = FileUtils.getFile(uri);
						zipToFlash = file;
						zipLoc.setText(zipToFlash.toString());

						final ProgressDialog myProgress = new ProgressDialog(
								this);
						myProgress.setIndeterminate(true);
						myProgress.setMessage("Extracting...");
						myProgress.setTitle("Please Wait");
						myProgress.setCanceledOnTouchOutside(false);
						myProgress.show();

						new Thread() {
							public void run() {
								Help.installZipBinary(context);
								CMDProcessor cmd = new CMDProcessor();
								cmd.su.runWaitFor("busybox rm -rf " + temp);
								cmd.su.runWaitFor("mkdir " + homeDir + "/tmp");
								cmd.su.runWaitFor("unzip -d " + homeDir
										+ "/tmp " + zipToFlash);

								handler.sendEmptyMessage(0);
							}
						}.start();

						handler = new Handler() {

							@Override
							public void handleMessage(Message msg) {
								myProgress.dismiss();

								if (!zipToFlash.toString().contains(".zip")) {
									Toast.makeText(
											context,
											"The file you selected is not a zip!",
											Toast.LENGTH_LONG).show();
								}

								if (!Help.isFlashable()) {
									Toast.makeText(
											context,
											"The package you selected is not a valid flashable zip!",
											Toast.LENGTH_SHORT).show();
								}
								super.handleMessage(msg);
							}
						};
					} catch (Exception e) {
						Log.e("Recovery Emulator", "File select error", e);
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
