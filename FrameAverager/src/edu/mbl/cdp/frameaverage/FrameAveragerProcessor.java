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
 */

import mmcorej.CMMCore;
import mmcorej.TaggedImage;
import org.json.JSONObject;
import org.micromanager.acquisition.TaggedImageQueue;
import org.micromanager.api.DataProcessor;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 * FrameAveragerProcessor is the DataProcessor<TaggedImage> that 
 * does the averaging...
 * 
 * @author Amitabh Verma, averma@mbl.edu
 * @author Grant Harris, gharris@mbl.edu 
 * at the Marine Biological Laboratory, Woods Hole, Mass.
 */

public class FrameAveragerProcessor extends DataProcessor<TaggedImage> {

	CMMCore core_;
	JSONObject json = null;
	public static TaggedImage POISON = new TaggedImage(null, null);
	double exposure;
	
        //boolean isDisplayAvailable = false;
	//int numberFrames_ = 1;
	int imgDepth;
	int iNO = 0;
	FrameAverager fa;

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
			if (taggedImage == null) { // EOL check
				produce(POISON);
				return;
			}
			if (TaggedImageQueue.isPoison(taggedImage)) { // EOL check
				produce(taggedImage);
				return;
			}

			if (fa.numberFrames < 2) { // if MFA is disabled
				produce(taggedImage);
                                if (fa.debugLogEnabled_) {
                                    ReportingUtils.logMessage("FrameAvg: averaging disabled");
                                }
				return;
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
                        
                    fa.taggedImageArray[0] = taggedImage;
                    compute(fa.taggedImageArray); // on to computing avg. frame
                    //isDisplayAvailable = true;
                    if (fa.debugLogEnabled_) {
                        core_.logMessage("FrameAvg: exiting processor");
                    }

		} catch (Exception ex) {
			ReportingUtils.logError("ERROR: FrameAvg, in Process: ");
			ex.printStackTrace();
			produce(POISON);
		}
	}

	private void compute(TaggedImage[] taggedImageArrayTemp) {   
            
		try {
                        if (fa.debugLogEnabled_) {
                            ReportingUtils.logMessage("FrameAvg: computing...");
                        }
			TaggedImage taggedImage = taggedImageArrayTemp[0];

			int width = MDUtils.getWidth(taggedImage.tags);
			int height = MDUtils.getHeight(taggedImage.tags);
                        core_.logMessage(MDUtils.getChannelName(taggedImage.tags));

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
						retF[j] = (float) (retF[j] + (int)(pixS[j] & 0xffff));
					}
				}
			}
			if (imgDepth == 1) {
				for (int j = 0; j < dimension; j++) {
					retB[j] = (byte) (int)(retF[j] / fa.numberFrames);
				}
				result = retB;
			} else if (imgDepth == 2) {
				for (int j = 0; j < dimension; j++) {
					retS[j] = (short) (int)(retF[j] / fa.numberFrames);
				}
				result = retS;
			}
			// Averaged channel
			// Weird way of copying a JSONObject
			JSONObject tags = new JSONObject(taggedImage.tags.toString());
			tags.put(FrameAverager.METADATAKEY, fa.numberFrames);
			TaggedImage averagedImage = new TaggedImage(result, tags);
			produce(averagedImage);
                        if (fa.debugLogEnabled_) {
                            ReportingUtils.logMessage("FrameAvg: produced averaged image");
                        }
		} catch (Exception ex) {
			ex.printStackTrace();
			ReportingUtils.logError("Error: FrameAvg, while producing averaged img.");
			produce(taggedImageArrayTemp[0]);
		}                
	}

	public DataProcessor<TaggedImage> getDataProcessor() {
		return (DataProcessor<TaggedImage>) this;
	}

}
