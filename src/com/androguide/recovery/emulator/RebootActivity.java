package com.androguide.recovery.emulator;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

public class RebootActivity extends Activity {

	SharedPreferences rPrefs;
	int mode = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//hotReboot();
		normalReboot();
	}

	private void hotReboot() {

		CMDProcessor cmd = new CMDProcessor();
		cmd.su.runWaitFor("busybox killall system_server");
	}

	private void normalReboot() {

		CMDProcessor cmd = new CMDProcessor();
		cmd.su.runWaitFor("busybox reboot");
	}

}
