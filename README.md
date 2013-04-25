FlashGordon
===========

Edify to Bash parser and translator, in the form of an Android app allowing to flash zips without using the recovery

## What is Flash Gordon ?
In laymen's words, Flash Gordon is an apk which allows you to flash any flashable zip (except for Roms) without having a recovery installed.
You just point it to the zip you want to flash, (be it a kernel, a mod, gapps...) and it will interpret the updater-script of this package and install its content just like a recovery would.

## How does it work ?
Here is what happens step by step :

* You tap the "select a zip" button, select the built-in file explorer to select the zip you want to flash
* Flash Gordon will first extract this zip, depending on its size it can take a little time
* When you press the "Flash Now" button, Flash Gordon will locate the updater-script and start translating it into shell code, line by line
* It will output the resulting translated bash script on your sdcard and execute it, thus installing the zip's content
* The app creates a notification, just tap on it to reboot

In order to achieve those last 2 steps, I'm writing a full Edify to Bash parser and translator.

Here is the list of all the Edify commands this parser can currently process and what their respective bash translation is (under each edify command), using examples:
(more will be added over time, this is really just a draft)

*By no means am I a bash expert, so if you have suggestions for better bash equivalents of any edify commands please post them*

    package_extract_file("path/to/source/file.txt", "/path/to/destination/file.txt");
    busybox cp -fp /path/to/source/file.txt /path/to/destination/file.txt

-

    package_extract_dir("/source/folder", "/destination/folder");
    busybox cp -rfp /source/folder/* /destination/folder
    
-

    set_perm(0, 2000, 0755, "/file/or/folder");
    chown 0:2000 /file/or/folder
    chmod 0755 /file/or/folder

-

    set_perm_recursive(0, 2000, 0644, 0755, "/path/to/set/permissions/recursively);
    chown 0:2000 /path/to/set/permissions/recursively
    chown 0:2000 /path/to/set/permissions
    chown 0:2000 /path/to/set
    chown 0:2000 /path/to
    chown 0:2000 /path
    chmod 0644 /path/to/set/permissions/recursively
    chmod 0755 /path/to/set/permissions
    chmod 0755 /path/to/set
    chmod 0755 /path/to
    chmod 0755 /path

-

    delete("file/to/delete");
    busybox rm -f /file/to/delete

-

    run_program("/script/to/run.sh");
    sh /script/to/run.sh

-

    mount("ext4", "EMMC", "/dev/block/mmcblk0p9", "/system");
    busybox mount -o rw,remount -t auto /system (will soon change to -t mmcblk0p9 or whichever mount point is in the updater-script)

-

    unmount("/system");
    busybox mount -o remount,ro /system

-

    symlink("busybox", "/system/xbin/[", "/system/xbin/[[",
        "/system/xbin/adjtimex", "/system/xbin/arp", "/system/xbin/ash",
        "/system/xbin/awk", "/system/xbin/base64", "/system/xbin/basename",
        "/system/xbin/bbconfig", "/system/xbin/blkid", "/system/xbin/blockdev",
        "/system/xbin/brctl", "/system/xbin/bunzip2", "/system/xbin/bzcat",
        "/system/xbin/bzip2", "/system/xbin/cal", "/system/xbin/cat",
        "/system/xbin/catv", "/system/xbin/chattr", "/system/xbin/chgrp",
        "/system/xbin/chmod", "/system/xbin/chown", "/system/xbin/chroot",
        "/system/xbin/clear", "/system/xbin/cmp", "/system/xbin/comm",
        "/as/many/lines/as/you/want");
        
-

    ln -s busybox  /system/xbin/[ /system/xbin/[[
        /system/xbin/adjtimex /system/xbin/arp /system/xbin/ash
        /system/xbin/awk /system/xbin/base64 /system/xbin/basename
        /system/xbin/bbconfig /system/xbin/blkid /system/xbin/blockdev
        /system/xbin/brctl /system/xbin/bunzip2 /system/xbin/bzcat
        /system/xbin/bzip2 /system/xbin/cal /system/xbin/cat
        /system/xbin/catv /system/xbin/chattr /system/xbin/chgrp
        /system/xbin/chmod /system/xbin/chown /system/xbin/chroot
        /system/xbin/clear /system/xbin/cmp /system/xbin/comm
        /as/many/lines/as/you/want

-

    write_raw_image("/tmp/boot.img", "mmcblk0p5");
    dd if=/tmp/boot.img of=/dev/block/mmcblk0p5
    

## Dependencies
This app depends on the following open source libraries: 

* [My fork of of the CardsUI library by Nadav Fima](https://github.com/Androguide/cardsui-for-android)
* [aFileChooser by Paul Burke](https://github.com/iPaulPro/aFileChooser)


## Contribute
You can discuss about this project on xda-developers.com at the following threads :

* [General Thread](http://forum.xda-developers.com/showthread.php?t=2250555)
* [Development Thread](http://forum.xda-developers.com/showthread.php?t=2250632)

If you want to contribute, please make a pull request or post your suggestions on the xda thread.


## Screenshots
![1](http://imageshack.us/a/img259/3427/flash2r.png) ![2](http://imageshack.us/a/img841/1894/flash3n.png) ![3](http://imageshack.us/a/img818/1431/flash4.png)
![4](http://imageshack.us/a/img707/4690/flash5t.png) ![5](http://imageshack.us/a/img841/1416/flash6.png) ![6](http://imageshack.us/a/img10/4111/flash8q.png)
![7](http://imageshack.us/a/img690/8149/flash1bz.png) ![8](http://img59.imageshack.us/img59/5444/flashib.png)
