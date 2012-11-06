package edu.mbl.cdp.mmx.topframe;

//import com.swtdesigner.SwingResourceManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mmcorej.CMMCore;
import mmcorej.MMCoreJ;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.MMListenerAdapter;
import org.micromanager.utils.ReportingUtils;

/**
 * A frame which is AlwaysOnTopFrame and contains often used settings and actions. Features: - 'Show' makes the
 * MMStudioMainFrame visible, as well as any other registered JFrames. - Remembers its position and size on the desktop.
 * - Camera panel allows toggling Live mode and changing exposure - Hotkeys for increasing (F12) and decreasing (F11)
 * exposure - Closing the TopFrame closes MMgr and ImageJ.
 *
 * - RunScriptAction is included as a template for creating an action
 *
 * To enable another plugin to register with TopFrame without having a dependency, i.e. only if TopFrame.jar is
 * available:
 *
 * <code>
 * private void registerFrameWithTopFrame(JFrame controlFrame_) {
 * // call static TopFrame.addFrameToShow(JFrame frame)
 * try {
 * ClassLoader l = Thread.currentThread().getContextClassLoader();
 * Class cls = l.loadClass("TopFrame");
 * Method mainMethod = cls.getDeclaredMethod("addFrameToShow", new Class[]{JFrame.class});
 * mainMethod.invoke(null, new Object[]{controlFrame_});
 * } catch (Exception ex) {
 * ex.printStackTrace();
 * }
 * }
 *
 * private void registerActionWithTopFrame(Action action) {
 * // call static TopFrame.addActionButton(Action action)
 * try {
 * ClassLoader l = Thread.currentThread().getContextClassLoader();
 * Class cls = l.loadClass("TopFrame");
 * Method mainMethod = cls.getDeclaredMethod("addActionButton", new Class[]{Action.class});
 * mainMethod.invoke(null, new Object[]{action});
 * } catch (Exception ex) {
 * ex.printStackTrace();
 * }
 * }
 *
 * TODO: - Prevent multiple copies of a button/action to be added to TopFrame - Allow removal of a button
 *  October, 2012
 * @author GBH
 */
public class TopFrame {

	private static JFrame topFrame;
	private static JPanel container;
	// Buttons...
	private static JPanel addedButtonsContainer;
	private static ArrayList<Action> actions = new ArrayList<Action>();
	private static ArrayList<JButton> buttons = new ArrayList<JButton>();
	//
	static ExposureSpinner exposureSpinner;
	// MM..
	private static MMStudioMainFrame MMframe_;
	private static ScriptInterface app;
	private static CMMCore core;
	// *** private static PropListener propListener = new PropListener();  // MMListener
	// TODO: test for camera (and add a restart of TopFrame.)
	private static boolean cameraPresent = true;
	//
	private static Preferences prefs = Preferences.userRoot().node("TopFrame");
	private static Options options = new Options(prefs);
	//private static Color backColor = new Color(136, 139, 189);
	private static Color backColor = new Color(177, 204, 215);

	public static void setApp(ScriptInterface app_) {
		app = app_;
	}

	public static void setCore(CMMCore core_) {
		TopFrame.core = core_;
	}

	public static JFrame getTopFrame() {
		return topFrame;
	}

	public static void init() {
		MMframe_ = MMStudioMainFrame.getInstance();
		if (MMframe_ == null) {
			app = (ScriptInterface) MMframe_;
			MMframe_.getCore();
			MMframe_.getAcquisitionEngine();
		}
	}

