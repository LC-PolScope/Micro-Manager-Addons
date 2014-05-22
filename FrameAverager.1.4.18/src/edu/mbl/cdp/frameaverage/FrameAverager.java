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

import ij.ImageStack;
import ij.gui.ImageWindow;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.MMOptions;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.DataProcessor;
import org.micromanager.imageDisplay.AcquisitionVirtualStack;
//import org.micromanager.imageDisplay.SimpleWindowControls;
import org.micromanager.imageDisplay.VirtualAcquisitionDisplay;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ImageFocusListener;
import org.micromanager.utils.ReportingUtils;

public class FrameAverager  implements ImageFocusListener {

    static final String METADATAKEY = "FramesAveraged";
    CMMCore core_;    
    TaggedFrameAverager tfa_;
    AcquisitionWrapperEngine engineWrapper_;

    FrameAveragerProcessor processor;
    public FrameAveragerControls controlFrame_;
    
    public boolean debugLogEnabled_ = false;
    public boolean isEnabledForImageAcquisition = false;

    public int numberFrames = 4; // 4 frames by default
    int[] avoidDisplayChs_ = null;
    int[] avoidEngineChs_ = null;
    // shared...
    public TaggedImage[] taggedImageArray = null;
    
    public VirtualAcquisitionDisplay display_;    
    public VirtualAcquisitionDisplay displayLive_;    
            
    boolean isAdditionalDelayReg = false;
    static String CameraNameProperty = "CameraName";
    static String[] AdditionalDelayCams = {"Retiga 4000R"};

    public FrameAverager(AcquisitionWrapperEngine engineWrapper, CMMCore core, TaggedFrameAverager tfa) {

        engineWrapper_ = engineWrapper;
        core_ = core;
        tfa_ = tfa;
        
        additionalDelayCheck();
        setNumberFrames(numberFrames);
        getDebugOptions();
        
        GUIUtils.registerImageFocusListener(this); // Image Window listener
    }
    
    
    final void additionalDelayCheck() {
        try {
            String cam = core_.getCameraDevice();
            String camName = core_.getProperty(cam, CameraNameProperty);
            for (int i=0; i < AdditionalDelayCams.length; i++) {
                if (camName.equals(AdditionalDelayCams[i])) {
                    isAdditionalDelayReg = true;
                } else {
                    isAdditionalDelayReg = false;
                }
            }
        } catch (Exception ex) {
            isAdditionalDelayReg = false;
            ReportingUtils.logError(ex);
        }        
    }

    
    public void UpdateEngineAndCore() {
        if (tfa_ != null) {
            engineWrapper_ = tfa_.getAcquisitionWrapperEngine();
            core_ = tfa_.getCMMCore();
        }
    }
    
    public void getDebugOptions() {
        Preferences root = Preferences.userNodeForPackage(MMOptions.class);
        Preferences prefs = root.node(root.absolutePath() + "/" + "MMOptions");      
        debugLogEnabled_ = prefs.getBoolean("DebugLog", debugLogEnabled_);
    }

    public void attachRunnable() {
        Runnable setPauseAndAcquire = new FrameAveragerRunnable(this);
        // The runnable is attached to the channels for which there will be frame averaging.
        // 'Engine channels' to avoid are not included.
        if (avoidEngineChs_ == null || engineWrapper_.getChannels().size()==1) {
            engineWrapper_.attachRunnable(-1, -1, -1, -1, setPauseAndAcquire); // t, p, s, c
        } else if (engineWrapper_.getChannels().size() < avoidEngineChs_[avoidEngineChs_.length-1]) {
            engineWrapper_.attachRunnable(-1, -1, -1, -1, setPauseAndAcquire); // t, p, s, c
        } else {            
            for (int i = 0; i < engineWrapper_.getChannels().size(); i++) {
                boolean bool = true;                
                for (int j = 0; j < avoidEngineChs_.length; j++) {
                    if (i == avoidEngineChs_[j]) {
                        bool = false;
                    }
                }
                if (bool) {
                    engineWrapper_.attachRunnable(-1, -1, -1, i, setPauseAndAcquire);
                }
            }
        }
        if (debugLogEnabled_) {
            ReportingUtils.logMessage("FrameAvg: runnable attached");
        }
    }

    public int getNumberFrames() {
        return numberFrames;
    }

    public void setNumberFrames(int numberFrames) {
        this.numberFrames = numberFrames;
        bufferArraySpace();
    }

    public void bufferArraySpace() {
        taggedImageArray = new TaggedImage[numberFrames];            
    }

    // Diplay channels include Engine channels and Virtual channels
    public void setAvoidDisplayChannels(int[] channelsToAvoid) {
        this.avoidDisplayChs_ = channelsToAvoid;
    }

    // Specifies channels 
    public void setAvoidEngineChannels(int[] channelsToAvoid) {
        this.avoidEngineChs_ = channelsToAvoid;
    }

    public void enable(boolean enableAveraging) {
        if (enableAveraging) {
            startProcessor();
            attachRunnable();
        } else {
            // Disable
            stopAndClearProcessor();
            stopAndClearRunnable();
        }
    }

    public void startProcessor() {
        try {
            attachDataProcessor();
        } catch (Exception ex) {
        }
    }

    public void attachDataProcessor() {
        if (processor==null) {
            processor = new FrameAveragerProcessor();
        }
        engineWrapper_.addImageProcessor(processor);
    }

    public void stopAndClearProcessor() {
        if (processor != null) {
            processor.requestStop();
        }
        try {
            engineWrapper_.removeImageProcessor(processor.getDataProcessor());
            
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: processor removed");
            }
        } catch (Exception ex) {
        }
    }

    public void stopAndClearRunnable() {
        try {
            engineWrapper_.clearRunnables();  // Potentially dangerous - needs specific clearRunnable(X)
            
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: runnable removed");
            }
        } catch (Exception ex) {
        }
    }
    
    public void acquireImagesFromRunningSequence() {
        processor.runAcquireFromRunningSequence();
    }
    
    public void acquireImagesStartSequence() {
        processor.runAcquireStartSequence();
    }

    public DataProcessor<TaggedImage> getDataProcessor() {
        return processor.getDataProcessor();
    }

    public JFrame getControlFrame() {
        if (controlFrame_ == null) {
            controlFrame_ = new FrameAveragerControls(this);
        }
        return controlFrame_;
    }
    
    
    @Override
    public void focusReceived(ImageWindow focusedWindow) {
        // discard if closed
        if (focusedWindow == null) {
            return;
        }
        // discard Snap/Live Window
        if (focusedWindow != null) {
            String str = focusedWindow.getTitle();
            if (focusedWindow.getTitle().startsWith("Snap/Live Window") ||  str.equals(" (100%)")) {
                ImageStack ImpStack = focusedWindow.getImagePlus().getImageStack();
                if (ImpStack instanceof AcquisitionVirtualStack) {
                    displayLive_ = ((AcquisitionVirtualStack) ImpStack).getVirtualAcquisitionDisplay();
                } else {
                    displayLive_ = null;
                }
                return;
            }     
        }
        
        if (!focusedWindow.isClosed()) {
            ImageStack ImpStack = focusedWindow.getImagePlus().getImageStack();
            VirtualAcquisitionDisplay display;
            if (ImpStack instanceof AcquisitionVirtualStack) {
                display = ((AcquisitionVirtualStack) ImpStack).getVirtualAcquisitionDisplay();
                if (display.acquisitionIsRunning()) {
                    display_ = display;
                    display_.show();
                }
            } else {
                if (display_!=null && !display_.acquisitionIsRunning()) {
                    display_ = null;
                }
            }
        }
    }

}
