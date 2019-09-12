package com.seerofspace.utils.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Advapi32X extends StdCallLibrary
{
	public static final Advapi32X INSTANCE = (Advapi32X)Native.loadLibrary("advapi32", Advapi32X.class, W32APIOptions.UNICODE_OPTIONS);
	
	int RegNotifyChangeKeyValue(HKEY hkey, boolean bWatchSubtree, int dwNotifyFilter, HANDLE hEvent, boolean fAsynchronous);
}