	public static void constructTopFrame() {
		try {
			// Create TopFrame (AlwaysOnTopFrame)
			topFrame = new JFrame();
			topFrame.setTitle("TopFrame");
			// setlocation - top or bottom of screen
			int x = prefs.getInt("x", 48);
			int y = prefs.getInt("y", 4);
			int width = prefs.getInt("w", 400);
			int height = prefs.getInt("h", 100);
			boolean exitOnClose = prefs.getBoolean("exitOnClose", false);
			topFrame.setBounds(x, y, width, height);
			topFrame.setAlwaysOnTop(true);
			// add container
			container = new JPanel();
			container.setBackground(backColor);
			container.setPreferredSize(new Dimension(width - 8, height - 24));  // change this...
			container.setAlignmentX(Component.LEFT_ALIGNMENT);
			container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
			container.add(Box.createRigidArea(new Dimension(5, 0)));
			container.add(createShowButton());
			container.add(Box.createRigidArea(new Dimension(5, 0)));
			container.add(createOptionsButton());
			container.add(Box.createRigidArea(new Dimension(5, 0)));
			if (cameraPresent) {
				JPanel cameraPanel = constructCameraPanel();
				container.add(cameraPanel);
				container.add(Box.createRigidArea(new Dimension(5, 0)));
			}
			//container.add(Box.createHorizontalGlue());
			// Add panel to contain action buttons
			addedButtonsContainer = new JPanel();
			addedButtonsContainer.setBackground(backColor);
			addedButtonsContainer.setLayout(new BoxLayout(addedButtonsContainer, BoxLayout.X_AXIS));
			//addedButtonsContainer.setPreferredSize(new Dimension(128, 48));
			container.add(addedButtonsContainer);

//		container.add(Box.createHorizontalGlue());
//		StatusPanel statusPanel = new StatusPanel();
//		container.add(statusPanel);
			topFrame.setContentPane(container);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ImageIcon img = null;
		try {
			img = new ImageIcon(TopFrame.class.getClassLoader().getResource(
					"edu/mbl/cdp/mmx/topframe/frameIcon.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (img != null) {
			topFrame.setIconImage(img.getImage());
		}
		// Add listener to save bounds
		topFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				saveBounds();
			}

			public void componentMoved(ComponentEvent e) {
				saveBounds();
			}

		});
		if (options.isAddRunButton()) {
			addActionButton(new RunScriptAction(options));
		}
		//toolFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		//if (options.isExitOnClose()) {
		addExitOnCloseListener();
//		}
		topFrame.setVisible(true);
	}

	private static void saveBounds() {
		Rectangle r = topFrame.getBounds();
		prefs.putInt("x", r.x);
		prefs.putInt("y", r.y);
		prefs.putInt("w", r.width);
		prefs.putInt("h", r.height);
	}

