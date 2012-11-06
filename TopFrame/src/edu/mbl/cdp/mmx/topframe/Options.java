/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mbl.cdp.mmx.topframe;

import java.util.prefs.Preferences;

/**
 *
 * @author GBH
 */
public class Options {

	static Preferences prefs;
	private boolean showMM = false;
	private boolean showIJ = false;
	private boolean addRunButton = false;
	private String scriptFile = "";
	private boolean exitOnClose = false;

	public Options(Preferences mainPrefs_) {
		this.prefs = mainPrefs_;
		showMM = prefs.getBoolean("showMM", true);
		showIJ = prefs.getBoolean("showIJ", false);
		addRunButton = prefs.getBoolean("addRunButton", true);
		scriptFile = prefs.get("scriptFile", "");
		exitOnClose = prefs.getBoolean("exitOnClose", false);
	}

	public boolean isShowMM() {
		return showMM;
	}

	public void setShowMM(boolean showMM) {
		prefs.putBoolean("showMM", showMM);
		this.showMM = showMM;
	}

	public boolean isShowIJ() {
		return showIJ;
	}

	public void setShowIJ(boolean showIJ) {
		prefs.putBoolean("showIJ", showIJ);
		this.showIJ = showIJ;
	}

	public boolean isAddRunButton() {
		return addRunButton;
	}

	public void setAddRunButton(boolean addRunButton) {
		prefs.putBoolean("addRunButton", addRunButton);
		this.addRunButton = addRunButton;
	}

	public String getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) {
		prefs.put("scriptFile", scriptFile);
		this.scriptFile = scriptFile;
	}

	public boolean isExitOnClose() {
		return exitOnClose;
	}

	public void setExitOnClose(boolean exitOnClose) {
		prefs.putBoolean("exitOnClose", exitOnClose);
		this.exitOnClose = exitOnClose;
	}

}
