package edu.mbl.cdp.frameaverage;

/*
 * Copyright © 2009 – 2012, Marine Biological Laboratory
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
 */
import ij.ImageStack;
import ij.gui.ImageWindow;
import mmcorej.CMMCore;
import org.micromanager.acquisition.AcquisitionVirtualStack;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.acquisition.VirtualAcquisitionDisplay;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.ImageFocusListener;

/**
 *
 * @author Amitabh Verma, averma@mbl.edu
 * @author Grant Harris, gharris@mbl.edu at the Marine Biological Laboratory,
 * Woods Hole, Mass.
 */
public class FrameAveragerRunnable implements Runnable, ImageFocusListener {

    CMMCore core_;
    ScriptInterface gui_;    
    AcquisitionWrapperEngine engineWrapper_;
    VirtualAcquisitionDisplay display_;
    boolean isDisplayControlsEnabled = false;
    FrameAverager fa;

    FrameAveragerRunnable(FrameAverager fa) {
        this.fa = fa;
        this.core_ = fa.core_;
        this.engineWrapper_ = fa.engineWrapper_;
    }

    @Override
    public void run() {
        if (fa.numberFrames > 1) {
            try {
                core_.logMessage("FrameAvg: entering runnable");
                engineWrapper_.setPause(true);
                acquireImages();
                engineWrapper_.setPause(false);
            } catch (Exception ex) {
                ex.printStackTrace();
                core_.logMessage("ERROR: FrameAvg: while entering runnable");
            }
        }
    }

    public void acquireImages() {
        try {
//            manageShutter(true);
            core_.waitForDevice(core_.getCameraDevice());
            core_.clearCircularBuffer();

            core_.startSequenceAcquisition(fa.numberFrames-1, 0, true);
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            while (core_.getRemainingImageCount() > 0 || core_.isSequenceRunning(core_.getCameraDevice())) {
                if (core_.getRemainingImageCount() > 0) {
                   fa.taggedImageArray[frame] = core_.popNextTaggedImage();                   
                   frame++;
                    if (fa.processor.isDisplayAvailable) {
                        if (display_ != null) {
                            if (display_.isActiveDisplay()) {
                                display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                            }
                        }
                    }
                } 
             }
            long itTook = System.currentTimeMillis() - now;
            core_.stopSequenceAcquisition(core_.getCameraDevice());  
            core_.logMessage("Averaging Acquisition took: " + itTook + " milliseconds for "+fa.numberFrames + " frames");
            
            // keep 0 free for the image from engine
//            for (int i = 1; i < fa.numberFrames; i++) {
//                core_.waitForDevice(core_.getCameraDevice());                
//                core_.snapImage();                   
//                fa.taggedImageArray[i] = core_.getLastTaggedImage(i);
//                                
//                    if (display_ != null) {
//                        if (display_.isActiveDisplay()) {
//                            display_.displayStatusLine("Image Avg. Acquiring No. " + (i + 1));
//                        } else {
//                            display_ = (VirtualAcquisitionDisplay) engineWrapper_.getDisplay();
//                        }
//                    } else {
//                        display_ = (VirtualAcquisitionDisplay) engineWrapper_.getDisplay();
//                    }
//                
//                core_.logMessage("FrameAvg: acquiring #: " + (i + 1));
//            }
            
         //   manageShutter(false);

        } catch (Exception ex) {
            ex.printStackTrace();
            core_.logMessage("FrameAvg Error");
            engineWrapper_.setPause(false);
        }
    }
    private boolean shutterOriginalState_;
    private boolean autoShutterOriginalState_;

    private void manageShutter(boolean enable) throws Exception {
        String shutterLabel = core_.getShutterDevice();
        if (shutterLabel.length() > 0) {
            if (enable) {
                shutterOriginalState_ = core_.getShutterOpen();
                autoShutterOriginalState_ = core_.getAutoShutter();
                core_.setAutoShutter(false);
                core_.setShutterOpen(shutterOriginalState_ || autoShutterOriginalState_);
            } else {
                core_.setShutterOpen(shutterOriginalState_);
                core_.setAutoShutter(autoShutterOriginalState_);
            }
        }
    }

    @Override
    public void focusReceived(ImageWindow focusedWindow) {
        // discard simple imageJ windows
        if (focusedWindow != null) {
            if (focusedWindow.getTitle().startsWith("Snap/Live Window")) {
                return;
            }
            if (!focusedWindow.getImagePlus().isHyperStack()) {
                return;
            }
        }
        ImageStack ImpStack = focusedWindow.getImagePlus().getImageStack();
        display_ = ((AcquisitionVirtualStack) ImpStack).getVirtualAcquisitionDisplay();
    }
}
