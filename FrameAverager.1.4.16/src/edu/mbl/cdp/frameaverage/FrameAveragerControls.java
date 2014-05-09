package edu.mbl.cdp.frameaverage;

/*
 * Copyright © 2009 – 2013, Marine Biological Laboratory
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of 
 * the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of any organization.
 * 
 * Multiple-Frame Averaging plug-in for Micro-Manager
 * @author Amitabh Verma (averma@mbl.edu), Grant Harris (gharris@mbl.edu)
 * Marine Biological Laboratory, Woods Hole, Mass.
 * 
 */

import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.micromanager.internalinterfaces.AcqSettingsListener;

public class FrameAveragerControls extends javax.swing.JFrame implements AcqSettingsListener {

    /* TODO
     * Start not-Enabled
     * Add #frames peristence and avoided channels
     * Disable channels to avoid when averaging is enabled
     * Disable #frame field during acquisition.
     *   Is there a signal (e.g. propertychanged) that can be listened for?
     * 
     * Testing
 
     * Add VirtChannels to avoid input field
     * Then test with VirtualChannelInsertThingie
     */
    //private final CMMCore core_;
    static FrameAveragerControls frame;
    private FrameAverager fa;
    //private AcquisitionWrapperEngine engine_ = null;
    //private boolean enabled_ = false;
    private static JFrame About;

    private String lastChannelAvoidanceStr = "";
    
    public static final Preferences FrameAveragerControlPrefs = Preferences.userNodeForPackage(FrameAveragerControls.class);
    public final String FrameXposKey = "PREF_FrameX";
    public final String FrameYposKey = "PREF_FrameY";
    public final String NumberOfFramesKey = "PREF_NumberOfFramesKey";
    public int FrameXpos = 300;
    public int FrameYpos = 300;
    
    /**
     * Creates new form FrameAveragerControls
     */
    public FrameAveragerControls(FrameAverager fa) {
        this.fa = fa;
        //core_ = fa.core_;
        initComponents();
        URL url = this.getClass().getResource("frameIcon.png");
        Image im = Toolkit.getDefaultToolkit().getImage(url);
        setIconImage(im);
        getPreferences();
        numFramesField.setText(String.valueOf(fa.numberFrames));
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        
        lastChannelAvoidanceStr = channelsToAvoid.getText();
        // add as Frame to show with Toolbar
        // ToolbarMMX.addFrameToShow(this);
        //initPlugin();
        
        frame = this;
    }

    
    @Override
    public void settingsChanged() {        
        update();
    }
    
    public void update() {
        if (this.enabledCheckBox_.isSelected()) {
            fa.UpdateEngineAndCore();
            fa.enable(false);
        }

        fa.enable(this.enabledCheckBox_.isSelected());
        if (!this.enabledCheckBox_.isSelected()) {
            updateLabel("");
        } else {
            // Add disable avoid channels input box
            updateStatus();
        }
    }
    
    private void getPreferences() {
        FrameXpos = FrameAveragerControlPrefs.getInt(FrameXposKey, FrameXpos);
        FrameYpos = FrameAveragerControlPrefs.getInt(FrameYposKey, FrameYpos);
        fa.numberFrames = FrameAveragerControlPrefs.getInt(NumberOfFramesKey, fa.numberFrames);
    }
    
