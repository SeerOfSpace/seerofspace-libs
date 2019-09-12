package com.seerofspace.utils.windows;

import java.util.function.BooleanSupplier;

import com.seerofspace.utils.FileUtils;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;

public class RegKey
{
	private HKEY hkey;
	private String key;
	
	public RegKey(HKEY hkey, String key) {
		this.hkey = hkey;
		this.key = key;
	}
	
	public RegKey(RegKey parent, String child) {
		this.hkey = parent.hkey;
		this.key = parent.key + "\\" + child;
	}
	
	public HKEY getHKEY() {
		return hkey;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getFullPath() {
		return hkey.toString() + "\\" + key;
	}
	
	public boolean create() {
		return Advapi32Util.registryCreateKey(hkey, key);
	}
	
	public void delete() {
		Advapi32Util.registryDeleteKey(hkey, key);
	}
	
	public void setStringValue(String name, String value) {
		Advapi32Util.registrySetStringValue(hkey, key, name, value);
	}
	
	public String getStringValue(String name) {
		return Advapi32Util.registryGetStringValue(hkey, key, name);
	}
	
	public void removeValue(String name) {
		Advapi32Util.registryDeleteValue(hkey, key, name);
	}
	
	public boolean exists() {
		return Advapi32Util.registryKeyExists(hkey, key);
	}
	
	public boolean valueExists(String name) {
		return Advapi32Util.registryValueExists(hkey, key, name);
	}
	
	public boolean isEmpty() {
		if(exists()) {
			if(Advapi32Util.registryGetValues(hkey, key).size() == 0 && Advapi32Util.registryGetKeys(hkey, key).length == 0) {
				return true;
			}
		}
		return false;
	}
	
	public RegKey getParent() {
		return new RegKey(hkey, FileUtils.getParent(key));
	}
	
	public void notifyRegChanges(Object sharedLock) {
		notifyRegChanges(sharedLock, () -> true);
	}
	
	public void notifyRegChanges(Object sharedLock, BooleanSupplier condition) {
		HKEYByReference phkResult = new HKEYByReference();
		int result = Advapi32.INSTANCE.RegOpenKeyEx(hkey, key, 0, WinNT.KEY_NOTIFY, phkResult);
		if (result != 0) {
			throw new RuntimeException(Kernel32Util.formatMessageFromLastErrorCode(result));
		}
		do {
			result = Advapi32X.INSTANCE.RegNotifyChangeKeyValue(phkResult.getValue(), false, WinNT.REG_NOTIFY_CHANGE_NAME, null, false);
			if (result != 0) {
				throw new RuntimeException(Kernel32Util.formatMessageFromLastErrorCode(result));
			}
		} while (!condition.getAsBoolean());
		Advapi32.INSTANCE.RegCloseKey(phkResult.getValue());
		synchronized (sharedLock) {
			sharedLock.notify();
		}
	}
}
