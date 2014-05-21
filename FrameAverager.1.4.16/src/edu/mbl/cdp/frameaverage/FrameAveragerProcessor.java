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
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.AcquisitionVirtualStack;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.api.DataProcessor;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ImageFocusListener;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

public class FrameAveragerProcessor extends DataProcessor<TaggedImage> {

    CMMCore core_;
    JSONObject json = null;
    double exposure;
    //boolean isDisplayAvailable = false;
    //int numberFrames_ = 1;
    int imgDepth;
    int iNO = 0;
    FrameAverager fa;
    TaggedImage imageOnError;

    public FrameAveragerProcessor(FrameAverager fa, CMMCore core) {
        this.fa = fa;
        core_ = core;
        
        //isDisplayAvailable = false;
        //engine_ = gui_.getAcquisitionEngine();
    }

    @Override
    protected void process() {
        try {            
            final TaggedImage taggedImage = poll();
            imageOnError = taggedImage;
            
            if (fa.numberFrames < 2) { // if MFA is disabled
                if (taggedImage == null) { // EOL check
                    produce(TaggedImageQueue.POISON);
                    return;
                }
                if (TaggedImageQueue.isPoison(taggedImage)) { // EOL check
                    produce(taggedImage);
                    return;
                }
            
                produce(taggedImage);
                if (fa.debugLogEnabled_) {
                    ReportingUtils.logMessage("FrameAvg: averaging disabled");
                }
                return;
            } else {
                if (isFirstEmptyArray() && !isPartiallyFilledArray()) {
                    if (taggedImage == null) { // EOL check
                        produce(TaggedImageQueue.POISON);
                        return;
                    }
                    if (TaggedImageQueue.isPoison(taggedImage)) { // EOL check
                        produce(taggedImage);
                        return;
                    }
                } else {                                                
                    if (taggedImage == null || TaggedImageQueue.isPoison(taggedImage)) {                        
                        new Thread("Poison-Image-Delay") {
                            public void run() {                            
                                    if (isPartiallyFilledArray()) {
                                        if (!isFirstEmptyArray()) {
                                            // deals with case of snap when only a single image is coming through                                        
                                            fa.taggedImageArray[0] = fa.taggedImageArray[1];

                                            if (!core_.isSequenceRunning()) {
                                                acquireImagesStartSeequence(false);
                                            }

                                            compute(fa.taggedImageArray); // on to computing avg. frame
                                            //isDisplayAvailable = true;
                                            if (fa.debugLogEnabled_) {
                                                core_.logMessage("FrameAvg: exiting processor");
                                            }
                                        }
                                    }
                                    produce(TaggedImageQueue.POISON);
                            }
                        }.start();
                    return;
                    }
                }
            }      
            json = taggedImage.tags;
            final int channel = MDUtils.getChannelIndex(json);
            final String channelName = MDUtils.getChannelName(json);

            if (fa.avoidDisplayChs_ != null) {// channel avoidance
                for (int c = 0; c < fa.avoidDisplayChs_.length; c++) {
                    if (channel == fa.avoidDisplayChs_[c]) {
                        if (fa.debugLogEnabled_) {
                            ReportingUtils.logMessage("FrameAvg: avoided channel: " + channel + " " + channelName);
                        }
                        produce(taggedImage);
                        return;
                    }
                }
            }

            imgDepth = MDUtils.getDepth(json);
            if (imgDepth == 1 || imgDepth == 2) { // if Image is not 8/16 bit
            } else {
                produce(taggedImage);
                return;
            }
            if (fa.debugLogEnabled_) {
                core_.logMessage("FrameAvg: entering processor");
            }
            // taggedImageArray has the rest of the array filled before in Runnable
            
            if (testForEmptyArray()) {
                for (int i = 1; i < fa.taggedImageArray.length; i++) {
                    if (fa.taggedImageArray[i] == null) {
                        if (fa.displayLive_ != null) {
                            if (TaggedFrameAverager.gui.isLiveModeOn()) {
                             String currTxt = fa.displayLiveLabel.getText();
                             if (currTxt.contains("fps")) {
                                 if (currTxt.contains(" - I")) {
                                    currTxt = currTxt.substring(0, currTxt.indexOf(" - I"));
                                 }
                                fa.displayLive_.displayStatusLine(currTxt + " - Image Avg. Acquiring No. " + (i+1));
                             } else {
                                 fa.displayLiveLabel.invalidate();                                 
                             }
                            }
                        }
                        fa.taggedImageArray[i] = taggedImage;
                        return;
                    }
                }
            }
            
            fa.taggedImageArray[0] = taggedImage;
            if (fa.displayLive_ != null) {
                if (TaggedFrameAverager.gui.isLiveModeOn()) {
                    String currTxt = fa.displayLiveLabel.getText();
                    if (currTxt.contains("fps")) {
                        if (currTxt.contains(" - I")) {
                            currTxt = currTxt.substring(0, currTxt.indexOf(" - I"));
                        }
                        fa.displayLive_.displayStatusLine(currTxt + " - Image Avg. Acquiring No.  1");
                    } else {
                        fa.displayLiveLabel.invalidate();
                    }
                }
            }
            
            compute(fa.taggedImageArray); // on to computing avg. frame
            //isDisplayAvailable = true;
            if (fa.debugLogEnabled_) {
                core_.logMessage("FrameAvg: exiting processor");
            }

        } catch (Exception ex) {
            produce(imageOnError);
            emptyImageArray();
            ReportingUtils.logError("ERROR: FrameAvg, in Process: ");
            ex.printStackTrace();            
        }
    }
    
