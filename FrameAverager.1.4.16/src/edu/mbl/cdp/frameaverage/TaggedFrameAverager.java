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

import javax.swing.JFrame;
import mmcorej.CMMCore;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

public class TaggedFrameAverager implements MMPlugin {

    public static final String menuName = "Frame Averager";
    public static final String tooltipDescription = "Multiple-Frame Averaging";
    //
    public static JFrame frame;
    FrameAverager fa;
    static ScriptInterface gui;    
    
    @Override
    public void setApp(ScriptInterface si) {
        gui = si;                
        fa = new FrameAverager(getAcquisitionWrapperEngine(), gui.getMMCore());
    }    
    
    public static AcquisitionWrapperEngine getAcquisitionWrapperEngine() {
        AcquisitionWrapperEngine engineWrapper = (AcquisitionWrapperEngine) MMStudioMainFrame.getInstance().getAcquisitionEngine();
        return engineWrapper;
    }
    
    public static CMMCore getCMMCore() {       
        return gui.getMMCore();
    }

    @Override
    public void dispose() {
    }

    @Override
    public void show() {
        if (frame == null) {
            frame = fa.getControlFrame();
            gui.addMMBackgroundListener(frame);
            frame.setLocation(fa.controlFrame_.FrameXpos, fa.controlFrame_.FrameYpos);
        }
        frame.setVisible(true);
    }

//    @Override
    public void configurationChanged() {
    }

    @Override
    public String getDescription() {
        return tooltipDescription;
    }

    @Override
    public String getInfo() {
        return "Frame Averager Plugin";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getCopyright() {
        return "MBL, 2012";
    }
}
