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

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.micromanager.utils.ReportingUtils;

public class About extends javax.swing.JFrame {

  public static final String BugPageLink_ = "http://www.openpolscope.org/mantis/";
  public static final String BugPageLinkLABEL_ = "<html><a href=" + BugPageLink_ + ">Web Page</a>";
  
  public static final String WebPageLink_ = "http://www.openpolscope.org/pages/MMPlugin_Frame_Averager.htm";
  public static final String WebPageLinkLABEL_ = "<html><a href=" + WebPageLink_ + ">Web Page</a>";
    
  public About(JFrame frame) {
    initComponents();
    setIcon();    
    
    setCopyRight();
    setLicenseText();
    Rectangle rec = frame.getBounds();
    this.setBounds(rec.x, rec.y, WIDTH, HEIGHT);
    this.setVisible(true);
  }  
  
  private void setIcon() {
    URL url = this.getClass().getResource("frameIcon.png");
    Image im = Toolkit.getDefaultToolkit().getImage(url);
    setIconImage(im);
  }
  
  private void setLicenseText() {
    try {
      String text = textPaneLicense.getText();
      textPaneLicense.setText(text);   
      textPaneLicense.setCaretPosition(0);
      
      SimpleAttributeSet sa = new SimpleAttributeSet();
      StyleConstants.setAlignment(sa, StyleConstants.ALIGN_JUSTIFIED);
      textPaneLicense.getStyledDocument().setParagraphAttributes(0,text.length(),sa,false);
    }
    catch (Exception ex) {ReportingUtils.showError(ex);}
  }
  
  private void setCopyRight() {
      
      Calendar cal = Calendar.getInstance();
      int year = cal.get(Calendar.YEAR);
      String str = "Marine Biological Laboratory © 2009 - " + year;
      labelFooter.setText(str);
  }
  
  public void setDialogueName(String text) {
    labelDialogueName.setText("About: FrameAverager");    
  }    
  
  public static void openHttpUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelDialogueName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        textPaneLicense = new javax.swing.JTextPane();
        labelVersion = new javax.swing.JLabel();
        labelFooter = new javax.swing.JLabel();
        labelWebPage = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setMinimumSize(new java.awt.Dimension(410, 465));

        labelDialogueName.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        labelDialogueName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelDialogueName.setText("FrameAverager");

        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        textPaneLicense.setEditable(false);
        textPaneLicense.setText("﻿Copyright © 2009 - 2013, Marine Biological Laboratory\n\nLICENSE (Berkeley Software Distribution License): Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n3. Neither the name of the Marine Biological Laboratory nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n\nThe views and conclusions contained in the software and documentation are those of the authors and should not be interpreted as representing official policies, either expressed or implied, of any organization.\n\nDeveloped at the Laboratory of Rudolf Oldenbourg at the Marine Biological Laboratory in Woods Hole, MA.\n\nSoftware Developers: Amitabh Verma & Grant Harris");
        textPaneLicense.setAutoscrolls(false);
        jScrollPane1.setViewportView(textPaneLicense);

        labelVersion.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelVersion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelVersion.setText("Version 1.03b (Compatible for Micro-Manager 1.4.13 - 1.4.15)");

        labelFooter.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelFooter.setText("Marine Biological Laboratory © 2009 - 2014");

        labelWebPage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelWebPage.setText("<html><a href=\"\">Web Page</a>");
        labelWebPage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelWebPageMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                labelWebPageMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                labelWebPageMouseExited(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("<html><a href=\"\">Bugs and Feature Requests</a>");
        jLabel2.setToolTipText("Email: feedback@openpolscope.org");
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel2MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel2MouseExited(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(labelDialogueName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelFooter, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                    .addComponent(labelWebPage)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(labelDialogueName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelVersion)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelWebPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelFooter)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void labelWebPageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelWebPageMouseClicked
    openHttpUrl(WebPageLink_);
  }//GEN-LAST:event_labelWebPageMouseClicked

  private void labelWebPageMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelWebPageMouseEntered
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }//GEN-LAST:event_labelWebPageMouseEntered

  private void labelWebPageMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelWebPageMouseExited
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }//GEN-LAST:event_labelWebPageMouseExited

  private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
    openHttpUrl(BugPageLink_);
  }//GEN-LAST:event_jLabel2MouseClicked

  private void jLabel2MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseEntered
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }//GEN-LAST:event_jLabel2MouseEntered

  private void jLabel2MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseExited
    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }//GEN-LAST:event_jLabel2MouseExited

  /**
   * @param args the command line arguments
   */
//  public static void main(String args[]) {
//    /*
//     * Set the Nimbus look and feel
//     */
//    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /*
//     * If Nimbus (introduced in Java SE 6) is not available, stay with the
//     * default look and feel. For details see
//     * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
//     */
//    try {
//      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//        if ("Nimbus".equals(info.getName())) {
//          javax.swing.UIManager.setLookAndFeel(info.getClassName());
//          break;
//        }
//      }
//    } catch (ClassNotFoundException ex) {
//      java.util.logging.Logger.getLogger(About.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//    } catch (InstantiationException ex) {
//      java.util.logging.Logger.getLogger(About.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//    } catch (IllegalAccessException ex) {
//      java.util.logging.Logger.getLogger(About.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//      java.util.logging.Logger.getLogger(About.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//    }
//    //</editor-fold>
//
//    /*
//     * Create and display the form
//     */
//    java.awt.EventQueue.invokeLater(new Runnable() {
//
//      public void run() {
//        new About().setVisible(true);
//      }
//    });
//  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelDialogueName;
    private javax.swing.JLabel labelFooter;
    private javax.swing.JLabel labelVersion;
    private javax.swing.JLabel labelWebPage;
    private javax.swing.JTextPane textPaneLicense;
    // End of variables declaration//GEN-END:variables
}
