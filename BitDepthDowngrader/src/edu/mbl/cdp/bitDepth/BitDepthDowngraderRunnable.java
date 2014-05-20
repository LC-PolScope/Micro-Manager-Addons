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

import static edu.mbl.cdp.bitDepth.BitDepthDowngraderProcessor.BIT_DEPTH;
import static edu.mbl.cdp.bitDepth.BitDepthDowngraderProcessor.PIX_TYPE;
import static edu.mbl.cdp.bitDepth.BitDepthDowngraderProcessor.PIX_TYPE_GRAY_8;
import ij.ImagePlus;
import mmcorej.CMMCore;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.utils.ReportingUtils;

public class BitDepthDowngraderRunnable implements Runnable {

    CMMCore core_;     
    AcquisitionWrapperEngine engineWrapper_;
    boolean isDisplayControlsEnabled = false;
    BitDepthDowngrader fa;
    
    
    BitDepthDowngraderRunnable(BitDepthDowngrader fa) {
        this.fa = fa;
        this.core_ = fa.core_;
        this.engineWrapper_ = fa.engineWrapper_;
        
        fa.getDebugOptions();        
    }
    
    
    @Override
    public void run() {
        changeSummary();
    }

    public void changeSummary() {
        try {            
            engineWrapper_.getSummaryMetadata().put(BIT_DEPTH, fa.processor.imgChangeToBitDepth);
            engineWrapper_.getSummaryMetadata().put(PIX_TYPE, PIX_TYPE_GRAY_8);
            engineWrapper_.getSummaryMetadata().put("IJType", ImagePlus.GRAY8);

        } catch (Exception ex) {
            ex.printStackTrace();
            ReportingUtils.logMessage("BitDepthDowngrader Error");
        }
    }
    
    
}