	//-----------------------------------------------------------
	// Options Dialog...
	private static JButton createOptionsButton() {
		JButton options = new JButton();
		options.setFont(new Font("sansserif", Font.BOLD, 10));
		options.setMargin(new Insets(2, 2, 2, 2));
		options.setMinimumSize(new Dimension(48, 48));
		options.setMaximumSize(new Dimension(48, 48));
		ImageIcon img = null;
		try {
			img = new ImageIcon(TopFrame.class.getClassLoader().getResource(
					"edu/mbl/cdp/mmx/topframe/prefs24.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (img
				!= null) {
			options.setIcon(img);
		}
		options.setToolTipText("Options");
		options.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openOptionsDialog();
			}

		});
		//options.setVisible(false); // TODO set to hide for distribution package
		return options;
	}

	private static void openOptionsDialog() {
		OptionsDialog d22 = new OptionsDialog((JFrame) topFrame, options, true);
		//d22.setBounds(200, 232, 300, 200);

		d22.addWindowListener(closeWindow);
		d22.setVisible(true);
	}

	private static WindowListener closeWindow = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
			e.getWindow().dispose();
		}

	};

	//------------------------------------------------------------------------------------------------
	// Methods called by clients
	//
	public static void removeActionButton(Action action) {
		for (Action act : actions) {
			if (act.equals(action)) {
				for (JButton button : buttons) {
					if (button.getAction().equals(act)) {
						addedButtonsContainer.remove(button);
					}
				}
			}
		}
	}

	public static void addActionButton(Action action) {
		if (actions.contains(action)) {
			return;  // already added.
		}
		try {
			JButton button = new JButton(action);

			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setVerticalTextPosition(SwingConstants.BOTTOM);
			button.setFont(new Font("sansserif", Font.BOLD, 11));
			//button.setIconTextGap(8);
			//button.setPreferredSize(new Dimension(32,32));
			//button.setMinimumSize(new Dimension(32, 32));
			//button.setMaximumSize(new Dimension(128, 32));

			addedButtonsContainer.add(button);
			addedButtonsContainer.add(Box.createRigidArea(new Dimension(5, 0)));
			//Dimension dim = addedButtonsContainer.getPreferredSize();
			//addedButtonsContainer.setPreferredSize(new Dimension(dim.width + button.getSize().width+5, dim.height));
			KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
			if (keyStroke != null) {
				addGlobalHotKeyBinding(keyStroke, action);
			}
			String tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
			if (tooltip != null) {
				button.setToolTipText(tooltip);
			}

			actions.add(action);
			buttons.add(button);
			topFrame.pack();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public static void addGlobalHotKeyBinding(KeyStroke keyStroke, Action action) {
		String actionKey = (String) action.getValue(Action.NAME);
		GlobalHotkeyManager hotkeyManager = GlobalHotkeyManager.getInstance();
		hotkeyManager.getInputMap().put(keyStroke, actionKey);
		hotkeyManager.getActionMap().put(actionKey, action);
	}

	// For showing (making visible) other frames
	private static List<JFrame> showFrames = new ArrayList<JFrame>();

	public static void addFrameToShow(JFrame frame) {
		showFrames.add(frame);
	}

	private static JButton createShowButton() {
		JButton show = new JButton();
		show.setFont(new Font("sansserif", Font.BOLD, 12));
		show.setMargin(new Insets(2, 2, 2, 2));
		//show.setPreferredSize(new Dimension(32,32));
		show.setMinimumSize(new Dimension(48, 48));
		show.setMaximumSize(new Dimension(48, 48));
		show.setToolTipText("Show: Bring special windows to top");
		ImageIcon img = null;
		try {
			img = new ImageIcon(TopFrame.class.getClassLoader().getResource("edu/mbl/cdp/mmx/topframe/show.gif"));
			if (img != null) {
				show.setIcon(img);
			}
		} catch (Exception e) {
			show.setText("Show");
			e.printStackTrace();
		}
		show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JFrame frame : showFrames) {
					if (frame != null) {
						frame.setVisible(true);
					}
				}
				//topFrame.setVisible(true);
				if (options.isShowMM() && MMframe_ != null) {
					MMframe_.setVisible(true);
				}
				if (options.isShowIJ() && ij.IJ.getInstance() != null) {
					ij.IJ.getInstance().setVisible(true);
				}
			}

		});
		return show;
	}

	//------------------------------------------------------------------------------------------------
	// MM Camera-relate Panel with Live toggle and exposure control
	private static JPanel constructCameraPanel() {
		// Add Camera panel 
		// containing Live togglebutton, exposure spinner, hotkeys
		JPanel cameraPanel = new JPanel();
		cameraPanel.setBackground(backColor);
		cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.X_AXIS));
		//cameraPanel.setBackground(Color.getHSBColor(.4f, 0.1f, 0.8f));
		cameraPanel.setPreferredSize(new Dimension(132, 54));
		TitledBorder border = new TitledBorder(new LineBorder(Color.gray, 1), "Camera");
		border.setTitleFont(new Font("sansserif", Font.BOLD, 11));
		cameraPanel.setBorder(border);
		//cameraPanel.setSize(new Dimension(132, 54));
		cameraPanel.add(Box.createRigidArea(new Dimension(5, 42)));
		JToggleButton live = createLiveToggleButton();
		cameraPanel.add(live);
		cameraPanel.add(Box.createRigidArea(new Dimension(5, 42)));
		exposureSpinner = new ExposureSpinner(0.050, 60000.0, 5.0);
		exposureSpinner.setToolTipText("Exposure (msec) [F11,F12]");
		addExposureHotKeys();
		cameraPanel.add(exposureSpinner);
		cameraPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		// TODO add sync with MM...
		//		if (app != null) {
		//			app.addMMListener(propListener);
		//		}
		return cameraPanel;
	}

	private static JToggleButton createLiveToggleButton() {
		final JToggleButton live = new JToggleButton("Live");
		live.setFont(new Font("sansserif", Font.BOLD, 12));
		live.setMargin(new Insets(2, 2, 2, 2));
		live.setToolTipText("Toggle Live Mode");
		live.setMinimumSize(new Dimension(48, 32));
		live.setMaximumSize(new Dimension(48, 32));
		live.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (app != null) {
					app.enableLiveMode(live.isSelected());
				}
			}

		});
		return live;
	}

	private static void addExposureHotKeys() {
		Action increase = new IncreaseExposureAction();
		KeyStroke incrKey = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		addGlobalHotKeyBinding(incrKey, increase);

		Action decrease = new DecreaseExposureAction();
		KeyStroke decrKey = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		addGlobalHotKeyBinding(decrKey, decrease);


	}

	static class ExposureSpinner extends JSpinner {

		public SpinnerNumberModel modelSpin;
		private JSpinner.NumberEditor editorSpin;

		// TODO - get min/max from core...
		ExposureSpinner(double minimum, double maximum, double stepSize) {
			super();
			double exposure = 10;
			if (core != null) {
				try {
					exposure = core.getExposure();
				} catch (Exception ex) {
					Logger.getLogger(TopFrame.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			modelSpin = new SpinnerNumberModel((float) exposure, minimum, maximum, stepSize);
			super.setModel(modelSpin);
			editorSpin = new JSpinner.NumberEditor(this, "0.00");
			setEditor(editorSpin);
			setExpSpinStep(1);
			//setBounds(new Rectangle(128, 13, 65, 24));
			setMaximumSize(new Dimension(60, 28));
			setMinimumSize(new Dimension(60, 28));
			setPreferredSize(new Dimension(60, 28));
			ChangeListener listener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					SpinnerModel source = (SpinnerModel) e.getSource();
					String inStr = String.valueOf(source.getValue());
					float value = 0;
					try {
						value = Float.parseFloat(inStr);
						try {
							setExposure(value);

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} catch (NumberFormatException nfe) {
					}
					setExpSpinStep(value);
				}

			};
			modelSpin.addChangeListener(listener);
		}

		void setExpSpinStep(float value) {
			// update the step
			String decFormat = "0";
			if ((value >= 0) && (value < 0.5)) {
				modelSpin.setStepSize(new Double(0.01));
				decFormat = "0.00";
			}
			if ((value >= 0.5) && (value < 2)) {
				modelSpin.setStepSize(new Double(0.1));
				decFormat = "0.00";
			}
			if ((value >= 2) && (value < 10)) {
				modelSpin.setStepSize(new Double(1));
				decFormat = "0.0";
			}
			if ((value >= 10) && (value < 20)) {
				modelSpin.setStepSize(new Double(2));
				decFormat = "0.0";
			}
			if ((value >= 20) && (value < 100)) {
				modelSpin.setStepSize(new Double(5));
				decFormat = "0.";
			}
			if ((value >= 100) && (value < 1000)) {
				modelSpin.setStepSize(new Double(10));
				decFormat = "0.";
			}
			if ((value >= 1000)) {
				modelSpin.setStepSize(new Double(100));
				decFormat = "0.";
			}
			// update the format
			((JSpinner.NumberEditor) this.getEditor()).getFormat().applyPattern(decFormat);
			//spin_Expos.setValue(new Double((double) value));
		}

		private void setExposure(float exposure) {
			try {
				// TODO add check for camera...
				boolean wasLive = app.isLiveModeOn();
				if (wasLive) {
					app.enableLiveMode(false);
				}
				//core.setExposure(exposure);
				setExposureProperty(exposure);
				if (wasLive) {
					app.enableLiveMode(true);
				}
			} catch (Exception exp) {
				// Do nothing.
			}
		}

	}

	public static class IncreaseExposureAction extends AbstractAction {

		public IncreaseExposureAction() {
			putValue(NAME, "increaseExposure");
			putValue(Action.ACTION_COMMAND_KEY, "increaseExposure");
		}

		public void actionPerformed(ActionEvent ae) {
			System.out.println("Increase exposure...");
			changeExposure(true);
		}

	}

	public static class DecreaseExposureAction extends AbstractAction {

		public DecreaseExposureAction() {
			putValue(NAME, "decreaseExposure");
			putValue(Action.ACTION_COMMAND_KEY, "decreaseExposure");
		}

		public void actionPerformed(ActionEvent ae) {
			System.out.println("Decrease exposure...");
			changeExposure(false);
		}

	}

	static void changeExposure(boolean increase) {
		SpinnerNumberModel model = ((SpinnerNumberModel) exposureSpinner.getModel());

		float step = model.getStepSize().floatValue();
		String inStr = String.valueOf(model.getValue());
		float current = 0;
		try {
			current = Float.parseFloat(inStr);
		} catch (Exception ex) {
		}
		float newValue;
		if (increase) {
			newValue = current + step;
		} else {
			newValue = current - step;
		}
		model.setValue(newValue);
	}

	public static void setExposureProperty(double exposure) {
		try {
			String cameraLabel_ = core.getCameraDevice();
			core.setProperty(cameraLabel_, MMCoreJ.getG_Keyword_Exposure(), exposure);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	static class PropListener extends MMListenerAdapter {

		// update exposure value if changed from elsewhere...
		@Override
		public void propertiesChangedAlert() {
//		// avoid re-executing a refresh because of callbacks while we are updating
			System.out.println("propertiesChangedAlert");
			ReportingUtils.logMessage("propertiesChangedAlert");
		}

		@Override
		public void propertyChangedAlert(String device, String property, String value) {
			if (device.equalsIgnoreCase("camera")
					&& property.equalsIgnoreCase("exposure")) {
				System.out.println("propertyChangedAlert");
				ReportingUtils.logMessage("propertyChangedAlert");

			}
		}

	}
//------------------------------------------------------------------------------------------------
// Exit on Close...

	private static void addExitOnCloseListener() {
		// Close and exit Micromanager & ImageJ
		topFrame.addWindowListener(
				new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						if (options.isExitOnClose()) {
							if (app != null) {
								((MMStudioMainFrame) app).closeSequence();
							} else {
								System.exit(0);
							}

						}
					}

				});
	}

	//-----------------------------------------------------------
	// For testing...
	public static void main(String[] args) {
		TopFrame.constructTopFrame();
		//TopFrame.addActionButton(new RunScriptAction(options));
		JFrame settingsFrame = TopFrame.createSettingsFrame1();
		JFrame settingsFrame2 = TopFrame.createSettingsFrame2();
		TopFrame.addFrameToShow(settingsFrame);
		TopFrame.addFrameToShow(settingsFrame2);
	}

	private static JFrame createSettingsFrame1() {
		// This frame contains the settings panels
		// It gets focus (becomes visible) when TopFrame gets focus.
		JFrame set = new JFrame();
		set.getContentPane().add(BorderLayout.CENTER, new JButton("hello1"));
		set.setBounds(100, 160, 300, 300);
		//set.setUndecorated(true);
		set.setVisible(true);
		return set;
	}

	private static JFrame createSettingsFrame2() {
		// This frame contains the settings panels
		// It gets focus (becomes visible) when TopFrame gets focus.
		JFrame set = new JFrame();
		set.getContentPane().add(BorderLayout.CENTER, new JButton("hello2"));
		set.setBounds(300, 400, 300, 300);
		//set.setUndecorated(true);
		set.setVisible(true);
		return set;


	}

	class FakeCore {

		public double getExposure() {
			return 10.1;
		}

		public void setExposure(double exposure) {
		}

	}
}
