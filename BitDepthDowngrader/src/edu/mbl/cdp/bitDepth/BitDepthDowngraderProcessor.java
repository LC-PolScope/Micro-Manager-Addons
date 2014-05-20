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

import ij.ImagePlus;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.ScriptInterface;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

public class BitDepthDowngraderProcessor extends DataProcessor<TaggedImage> {

    public static final String PIX_TYPE_GRAY_16 = "GRAY16";
    public static final String PIX_TYPE_GRAY_8 = "GRAY8";
    public static final String PIX_TYPE = "PixelType";
    public static final String BIT_DEPTH = "BitDepth";
    int imgCurrentDepth;
    TaggedBitDepthDowngrader tfa;
    int imgChangeToBitDepth = 8;    
    int imgCurrentToBitDepth = 8;    

    @Override
    public void setApp(ScriptInterface gui) {
        gui_ = gui;
        tfa = new TaggedBitDepthDowngrader();
        tfa.setApp(gui_);
        tfa.fa.processor = this;   
        tfa.frame = tfa.fa.getControlFrame();
//        tfa.fa.controlFrame_.setPluginEnabled(true);
   }

    @Override
    protected void process() {
        try {            
            final TaggedImage taggedImage = poll();
            
            if (tfa.fa.engineWrapper_.isAcquisitionRunning() && !tfa.fa.isEnabledForImageAcquisition) {
                produce(taggedImage);
                return;
            }
            if (taggedImage==null) {
                return;
            }
            if (TaggedImageQueue.isPoison(taggedImage)) { // EOL check
                produce(taggedImage);
                return;
            }
                        
            imgCurrentDepth = MDUtils.getDepth(taggedImage.tags);
            if (!gui_.isLiveModeOn() && (imgCurrentDepth == 2 || (taggedImage.pix instanceof short[]))) {
                computeAndProduce(taggedImage);
            } else {
                produce(taggedImage);
            }

        } catch (Exception ex) {
            ReportingUtils.logError("ERROR: in Process: ");
            ex.printStackTrace();
            produce(TaggedImageQueue.POISON);
        }
    }
    
    private void computeAndProduce(TaggedImage taggedImageArrayTemp) {

        try {
            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("FrameAvg: computing...");
            }

            int width = MDUtils.getWidth(taggedImageArrayTemp.tags);
            int height = MDUtils.getHeight(taggedImageArrayTemp.tags);
            //tfa.getCMMCore().logMessage(MDUtils.getChannelName(taggedImage.tags));

            int dimension = width * height;
            short[] pixS;
            byte[] retB = new byte[dimension];
            float retF;
            Object result = null;


            pixS = (short[]) taggedImageArrayTemp.pix;
            for (int j = 0; j < dimension; j++) {
                retF = (float) (pixS[j] & 0xffff);
                retB[j] = (byte) (int) (retF * 255 / 65535);
            }

            result = retB;

            JSONObject tags = new JSONObject(taggedImageArrayTemp.tags.toString());
            tags.put(BIT_DEPTH, imgChangeToBitDepth);
            tags.put(PIX_TYPE, PIX_TYPE_GRAY_8);
            tags.put("IJType", ImagePlus.GRAY8);
            TaggedImage averagedImage = new TaggedImage(result, tags);
            produce(averagedImage);            

            if (tfa.fa.debugLogEnabled_) {
                ReportingUtils.logMessage("produced image");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logError("Error: while producing img.");
            produce(taggedImageArrayTemp);            
        }
    }
    
    public DataProcessor<TaggedImage> getDataProcessor() {
        return (DataProcessor<TaggedImage>) this;
    }
    
    @Override
   public void makeConfigurationGUI() {
        if (tfa==null) {
            tfa = new TaggedBitDepthDowngrader();
            tfa.setApp(gui_);
            tfa.fa.processor = this;
            tfa.frame = tfa.fa.getControlFrame();
            tfa.gui.addMMBackgroundListener(tfa.frame);
        } else if (tfa.frame==null) {            
            tfa.frame = tfa.fa.getControlFrame();
            tfa.gui.addMMBackgroundListener(tfa.frame);
        }
        tfa.frame.setVisible(true);
//        tfa.fa.controlFrame_.setPluginEnabled(true);
   }
    
    @Override
   public void dispose() {
      if (tfa.frame != null) {
         tfa.frame = null;    
         tfa.fa.stopAndClearRunnable();
         tfa.fa.stopAndClearProcessor();
      }
   }
}