    private void setPreferences() {
        
        FrameXpos = this.getX();
        FrameYpos = this.getY();
        
        FrameAveragerControlPrefs.putInt(FrameXposKey, FrameXpos);
        FrameAveragerControlPrefs.putInt(FrameYposKey, FrameYpos);
        FrameAveragerControlPrefs.putInt(NumberOfFramesKey, fa.numberFrames);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        numFramesField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        enabledCheckBox_ = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        channelsToAvoid = new javax.swing.JTextField();
        labelStatus = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Frame Averager");
        setBounds(new java.awt.Rectangle(300, 300, 150, 150));
        setMinimumSize(new java.awt.Dimension(150, 150));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                formFocusLost(evt);
            }
        });

        numFramesField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        numFramesField.setText("4");
        numFramesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numFramesFieldActionPerformed(evt);
            }
        });
        numFramesField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                numFramesFieldFocusLost(evt);
            }
        });

        jLabel1.setText("Number of Image Frames to average");

        enabledCheckBox_.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        enabledCheckBox_.setText("Enabled");
        enabledCheckBox_.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enabledCheckBox_ActionPerformed(evt);
            }
        });

        jLabel2.setText("Avoid Channel(s) (zero-based)");

        channelsToAvoid.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        channelsToAvoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                channelsToAvoidActionPerformed(evt);
            }
        });
        channelsToAvoid.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                channelsToAvoidFocusLost(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("eg. 1,2 or 1-5");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Multi-Frame Averaging");

        jMenu1.setText("Help");

        jMenuItem1.setText("About");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 33, Short.MAX_VALUE)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, channelsToAvoid, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, numFramesField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(labelStatus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(enabledCheckBox_)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(numFramesField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(channelsToAvoid, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(enabledCheckBox_))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(labelStatus, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
      
  }//GEN-LAST:event_formWindowClosed

  private void enabledCheckBox_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enabledCheckBox_ActionPerformed
      if (!showingMsg) {
        if (fa.core_.isSequenceRunning()) {
            enabledCheckBox_.setSelected(!enabledCheckBox_.isSelected());
            FrameAveragerControls.showMessage("Live mode is running ! Please stop before enabling/disabling.");          
            return;
        } else if (TaggedFrameAverager.gui.isAcquisitionRunning()) {
            enabledCheckBox_.setSelected(!enabledCheckBox_.isSelected());
            FrameAveragerControls.showMessage("Acquisition is running ! Please stop before enabling/disabling.");          
            return;
        }
        update();
      } else {
          enabledCheckBox_.setSelected(!enabledCheckBox_.isSelected());
      }
  }//GEN-LAST:event_enabledCheckBox_ActionPerformed

  private void numFramesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numFramesFieldActionPerformed
      updateNumFramesField();
  }//GEN-LAST:event_numFramesFieldActionPerformed
  private void channelsToAvoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_channelsToAvoidActionPerformed
      channelAvoidance();
  }//GEN-LAST:event_channelsToAvoidActionPerformed

  private void channelsToAvoidFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_channelsToAvoidFocusLost
      channelAvoidance();
  }//GEN-LAST:event_channelsToAvoidFocusLost

  private void numFramesFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numFramesFieldFocusLost
      updateNumFramesField();
  }//GEN-LAST:event_numFramesFieldFocusLost

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        if (About == null) {
            About = new About(this);
        }
        About.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        fa.getDebugOptions();
    }//GEN-LAST:event_formFocusGained

    private void formFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusLost
        fa.getDebugOptions();
    }//GEN-LAST:event_formFocusLost

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (fa.core_.isSequenceRunning()) {
            if (enabledCheckBox_.isSelected()) {
                setVisible( true );
                FrameAveragerControls.showMessage("Live mode is running with FrameAverager ! Please stop Live before closing FrameAverager.");    
            }
        } else if (TaggedFrameAverager.gui.isAcquisitionRunning()) {
            if (enabledCheckBox_.isSelected()) {
                setVisible( true );
                FrameAveragerControls.showMessage("Acquisition is running with FrameAverager ! Please stop Acquisition before closing FrameAverager.");    
            }
        } else {
            if (enabledCheckBox_.isSelected()) {
                enabledCheckBox_.setSelected(false);
                update();
            }
            setVisible( false );
            setPreferences();
        }
    }//GEN-LAST:event_formWindowClosing

    private void updateNumFramesField() {
        int num;
        if (!showingMsg) {
            if (!numFramesField.getText().isEmpty()) {
                String str = numFramesField.getText().trim().toString();
                try {
                 num = (int) Integer.parseInt(str);
                } catch(NumberFormatException e) {
                    System.out.println("CAUGHT: " + e.toString());
                    System.out.println("Defaulting value to 4");
                    num = 4;
                }
                fa.setNumberFrames(num);
                updateStatus();
            } else {
                FrameAveragerControls.showMessage("Number of Image Frames to average has to be an Integer");    
            }
        } else {
            numFramesField.setText(String.valueOf(fa.numberFrames));
        }
    }
    
    private void channelAvoidance() {
        if (!showingMsg) {
            if (!lastChannelAvoidanceStr.equals(channelsToAvoid.getText())) {
                if (fa.core_.isSequenceRunning()) {
                    channelsToAvoid.setText(lastChannelAvoidanceStr);
                    FrameAveragerControls.showMessage("Live mode is running ! Please stop before chaging this field.");            
                } else if (TaggedFrameAverager.gui.isAcquisitionRunning()) {
                    channelsToAvoid.setText(lastChannelAvoidanceStr);
                    FrameAveragerControls.showMessage("Acquisition is running ! Please stop before chaging this field.");            
                } else {            
                    updateChannelsToAvoid();
                    update();
                    lastChannelAvoidanceStr = channelsToAvoid.getText();
                }
            }
        } else {
            channelsToAvoid.setText(lastChannelAvoidanceStr);
        }
    }

    private void updateChannelsToAvoid() {
        String str = channelsToAvoid.getText().trim().toString();
        int[] avoidChannels = stringToIntArray(str);
        fa.setAvoidDisplayChannels(avoidChannels);
        fa.setAvoidEngineChannels(avoidChannels);
        updateStatus();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField channelsToAvoid;
    private javax.swing.JCheckBox enabledCheckBox_;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JTextField numFramesField;
    // End of variables declaration//GEN-END:variables

    public void updateStatus() {
        try {
//			int[] intChannelsToAvoid = fa.setChannelsToAvoid();
//			int numFrames = getNumFrames();
            String avgString;
            String avoidString = "";

            if (fa.numberFrames < 1 || !enabledCheckBox_.isSelected()) {
                avgString = "Averaging is Disabled.";
                avoidString = "";
            } else {
                avgString = "Averaging " + fa.numberFrames + " frames per image ";
                if (fa.avoidEngineChs_ == null) {
                    avoidString = "for All Channels.";
                } else {
                    String str = channelsToAvoid.getText().trim().toString();
                    if (str.contains(",") || str.contains(";")) {
                        avoidString = "Except " + fa.avoidEngineChs_.length + " Channels, " + str + ".";
                    } else if (str.contains("-") || str.contains("=")) {
                        avoidString = "Except " + fa.avoidEngineChs_.length + " Channels, "
                                + fa.avoidEngineChs_[0] + " to "
                                + fa.avoidEngineChs_[fa.avoidEngineChs_.length - 1] + " (inclusive).";
                    } else if (fa.avoidEngineChs_.length == 1) {
                        avoidString = "Except " + fa.avoidEngineChs_.length + " Channel, No. " + str + ".";
                    }
                }
            }
            updateLabel(avgString + avoidString);
        } catch (Exception ex) {
        }
    }

//	public String getMetadataField() {
//		String str = textMetadataField.getText();
//		if (str.isEmpty()) {
//			str = null;
//		}
//		return str;
//	}
//	
//	public void setMetadataField() {
//		String str = textMetadataField.getText();
//		if (str.isEmpty()) {
//			str = "Image Averaging";
//		}
//		fa.METADATAKEY = str;
//	}
    public int[] stringToIntArray(String str) {
        int[] intArray = parseString(str);
        return intArray;
    }

    public String intArrayToString(int[] intArray) {
        if (intArray.length < 1) {
            return null;
        }
        String str = Arrays.toString(intArray);
        return str;
    }

    public void updateLabel(String str) {
        labelStatus.setText(str);
    }

    public int[] parseString(String str) {
        int[] intArray = null;
        if (str.contains("-")) {
            intArray = getHyphenStringToIntArray(str, "-");
        } else if (str.contains("=")) {
            intArray = getHyphenStringToIntArray(str, "=");
        } else if (str.contains(",")) {
            intArray = getCommaStringToIntArray(str, ",");
        } else if (str.contains(";")) {
            intArray = getCommaStringToIntArray(str, ";");
        } else if (str.isEmpty() || str.trim().isEmpty()) {
            intArray = null;
        } else {
            intArray = new int[1];
            intArray[0] = Integer.parseInt(str);
        }
        return intArray;
    }

    public int[] getCommaStringToIntArray(String str, String regStr) {
        String[] strArray = str.split(regStr);
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.parseInt(strArray[i]);
        }
        return intArray;
    }

    public int[] getHyphenStringToIntArray(String str, String regStr) {
        String[] strArray = str.split(regStr);
        int[] intArrayTemp = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArrayTemp[i] = Integer.parseInt(strArray[i].trim());
        }
        int n1 = intArrayTemp[0];
        int n2 = intArrayTemp[1];
        int[] intArray = new int[n2 - n1 + 1];
        for (int i = n1; i < n2 + 1; i++) {
            intArray[i - n1] = i;
        }
        return intArray;
    }
    
    public static void showMessage(String msg) {
        showNonBlockingMessage(JOptionPane.WARNING_MESSAGE, TaggedFrameAverager.menuName, msg, getInstance());
    }
    
    public static void showMessage(String title, String msg) {
        showNonBlockingMessage(JOptionPane.WARNING_MESSAGE, title, msg, getInstance());
    }
    
    static boolean showingMsg = false;
    public static void showNonBlockingMessage(int msgType, String title, String message, Frame owningFrame_) {
      if (null != owningFrame_) {
         Object[] options = { "OK" };
         final JOptionPane optionPane = new JOptionPane(message, msgType, JOptionPane.DEFAULT_OPTION, null, options);
         /* the false parameter is for not modal */
         final JDialog dialog = new JDialog(owningFrame_, title, false);
         optionPane.addPropertyChangeListener(
                 new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent e) {
                       String prop = e.getPropertyName();
                       if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                          dialog.setVisible(false);
                          showingMsg = false;
                       }
                    }
                 });         
         
         dialog.setContentPane(optionPane);
         /* adapting the frame size to its content */
         dialog.pack();
         dialog.setLocationRelativeTo(owningFrame_);
         dialog.setVisible(true);
         showingMsg = true;
      }
   }
    
  public static FrameAveragerControls getInstance() {
      return frame;
  }
}
