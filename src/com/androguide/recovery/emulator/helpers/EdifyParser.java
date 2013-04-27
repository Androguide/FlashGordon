package com.androguide.recovery.emulator.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.os.Environment;
import android.util.Log;

import com.androguide.recovery.emulator.CMDProcessor;

public class EdifyParser {

	public static CMDProcessor cmd = new CMDProcessor();
	public static String updaterScript = "META-INF/com/google/android/updater-script";
	private static String file = "";
	private static String chained[] = { "" };
	public static String sd = Environment.getExternalStorageDirectory()
			.toString();
	public static String temp = sd + "/RecoveryEmulator/tmp";

	public static void interpreterAlgorithm(String curr) {

		// delete all ");"
		curr = curr.replaceAll("\\s+", " ");
		curr = curr.replaceAll("assert\\(", "");

		// symlink() parsing & translation
		if (curr.contains("symlink(")) {

			curr = curr.replaceAll(",", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			curr = curr.replaceAll("symlink\\(", "ln -s ");
			Log.v("Recovery Emulator", curr);
			cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
					+ "/flash_gordon.sh");

			// avoiding assert() lines
		} else if (curr.contains("getprop")) {
			// TODO: translate assert() with the getBuildPropValueOf("") method
			// from CMDProcessor

			// avoiding format() lines
		} else if (curr.contains("format(")) {
			// TODO: learn what the translation for format() would be in bash

			// Avoiding commented-out lines
		} else if (curr.contains("#")) {

			// Deleting useless show_progress() lines
		} else if (curr.contains("show_progress(")) {
			curr = curr.replaceAll(",", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			curr = curr.replaceAll("show_progress\\(.*", "");
			Log.v("Recovery Emulator", curr);
			cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
					+ "/flash_gordon.sh");

			// Deleting useless ui_print() lines
		} else if (curr.contains("ui_print(")) {
			curr = curr.replaceAll(",", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			curr = curr.replaceAll("ui_print\\(.*", "");
			Log.v("Recovery Emulator", curr);
			cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
					+ "/flash_gordon.sh");

			// package_extract_file() parsing & translation
		} else if (curr.contains("package_extract_file(")) {

			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			
			if (curr.contains("boot.img")) {
				curr = curr.replaceAll("package_extract_file\\(", "dd if="
						+ temp + "/");
				curr = curr.replaceAll(", ", " of=");

				Log.v("Recovery Emulator", curr);
				cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
						+ "/flash_gordon.sh");

			} else {
				curr = curr.replaceAll(",", "");

				curr = curr.replaceAll("package_extract_file\\(",
						"busybox cp -fp " + temp + "/");

				Log.v("Recovery Emulator", curr);
				cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
						+ "/flash_gordon.sh");
			}