    public boolean testForEmptyArray() {
        for (int i=1; i < fa.taggedImageArray.length; i++) {
            if (fa.taggedImageArray[i] == null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isPartiallyFilledArray() {
        for (int i=0; i < fa.taggedImageArray.length; i++) {
            if (fa.taggedImageArray[i] == null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isFirstEmptyArray() {
        if (fa.taggedImageArray[1] == null) {
                return true;            
        }
        
        return false;
    }

    private void compute(TaggedImage[] taggedImageArrayTemp) {

        try {
            if (fa.debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: computing...");
            }
            TaggedImage taggedImage = taggedImageArrayTemp[0];

            int width = MDUtils.getWidth(taggedImage.tags);
            int height = MDUtils.getHeight(taggedImage.tags);
            //core_.logMessage(MDUtils.getChannelName(taggedImage.tags));

            int dimension = width * height;
            byte[] pixB;
            byte[] retB = new byte[dimension];
            short[] pixS;
            short[] retS = new short[dimension];
            float[] retF = new float[dimension];
            Object result = null;

            for (int i = 0; i < fa.numberFrames; i++) {
//                            core_.logMessage("FrameAvg: Avg... image "+i);
                if (imgDepth == 1) {
                    pixB = (byte[]) taggedImageArrayTemp[i].pix;
                    for (int j = 0; j < dimension; j++) {
                        retF[j] = (float) (retF[j] + (int) (pixB[j] & 0xff));
                    }
                } else if (imgDepth == 2) {
                    pixS = (short[]) taggedImageArrayTemp[i].pix;
                    for (int j = 0; j < dimension; j++) {
                        retF[j] = (float) (retF[j] + (int) (pixS[j] & 0xffff));
                    }
                }
            }
            if (imgDepth == 1) {
                for (int j = 0; j < dimension; j++) {
                    retB[j] = (byte) (int) (retF[j] / fa.numberFrames);
                }
                result = retB;
            } else if (imgDepth == 2) {
                for (int j = 0; j < dimension; j++) {
                    retS[j] = (short) (int) (retF[j] / fa.numberFrames);
                }
                result = retS;
            }
            // Averaged channel
            // Weird way of copying a JSONObject
            JSONObject tags = new JSONObject(taggedImage.tags.toString());
            tags.put(FrameAverager.METADATAKEY, fa.numberFrames);
            TaggedImage averagedImage = new TaggedImage(result, tags);
            produce(averagedImage);
            emptyImageArray();
            if (fa.debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: produced averaged image");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logError("Error: FrameAvg, while producing averaged img.");
            produce(taggedImageArrayTemp[0]);
            emptyImageArray();
        }
    }
    
    public void emptyImageArray() {
        for (int i = 0; i < fa.taggedImageArray.length; i++) {
            fa.taggedImageArray[i] = null;
        }
    }
    
    public void runAcquireFromRunningSequence() {
        if (fa.numberFrames > 1) {
            try {
                if (fa.debugLogEnabled_) {
                    ReportingUtils.logMessage("FrameAvg: acquireImagesFromRunningSequence");
                }
                acquireImagesFromRunningSequence();
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: FrameAvg: while entering acquireImagesFromRunningSequence");
            }
        }
    }
    
    public void acquireImagesFromRunningSequence() {
        try {
            
            String cam = core_.getCameraDevice();
            
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            
            while (frame < fa.numberFrames && (core_.getRemainingImageCount() > 0 || core_.isSequenceRunning(cam))) {
                if (core_.getRemainingImageCount() > 0) {
                    if (fa.isAdditionalDelayReg) {
                        Thread.sleep(250);
                    }
                   fa.taggedImageArray[frame] = core_.popNextTaggedImage();
                   frame++;
                        if (fa.display_ != null) {
                            if (fa.display_.isActiveDisplay()) {
                                fa.display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                            }
                        }
                }
             }
            long itTook = System.currentTimeMillis() - now;
            
            if (fa.debugLogEnabled_) {
                ReportingUtils.logMessage("Averaging Acquisition took: " + itTook + " milliseconds for "+fa.numberFrames + " frames");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("FrameAvg Error");
        }
    }    

    public void runAcquireStartSequence() {
        if (fa.numberFrames > 1) {
            try {
                if (fa.debugLogEnabled_) {
                    ReportingUtils.logMessage("FrameAvg: entering runnable");
                }                
                acquireImagesStartSeequence(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: FrameAvg: while entering runnable");
            }
        }
    }

    public void acquireImagesStartSeequence(boolean stopAtEnd) {
        try {
            core_.waitForDevice(core_.getCameraDevice());
            core_.clearCircularBuffer();
            String cam = core_.getCameraDevice();
            
//          CMMCore::startSequenceAcquisition(long numImages, double intervalMs, bool stopOnOverflow)
//          @param numImages Number of images requested from the camera
//          @param intervalMs interval between images, currently only supported by Andor cameras
//          @param stopOnOverflow whether or not the camera stops acquiring when the circular buffer is full
            core_.startSequenceAcquisition(fa.numberFrames-1, 0, false);
            
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            
            while (core_.getRemainingImageCount() > 0 || core_.isSequenceRunning(cam)) {
                if (core_.getRemainingImageCount() > 0) {
                    if (fa.isAdditionalDelayReg) {
                        Thread.sleep(250);
                    }
                   fa.taggedImageArray[frame] = core_.popNextTaggedImage();
                   frame++;                 
                    if (fa.display_ != null) {
                        if (fa.display_.acquisitionIsRunning()) {
                            fa.display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                        }
                    }
                    if (fa.displayLive_ != null) {
                        if (TaggedFrameAverager.gui.isLiveModeOn() || !stopAtEnd) {
                            String currTxt = fa.displayLiveLabel.getText();
                            if (currTxt.contains("fps")) {
                                if (currTxt.contains(" - I")) {
                                    currTxt = currTxt.substring(0, currTxt.indexOf(" - I"));
                                }
                                fa.displayLive_.displayStatusLine(currTxt + " - Image Avg. Acquiring No. " + frame);
                            } else {
                                fa.displayLiveLabel.invalidate();                                
                                fa.displayLive_.displayStatusLine(" - Image Avg. Acquiring No. " + frame);                                
                            }
                        }
                    }
                }
             }
            long itTook = System.currentTimeMillis() - now;
            if (stopAtEnd) {
                core_.stopSequenceAcquisition(cam);  
            }
            if (fa.debugLogEnabled_) {
                ReportingUtils.logMessage("Averaging Acquisition took: " + itTook + " milliseconds for "+fa.numberFrames + " frames");
            }
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
            ReportingUtils.logMessage("FrameAvg Error");
        }
    }

    public DataProcessor<TaggedImage> getDataProcessor() {
        return (DataProcessor<TaggedImage>) this;
    }

    public static boolean isPoison(TaggedImage image) {
        return ((image.pix == null) || (image.tags == null));
    }
}
