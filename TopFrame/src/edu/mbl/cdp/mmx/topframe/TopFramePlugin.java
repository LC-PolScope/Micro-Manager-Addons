package edu.mbl.cdp.mmx.topframe;


import mmcorej.CMMCore;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

/**
 * A frame which is AlwaysOnTopFrame and contains often used settings and actions. Features: - 'Show' makes the
 * MMStudioMainFrame visible, as well as any other registered JFrames. - Remembers its position and size on the desktop.
 * - Camera panel allows toggling Live mode and changing exposure - 
 * Hotkeys for increasing (F12) and decreasing (F11)
 * exposure - Closing the ToolFrame closes MMgr and ImageJ.
 *
 * - SomeAction is included as a template for creating an action
 *
 * @author GBH
 */
public class TopFramePlugin implements MMPlugin {
	
	public static final String menuName = "TopFrame";
	public static final String tooltipDescription =
			"Frame that is always on top, with exposure control, live mode toggle, and action buttons";
	//-----------------------------------------------------------
	// MMPlugin implementation ...
	// If launched as a MMgr plugin
	static ScriptInterface app;
	static CMMCore core;
	//FakeCore core = new FakeCore();

	@Override
	public void setApp(ScriptInterface si) {
		app = si;
		core = app.getMMCore(); 
		TopFrame.setApp(app);
		TopFrame.setCore(core);
	}
	
	@Override
	public void show() {
		TopFrame.constructTopFrame();
		TopFrame.addFrameToShow((MMStudioMainFrame) app);
	}
	
	@Override
	public void configurationChanged() {
	}
	
	@Override
	public String getDescription() {
		return "ToolFrame";
	}
	
	@Override
	public String getInfo() {
		return "ToolFrame";
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public String getCopyright() {
		return "MBL, 2012";
	}
	
	@Override
	public void dispose() {
	}
	
}
