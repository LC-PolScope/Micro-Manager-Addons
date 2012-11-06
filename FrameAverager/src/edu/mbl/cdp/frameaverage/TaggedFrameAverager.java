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
import javax.swing.JFrame;
import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.api.MMPlugin;
import org.micromanager.api.ScriptInterface;

/**
 * Multiple-Frame Averaging plug-in for Micromanager
 *
 *
 * @author Amitabh Verma, averma@mbl.edu
 * @author Grant Harris, gharris@mbl.edu at the Marine Biological Laboratory,
 * Woods Hole, Mass.
 */
public class TaggedFrameAverager implements MMPlugin {

    public static final String menuName = "Frame Averager";
    public static final String tooltipDescription = "Multiple-Frame Averaging";
    //
    FrameAverager fa;
    ScriptInterface gui;

    @Override
    public void setApp(ScriptInterface si) {
        gui = si;
        
        AcquisitionWrapperEngine engineWrapper = (AcquisitionWrapperEngine) gui.getAcquisitionEngine();
        fa = new FrameAverager(engineWrapper, gui.getMMCore());
    }

    @Override
    public void dispose() {
    }

    @Override
    public void show() {
        JFrame frame = fa.getControlFrame();
        gui.addMMBackgroundListener(frame);
        frame.setVisible(true);
    }

    @Override
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
