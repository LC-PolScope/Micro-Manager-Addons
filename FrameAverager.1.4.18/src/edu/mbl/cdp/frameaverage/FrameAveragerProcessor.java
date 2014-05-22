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
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

public class FrameAveragerProcessor extends DataProcessor<TaggedImage> {

    
    JSONObject json = null;
    int imgDepth;    
    TaggedFrameAverager tfa;
    TaggedImage imageOnError;

    @Override
    public void setApp(ScriptInterface gui) {
        gui_ = gui;
        tfa = new TaggedFrameAverager();
        tfa.setApp(gui_);
        tfa.fa.processor = this;   
        tfa.frame = tfa.fa.getControlFrame();
   }

    @Override
    protected void process() {
        try {            
            final TaggedImage taggedImage = poll();
            // make a copy to produce if FrameAverager throws an error
            // preserve image acquired
            imageOnError = taggedImage; 
            
            // if in Multi-D mode and disabled on plugin skip processing
            if (tfa.fa.engineWrapper_.isAcquisitionRunning() && !tfa.fa.isEnabledForImageAcquisition) {
                produce(taggedImage);
                return;
            }
                        
            // if less than 2 frames then disable processing
            if (tfa.fa.numberFrames < 2) { // if MFA is disabled
                if (taggedImage == null) { // EOL check
                    produce(TaggedImageQueue.POISON);
                    return;
                }
                if (TaggedImageQueue.isPoison(taggedImage)) { // EOL check
                    produce(taggedImage);
                    return;
                }
            
                produce(taggedImage);
                if (tfa.fa.debugLogEnabled_) {
                    ReportingUtils.logMessage("FrameAvg: averaging disabled");
                }
                return;
            } else {
                // if we are not in a state where we have acquired some frames for averaging
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
                    // this case would be for Snap or end of Live routine where additional images
                    // are needed to be acquired to fill the averaging array
                    // a Poison image indicates EOL
                    if (taggedImage == null || TaggedImageQueue.isPoison(taggedImage)) {                                                
                        new Thread("Poison-Image-Delay") {
                            public void run() {                            
                                    if (isPartiallyFilledArray()) { // make sure there are empty slots in the averaging array
                                        if (!isFirstEmptyArray()) { // basically make sure slot 1 is filled with original image
                                                                                   
                                            tfa.fa.taggedImageArray[0] = tfa.fa.taggedImageArray[1];

                                            if (!tfa.getCMMCore().isSequenceRunning()) {
                                                acquireImagesStartSequence(false);
                                            }

                                            computeProduceAndEmpty(tfa.fa.taggedImageArray); // on to computing avg. frame

                                            if (tfa.fa.debugLogEnabled_) {
                                                tfa.getCMMCore().logMessage("FrameAvg: exiting processor");
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

            if (tfa.fa.avoidDisplayChs_ != null) {// channel avoidance
                for (int c = 0; c < tfa.fa.avoidDisplayChs_.length; c++) {
                    if (channel == tfa.fa.avoidDisplayChs_[c]) {
                        if (tfa.fa.debugLogEnabled_) {
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
            if (tfa.fa.debugLogEnabled_) {
                tfa.getCMMCore().logMessage("FrameAvg: entering processor");
            }
                        
            // Only applies for Live - MultiD and Snap collect images elsewhere (in Runnable and Poison-Image-Delay thread)
            // when in Live collect (n-1) required images from stream
            // when averaging array is filled skip this step and continue to
            // compute and produce avg. image
            if (testForEmptyArray()) {
                for (int i = 1; i < tfa.fa.taggedImageArray.length; i++) {
                    if (tfa.fa.taggedImageArray[i] == null) {
                        if (tfa.fa.displayLive_ != null) {
                            if (tfa.gui.isLiveModeOn()) {
                             tfa.fa.displayLive_.displayStatusLine(" - Image Avg. Acquiring No. " + (i+1));                             
                            }
                        }
                        tfa.fa.taggedImageArray[i] = taggedImage;
                        return;
                    }
                }
            }
            
            tfa.fa.taggedImageArray[0] = taggedImage;
            
            // try and get Display window for status
            if (tfa.fa.displayLive_ != null) {
                if (tfa.gui.isLiveModeOn()) {
                    tfa.fa.displayLive_.displayStatusLine(" - Image Avg. Acquiring No.  1");                    
                }
            }
            
            computeProduceAndEmpty(tfa.fa.taggedImageArray); // on to computing avg. frame

            if (tfa.fa.debugLogEnabled_) {
                tfa.getCMMCore().logMessage("FrameAvg: exiting processor");
            }

        } catch (Exception ex) {
            produce(imageOnError);
            emptyImageArray();
            ReportingUtils.logError("ERROR: FrameAvg, in Process: ");
            ex.printStackTrace();            
        }
    }
    
    public boolean testForEmptyArray() {
        for (int i=1; i < tfa.fa.taggedImageArray.length; i++) {
            if (tfa.fa.taggedImageArray[i] == null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isPartiallyFilledArray() {
        for (int i=0; i < tfa.fa.taggedImageArray.length; i++) {
            if (tfa.fa.taggedImageArray[i] == null) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isFirstEmptyArray() {
        if (tfa.fa.taggedImageArray[1] == null) {
                return true;            
        }
        
        return false;
    }

    private void computeProduceAndEmpty(TaggedImage[] taggedImageArrayTemp) {

        try {
            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: computing...");
            }

            int width = MDUtils.getWidth(taggedImageArrayTemp[0].tags);
            int height = MDUtils.getHeight(taggedImageArrayTemp[0].tags);
//            ReportingUtils.logMessage(MDUtils.getChannelName(taggedImage.tags));

            int dimension = width * height;
            byte[] pixB;
            byte[] retB = new byte[dimension];
            short[] pixS;
            short[] retS = new short[dimension];
            float[] retF = new float[dimension];
            Object result = null;

            for (int i = 0; i < tfa.fa.numberFrames; i++) {
//            ReportingUtils.logMessage("FrameAvg: Avg... image "+i);
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
                    retB[j] = (byte) (int) (retF[j] / tfa.fa.numberFrames);
                }
                result = retB;
            } else if (imgDepth == 2) {
                for (int j = 0; j < dimension; j++) {
                    retS[j] = (short) (int) (retF[j] / tfa.fa.numberFrames);
                }
                result = retS;
            }
            // Averaged channel
            // Weird way of copying a JSONObject
            JSONObject tags = new JSONObject(taggedImageArrayTemp[0].tags.toString());
            tags.put(FrameAverager.METADATAKEY, tfa.fa.numberFrames);
            TaggedImage averagedImage = new TaggedImage(result, tags);
            produce(averagedImage);
            emptyImageArray();
            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: produced averaged image");
            }
        } catch (Exception ex) {
            produce(imageOnError);
            emptyImageArray();
            ex.printStackTrace();
            ReportingUtils.logError("Error: FrameAvg, while producing averaged img.");            
        }
    }
    
    public void emptyImageArray() {
        for (int i = 0; i < tfa.fa.taggedImageArray.length; i++) {
            tfa.fa.taggedImageArray[i] = null;
        }
    }
    
    public void runAcquireFromRunningSequence() {
        if (tfa.fa.numberFrames > 1) {
            try {
                if (tfa.fa.debugLogEnabled_) {
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
            
            String cam = tfa.getCMMCore().getCameraDevice();
            
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            
            while (frame < tfa.fa.numberFrames && (tfa.getCMMCore().getRemainingImageCount() > 0 || tfa.getCMMCore().isSequenceRunning(cam))) {
                if (tfa.getCMMCore().getRemainingImageCount() > 0) {
                    if (tfa.fa.isAdditionalDelayReg) {
                        Thread.sleep(250);
                    }
                   tfa.fa.taggedImageArray[frame] = tfa.getCMMCore().popNextTaggedImage();
                   frame++;
                        if (tfa.fa.display_ != null) {
                            if (tfa.fa.display_.isActiveDisplay()) {
                                tfa.fa.display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                            }
                        }
                }
             }
            long itTook = System.currentTimeMillis() - now;
            
            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("Averaging Acquisition took: " + itTook + " milliseconds for "+tfa.fa.numberFrames + " frames");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("FrameAvg Error");
        }
    }    

    public void runAcquireStartSequence() {
        if (tfa.fa.numberFrames > 1) {
            try {
                if (tfa.fa.debugLogEnabled_) {
                    ReportingUtils.logMessage("FrameAvg: entering runnable");
                }                
                acquireImagesStartSequence(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                ReportingUtils.logMessage("ERROR: FrameAvg: while entering runnable");
            }
        }
    }

    public void acquireImagesStartSequence(boolean stopAtEnd) {
        try {
            tfa.getCMMCore().waitForDevice(tfa.getCMMCore().getCameraDevice());
            tfa.getCMMCore().clearCircularBuffer();
            String cam = tfa.getCMMCore().getCameraDevice();
            
//          CMMCore::startSequenceAcquisition(long numImages, double intervalMs, bool stopOnOverflow)
//          @param numImages Number of images requested from the camera
//          @param intervalMs interval between images, currently only supported by Andor cameras
//          @param stopOnOverflow whether or not the camera stops acquiring when the circular buffer is full
            tfa.getCMMCore().startSequenceAcquisition(tfa.fa.numberFrames-1, 0, false);
            
            long now = System.currentTimeMillis();
            int frame = 1;// keep 0 free for the image from engine
            // reference BurstExample.bsh
            
            while (tfa.getCMMCore().getRemainingImageCount() > 0 || tfa.getCMMCore().isSequenceRunning(cam)) {
                if (tfa.getCMMCore().getRemainingImageCount() > 0) {
                    if (tfa.fa.isAdditionalDelayReg) {
                        Thread.sleep(250);
                    }
                   tfa.fa.taggedImageArray[frame] = tfa.getCMMCore().popNextTaggedImage();
                   frame++;                 
                    if (tfa.fa.display_ != null) {
                        if (tfa.fa.display_.acquisitionIsRunning()) {
                            tfa.fa.display_.displayStatusLine("Image Avg. Acquiring No. " + frame);
                        }
                    }
                    if (tfa.fa.displayLive_ != null) {
                        if (tfa.gui.isLiveModeOn() || !stopAtEnd) {
                            tfa.fa.displayLive_.displayStatusLine(" - Image Avg. Acquiring No. " + frame);                            
                        }
                    }
                }
             }
            long itTook = System.currentTimeMillis() - now;
            if (stopAtEnd) {
                tfa.getCMMCore().stopSequenceAcquisition(cam);  
            }
            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("Averaging Acquisition took: " + itTook + " milliseconds for "+tfa.fa.numberFrames + " frames");
            }

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
    
    @Override
   public void makeConfigurationGUI() {
        if (tfa==null) {
            tfa = new TaggedFrameAverager();
            tfa.setApp(gui_);
            tfa.fa.processor = this;
            tfa.frame = tfa.fa.getControlFrame();
            tfa.gui.addMMBackgroundListener(tfa.frame);
        } else if (tfa.frame==null) {            
            tfa.frame = tfa.fa.getControlFrame();
            tfa.gui.addMMBackgroundListener(tfa.frame);
        }
        tfa.frame.setVisible(true);
   }
    
    @Override
   public void dispose() {
      if (tfa.frame != null) {
         tfa.frame.dispose();
         tfa.frame = null;         
      }
   }
}
