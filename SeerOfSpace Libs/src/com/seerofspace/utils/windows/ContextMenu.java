package com.seerofspace.utils.windows;

import com.sun.jna.platform.win32.WinReg;

public class ContextMenu {
	
	public static final String DIRECTORY = "Directory";
	public static final String ALL_FILES = "*";
	private static final WinReg.HKEY HKEY = WinReg.HKEY_CLASSES_ROOT;
	private RegKey[][] keys;
	private String value;
	
	public ContextMenu(String name, String path, String... fileTypes) {
		value = surroundWithQuotes(path) + " " + surroundWithQuotes("%1");
		initKeys(fileTypes, name);
	}
	
	private void initKeys(String[] fileTypes, String name) {
		keys = new RegKey[fileTypes.length][4];
		for (int i = 0; i < fileTypes.length; i++) {
			keys[i][0] = new RegKey(HKEY, fileTypes[i]);
			keys[i][1] = new RegKey(keys[i][0], "shell");
			keys[i][2] = new RegKey(keys[i][1], name);
			keys[i][3] = new RegKey(keys[i][2], "command");
		}
	}
	
	public void enable() {
		for (int i = 0; i < keys.length; i++) {
			for (int j = 0; j < keys[i].length; j++) {
				if (!keys[i][j].exists() && !keys[i][j].create()) {
					throw new RuntimeException("Error creating key: " + keys[i][j].getFullPath());
				}
			}
			keys[i][3].setStringValue("", value);
		}
	}
	
	public void remove() {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i][3].exists() && keys[i][3].valueExists("")) {
				keys[i][3].removeValue("");
			}
			for (int j = keys[i].length - 1; j >= 0; --j) {
				if (keys[i][j].exists() && keys[i][j].isEmpty()) {
					keys[i][j].delete();
				}
			}
		}
	}
	
	public boolean isEnabled() {
		for (int i = 0; i < keys.length; i++) {
			if (!keys[i][3].exists()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean needsUpdate() {
		for (int i = 0; i < keys.length; i++) {
			if (keys[i][3].valueExists("") && !keys[i][3].getStringValue("").equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	public void update() {
		for (int i = 0; i < keys.length; i++) {
			keys[i][3].setStringValue("", value);
		}
	}
	
	public void autoManage() {
		if (!isEnabled()) {
			enable();
		} else if (needsUpdate()) {
			update();
		}
	}
	
	private String surroundWithQuotes(String s) {
		return '\"' + s + '\"';
	}
	
}
