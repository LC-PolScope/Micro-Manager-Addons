package edu.mbl.cdp.mmx.topframe;

import bsh.EvalError;
import bsh.Interpreter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import mmcorej.CMMCore;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.AcquisitionEngine;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.TextUtils;
import org.micromanager.utils.WaitDialog;

/**
 *
 * @author GBH
 */
public class RunScriptAction
		extends AbstractAction {
	private final Options options;

	public RunScriptAction(Options options) {
		this.options = options;
		putValue(NAME, "Run");
		putValue(ACTION_COMMAND_KEY, "runscript");
		try {
			putValue(SMALL_ICON,new javax.swing.ImageIcon(getClass().getResource("go.gif")));
		} catch (Exception ex) {
		}
		putValue(SHORT_DESCRIPTION, "Run selected Beanshell script <Ctrl-R>");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK, false));
	}

	public void actionPerformed(ActionEvent ae) {
		// @todo alternate Classpath & working dir
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String script = options.getScriptFile();
				System.out.println("Run script: " + script);
				if(script!=null && !script.isEmpty()) {
				   executeScript(script);
				}
			}

		});

	}
	// For running scripts...

	private static final String SCRIPT_CORE_OBJECT = "mmc";
	private static final String SCRIPT_ACQENG_OBJECT = "acq";
	private static final String SCRIPT_GUI_OBJECT = "gui";

	private void executeScript(String scriptFile_) {
		// execute startup script
		File f = new File(scriptFile_);
		
		CMMCore core_ = null;
		AcquisitionEngine engine_ = null;
		MMStudioMainFrame gui_ = null;
		try {
			core_ = MMStudioMainFrame.getInstance().getCore();
			engine_ = MMStudioMainFrame.getInstance().getAcquisitionEngine();
			gui_ = MMStudioMainFrame.getInstance();
		} catch (Exception e) {
			return;
		}
		if(core_==null || engine_==null || gui_==null) return;
		if (scriptFile_.length() > 0 && f.exists()) {
			WaitDialog waitDlg = new WaitDialog(
					"Executing script, please wait...");
			waitDlg.showDialog();
			Interpreter interp = new Interpreter();
			try {
				// insert core object only
				interp.set(SCRIPT_CORE_OBJECT, core_);
				interp.set(SCRIPT_ACQENG_OBJECT, engine_);
				interp.set(SCRIPT_GUI_OBJECT, gui_);

				// read text file and evaluate
				interp.eval(TextUtils.readTextFile(scriptFile_));
			} catch (IOException exc) {
				ReportingUtils.showError(exc, "Unable to read the script (" + scriptFile_ + ").");
			} catch (EvalError exc) {
				ReportingUtils.showError(exc, "Script Evaluation Error");
			} finally {
				waitDlg.closeDialog();
			}
		} else {
			if (scriptFile_.length() > 0) {
				ReportingUtils.logMessage("Script file (" + scriptFile_ + ") not present.");
			}
		}
	}

}