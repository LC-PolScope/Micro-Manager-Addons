package edu.mbl.cdp.bitDepth;

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

import java.util.prefs.Preferences;
import javax.swing.JFrame;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.micromanager.MMOptions;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ReportingUtils;

public class BitDepthDowngrader {

    static final String METADATAKEY = "BitDepthDowngrader";
    ScriptInterface gui_;
    CMMCore core_;    
    AcquisitionWrapperEngine engineWrapper_;

    BitDepthDowngraderProcessor processor;
    public BitDepthDowngraderControls controlFrame_;
    
    public boolean debugLogEnabled_ = false;
    public boolean isEnabledForImageAcquisition = false;
    

    public BitDepthDowngrader(ScriptInterface gui) {
        gui_ = gui;        
        core_ = gui_.getMMCore();
        UpdateEngine();
        getDebugOptions();
    }
        
    public void UpdateEngine() {
        engineWrapper_ = getAcquisitionWrapperEngine();
    }    
    
    public AcquisitionWrapperEngine getAcquisitionWrapperEngine() {
        AcquisitionWrapperEngine engineWrapper = (AcquisitionWrapperEngine) MMStudioMainFrame.getInstance().getAcquisitionEngine();
        return engineWrapper;
    }
    
    public void getDebugOptions() {
        Preferences root = Preferences.userNodeForPackage(MMOptions.class);
        Preferences prefs = root.node(root.absolutePath() + "/" + "MMOptions");      
        debugLogEnabled_ = prefs.getBoolean("DebugLog", debugLogEnabled_);
    }

    public void attachRunnable() {
        Runnable changeSummaryMetadata = new BitDepthDowngraderRunnable(this);
        
        engineWrapper_.attachRunnable(0, 0, 0, 0, changeSummaryMetadata); // t, p, s, c
        
        if (debugLogEnabled_) {
            ReportingUtils.logMessage("BitDepthDowngrader: runnable attached");
        }
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
            processor = new BitDepthDowngraderProcessor();
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
                ReportingUtils.logMessage("BitDepthDowngrader: processor removed");
            }
        } catch (Exception ex) {
        }
    }

    public void stopAndClearRunnable() {
        try {
            engineWrapper_.clearRunnables();  // Potentially dangerous - needs specific clearRunnable(X)
            
            if (debugLogEnabled_) {
                ReportingUtils.logMessage("BitDepthDowngrader: runnable removed");
            }
        } catch (Exception ex) {
        }
    }
    
    public DataProcessor<TaggedImage> getDataProcessor() {
        return processor.getDataProcessor();
    }

    public JFrame getControlFrame() {
        if (controlFrame_ == null) {
            controlFrame_ = new BitDepthDowngraderControls(this);
        }
        return controlFrame_;
    }
    
}
