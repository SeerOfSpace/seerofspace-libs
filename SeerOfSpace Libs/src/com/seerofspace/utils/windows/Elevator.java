package com.seerofspace.utils.windows;

import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinNT.TOKEN_INFORMATION_CLASS;
import com.sun.jna.ptr.IntByReference;

public class Elevator {
	
	public static void executeAsAdministrator(String command, String args) {
		Shell32X.SHELLEXECUTEINFO execInfo = new Shell32X.SHELLEXECUTEINFO();
		execInfo.lpFile = new WString(command);
		if (args != null) {
			execInfo.lpParameters = new WString(args);
		}
		execInfo.nShow = Shell32X.SW_SHOWDEFAULT;
		execInfo.fMask = Shell32X.SEE_MASK_NOCLOSEPROCESS;
		execInfo.lpVerb = new WString("runas");
		boolean result = Shell32X.INSTANCE.ShellExecuteEx(execInfo);

		if (!result) {
			int lastError = Kernel32.INSTANCE.GetLastError();
			String errorMessage = Kernel32Util.formatMessageFromLastErrorCode(lastError);
			throw new RuntimeException("Error performing elevation: " + lastError + ": " + errorMessage + " (apperror=" + execInfo.hInstApp + ")");
		}
	}
	
	public static boolean isElevated() {
		HANDLEByReference htoken = new HANDLEByReference();
		boolean result = Advapi32.INSTANCE.OpenProcessToken(
				Kernel32.INSTANCE.GetCurrentProcess(), 
				WinNT.TOKEN_QUERY, 
				htoken
		);
		if (!result) {
			throw new RuntimeException("Error opening process token");
		}
		TOKEN_ELEVATION_STRUCTURE tokenElevationStructure = new TOKEN_ELEVATION_STRUCTURE();
		IntByReference returnLength = new IntByReference();
		result = Advapi32.INSTANCE.GetTokenInformation(
				htoken.getValue(), 
				TOKEN_INFORMATION_CLASS.TokenElevation, 
				tokenElevationStructure, 
				tokenElevationStructure.size(), 
				returnLength
		);
		Kernel32.INSTANCE.CloseHandle(htoken.getValue());
		if (!result) {
			throw new RuntimeException("Error getting token information");
		}
		return tokenElevationStructure.TokenIsElevated != 0;
	}

	private static class TOKEN_ELEVATION_STRUCTURE extends Structure {
		public int TokenIsElevated;
	}
	
}
