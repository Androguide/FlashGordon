package com.androguide.recovery.emulator.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.androguide.recovery.emulator.CMDProcessor;

public class Help {

	public static String sd = Environment.getExternalStorageDirectory()
			.toString();

	public static CMDProcessor cmd = new CMDProcessor();

	public static String homeDir = sd + "/RecoveryEmulator";
	public static String temp = sd + "/RecoveryEmulator/tmp";
	public static String updaterScript = "META-INF/com/google/android/updater-script";


	public static void unpackZip(File selectedZip) {

		cmd.su.runWaitFor("mkdir " + homeDir + "/tmp");
		cmd.su.runWaitFor("unzip -d " + homeDir + "/tmp " + selectedZip);
	}

	public static Boolean isFlashable() {

		File check = new File(temp + "/" + updaterScript);
		if (check.exists())
			return true;
		else
			return false;
	}

	public static void moveScriptToTmp(){
		
		cmd.su.runWaitFor("cp -fp "+ temp + updaterScript + " " + temp + "/updater-script");
	}
	
	public static void installZipBinary(Context context) {

		cmd.su.runWaitFor("mkdir " + homeDir);

		File update = new File(homeDir + "/zip");
		File zipBin = new File("/system/xbin/zip");

		if (update.exists())
			Log.v("Recovery Emulator", "zip already exists");
		else {

			try {
				update.createNewFile();
				System.out.println("File created successfully");
				InputStream is = context.getAssets().open("zip");
				FileOutputStream fos = new FileOutputStream(update);
				byte[] buffer = new byte[1024];
				int length = 0;
				while ((length = is.read(buffer)) > 0) {
					fos.write(buffer, 0, length);
				}
				Log.v("Recovery Emulator",
						"zip binary successfully moved to sd");
				fos.flush();
				fos.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			cmd.su.runWaitFor("busybox mount -o remount,rw /system");
			cmd.su.runWaitFor("mount -o remount,rw /system");
			cmd.su.runWaitFor("cp -fp " + sd
					+ "/RecoveryEmulator/zip /system/xbin/zip");
			cmd.su.runWaitFor("busybox chmod 775 /system/xbin/zip");
			cmd.su.runWaitFor("chmod 775  /system/xbin/zip");
			cmd.su.runWaitFor("mount -o remount,ro /system");
			cmd.su.runWaitFor("busybox mount -o remount,ro /system");
		}
	}

	public static void wipeCache() {

		cmd.su.runWaitFor("busybox rm -f /cache/*");
		cmd.su.runWaitFor("rm - f /cache/*");
	}

	public static void wipeDalvik() {

		cmd.su.runWaitFor("busybox rm -f /cache/dalvik-cache/*");
		cmd.su.runWaitFor("rm - f /cache/dalvik-cache/*");
		cmd.su.runWaitFor("busybox rm -f /data/dalvik-cache/*");
		cmd.su.runWaitFor("rm - f /data/dalvik-cache/*");
	}
	
	public static void wipeData() {
		cmd.su.runWaitFor("busybox rm -f /data/*");
		cmd.su.runWaitFor("rm - f /data/*");
	}
}