			// package_extract_dir() parsing & translation
		} else if (curr.contains("package_extract_dir(")) {
			String original = curr;
			curr = curr.replace("package_extract_dir(\"", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			
			String arr[] = curr.split(", ");
			
			Log.v("Recovery Emulator", "mkdir -p " + arr[1]);
			cmd.su.runWaitFor("echo \"mkdir -p " + arr[1] + "\" >> " + temp
					+ "/flash_gordon.sh");
			
			
			original = original.replace("package_extract_dir(\"", "busybox cp -rfp "
					+ temp + "/");
			original = original.replaceAll("\", \"", "/* ");
			original = original.replaceAll(",", "");
			original = original.replaceAll("\"", "");
			original = original.replaceAll("\\)", "");
			original = original.replaceAll(";", "");
			Log.v("Recovery Emulator", original);
			cmd.su.runWaitFor("echo \"" + original + "\" >> " + temp
					+ "/flash_gordon.sh");

			// set_perm() parsing & translation
		} else if (curr.contains("set_perm(")) {
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			curr = curr.replaceAll("set_perm\\(", "");
			String[] array = curr.split(",\\s");

			Log.v("Recovery Emulator", "chown " + array[0] + ":" + array[1]
					+ " " + array[3]);
			cmd.su.runWaitFor("echo \"" + "chown " + array[0] + ":" + array[1]
					+ " " + array[3] + "\" >> " + temp + "/flash_gordon.sh");
			Log.v("Recovery Emulator", "chmod " + array[2] + " " + array[3]);
			cmd.su.runWaitFor("echo \"" + "chmod " + array[2] + " " + array[3]
					+ "\" >> " + temp + "/flash_gordon.sh");

			// set_perm_recursive() parsing & translation
		} else if (curr.contains("set_perm_recursive(")) {
			curr = curr.replaceAll("set_perm_recursive\\(", "");
			curr = curr.replaceAll("\"", "");
			String[] array = curr.split(",\\s");

			String[] path = array[4].split("/");

			Log.v("Recovery Emulator", "chown " + array[0] + ":" + array[1]
					+ " " + array[4]);
			cmd.su.runWaitFor("echo \"" + "chown " + array[0] + ":" + array[1]
					+ " " + array[4] + "\" >> " + temp + "/flash_gordon.sh");

			Log.v("Recovery Emulator", "chmod " + array[3] + " " + array[4]);
			cmd.su.runWaitFor("echo \"" + "chmod " + array[3] + " " + array[4]
					+ "\" >> " + temp + "/flash_gordon.sh");

			int a = 0;

			while (a < path.length) {
				if (a > 0 && a < (path.length - 1)) {

					Log.v("Recovery Emulator", "chown " + array[0] + ":"
							+ array[1] + " /" + path[a]);
					cmd.su.runWaitFor("echo \"" + "chown " + array[0] + ":"
							+ array[1] + " /" + path[a] + "\" >> " + temp
							+ "/flash_gordon.sh");

					Log.v("Recovery Emulator", "chmod " + array[3] + " /"
							+ path[a]);
					cmd.su.runWaitFor("echo \"" + "chmod " + array[3] + " /"
							+ path[a] + "\" >> " + temp + "/flash_gordon.sh");
				}
				a++;
			}

			// delete() parsing & translation
		} else if (curr.contains("delete(\"")) {
			curr = curr.replaceAll(",", "");
			curr = curr.replaceAll("\"", "");
			curr = curr.replaceAll("\\)", "");
			curr = curr.replaceAll(";", "");
			curr = curr.replaceAll("delete\\(", "busybox rm -f ");
			Log.v("Recovery Emulator", curr);
			cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
					+ "/flash_gordon.sh");

			// run_program() parsing & translation
		} else if (curr.contains("run_program(\"")) {

			if (curr.contains("/sbin/busybox")) {
				curr = curr.replaceAll("\\)", "");
				curr = curr.replaceAll(";", "");
				curr = curr.replaceAll("\"", "");
				curr = curr.replaceAll("run_program\\(/sbin/busybox",
						"busybox ");
				String arr[] = curr.split(",");
				int i = 0;
				String chain = "";
				while (i < arr.length) {
					chain = chain + arr[i];
					i++;
				}
				Log.v("Recovery Emulator", chain);
			} else {
				curr = curr.replaceAll(",", "");
				curr = curr.replaceAll("\"", "");
				curr = curr.replaceAll("\\)", "");
				curr = curr.replaceAll(";", "");
				curr = curr.replaceAll("run_program\\(", "sh ");
				Log.v("Recovery Emulator", curr);
				cmd.su.runWaitFor("echo \"" + curr + "\" >> " + temp
						+ "/flash_gordon.sh");
			}

			// mount() / unmount() parsing & translation
		} else if (curr.contains("mount(")) {
			if (curr.contains("/system")) {
				if (curr.contains("unmount(")) {
					// Log.v("Recovery Emulator",
					// "busybox mount -o ro,remount -t auto /system");
					// cmd.su.runWaitFor("echo \""+"busybox mount -o ro,remount -t auto /system");

				} else {

					Log.v("Recovery Emulator",
							"busybox mount -o rw,remount -t auto /system");
					cmd.su.runWaitFor("echo \""
							+ "busybox mount -o rw,remount -t auto /system"
							+ "\" >> " + temp + "/flash_gordon.sh");
				}
			}

			// write_raw_image() parsing & translation
		} else if (curr.contains("write_raw_image(")) {
			curr = curr.replaceAll("write_raw_image\\(", "dd if=");
			String[] arr = curr.split("\", \"");
			arr[0] = arr[0].replaceAll("\"", "");
			arr[1] = arr[1].replaceAll("\"", "");
			Log.v("Recovery Emulator", arr[0] + " of=" + arr[1]);
			cmd.su.runWaitFor("echo \"" + arr[0] + " of=" + arr[1] + "\" >> "
					+ temp + "/flash_gordon.sh");

		} else {
			Log.v("Recovery Emulator", curr);
		}

	}

	public static void loop() {
		file = file.replaceAll("\\),", ");");
		chained = file.split("\\);");
		chained = file.split("\\);");
		int i = 0;
		while (i < chained.length) {
			String curr = chained[i];
			interpreterAlgorithm(curr);
			i++;
		}
	}

	public static void noQuote(String line) {
		line = line.replaceAll("\"", "");
	}

	public static void noComma(String line) {
		line = line.replaceAll("[,]", "");
	}

	public static void noEnd(String line) {
		line = line.replaceAll("(\\);)", "");
	}

	public static void readUpdaterScript(String scriptLocation) {

		try {
			String line;
			Process process = Runtime.getRuntime().exec("su");
			OutputStream stdin = process.getOutputStream();
			InputStream stderr = process.getErrorStream();
			InputStream stdout = process.getInputStream();

			stdin.write(("cat " + scriptLocation + "\n").getBytes());
			stdin.write("exit\n".getBytes());
			stdin.flush();

			stdin.close();

			BufferedReader br = new BufferedReader(
					new InputStreamReader(stdout));
			while ((line = br.readLine()) != null) {
				file += line;
				// Log.d("[Output]", line);
				// Log.d("[Output Chained]", file);
				// interpreterAlgorithm(line);
			}
			loop();
			br.close();

			br = new BufferedReader(new InputStreamReader(stderr));
			while ((line = br.readLine()) != null) {
				// Log.e("[Error]", line);
			}
			br.close();

			process.waitFor();
			process.destroy();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
