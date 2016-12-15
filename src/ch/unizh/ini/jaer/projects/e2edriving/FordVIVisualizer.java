/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.unizh.ini.jaer.projects.e2edriving;

import com.jogamp.opengl.GLAutoDrawable;
import eu.visualize.ini.convnet.DavisDeepLearnCnnProcessor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventio.AEFileInputStream;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.AEViewer;
import net.sf.jaer.graphics.FrameAnnotater;

/**
 * Reads Ford VI (vehicle interface) log files to display vehicle data over
 * recording
 *
 * @author tobi, jbinas
 */
public class FordVIVisualizer extends EventFilter2D implements FrameAnnotater, PropertyChangeListener {

    private long aeDatStartTimeMs = 0;
    private File fordViFile = null;
    String lastFordVIFile = getString("lastFordVIFile", null);
    private boolean addedPropertyChangeListener=false;
    BufferedInputStream fordViInputStream=null;
    private boolean showSteering=getBoolean("showSteering",true);
    private boolean showThrottleBrake=getBoolean("showThrottleBrake",true);
    private boolean showGPS=getBoolean("showGPS",true);

    public FordVIVisualizer(AEChip chip) {
        super(chip);
    }

    @Override
    public EventPacket<?> filterPacket(EventPacket<?> in) {
        if(!addedPropertyChangeListener){
            addedPropertyChangeListener=true;
            chip.getAeViewer().addPropertyChangeListener(AEViewer.EVENT_FILEOPEN, this);
        }
        if (chip.getAeViewer().getPlayMode() != AEViewer.PlayMode.PLAYBACK) {
            return in;
        }
        if (!in.isEmpty()) {
            int lastTs = in.getLastTimestamp();
        }
        return in; // only annotates
    }

    @Override
    public void resetFilter() {
    }

    @Override
    public void initFilter() {
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
    }

    private long parseDataStartTimeFromAeDatFile(AEFileInputStream aeis) throws FileNotFoundException, IOException{
        // # DataStartTime: System.currentTimeMillis() 1481800498468
        File f=aeis.getFile();
        LineNumberReader is=new LineNumberReader(new InputStreamReader(new FileInputStream(f)));
        while(is.getLineNumber()<5000){
            String line=is.readLine();
            if(line.contains("DataStartTime")){
                Scanner s=new Scanner(line);
                aeDatStartTimeMs=s.nextLong();
                return aeDatStartTimeMs;
            }
        }
        log.warning("could not find data start time DataStartTime in AEDAT file");
        return -1;
        
    }
   
   
    synchronized public void doOpenFordVIFile() {
        JFileChooser c = new JFileChooser(lastFordVIFile);
        FileFilter filt = new FileNameExtensionFilter("dat file", "dat");
        c.addChoosableFileFilter(filt);
        c.setFileFilter(filt);
        c.setSelectedFile(new File(lastFordVIFile));
        int ret = c.showOpenDialog(chip.getAeViewer());
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }
        lastFordVIFile = c.getSelectedFile().toString();
        putString("lastFordVIFile", lastFordVIFile);
        try {
            fordViFile = c.getSelectedFile();
            fordViInputStream=new BufferedInputStream(new FileInputStream(fordViFile));
        } catch (Exception ex) {
            Logger.getLogger(DavisDeepLearnCnnProcessor.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(chip.getAeViewer().getFilterFrame(), "Couldn't load from this file, caught exception " + ex + ". See console for logging.", "Bad data file", JOptionPane.WARNING_MESSAGE);
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if(pce.getPropertyName()==AEViewer.EVENT_FILEOPEN){
            int fileStartTs=chip.getAeInputStream().getFirstTimestamp();
        }
        
    }

    /**
     * @return the showSteering
     */
    public boolean isShowSteering() {
        return showSteering;
    }

    /**
     * @param showSteering the showSteering to set
     */
    public void setShowSteering(boolean showSteering) {
        this.showSteering = showSteering;
        putBoolean("showSteering",showSteering);
    }

    /**
     * @return the showThrottleBrake
     */
    public boolean isShowThrottleBrake() {
        return showThrottleBrake;
    }

    /**
     * @param showThrottleBrake the showThrottleBrake to set
     */
    public void setShowThrottleBrake(boolean showThrottleBrake) {
        this.showThrottleBrake = showThrottleBrake;
        putBoolean("showThrottleBrake",showThrottleBrake);
    }

    /**
     * @return the showGPS
     */
    public boolean isShowGPS() {
        return showGPS;
    }

    /**
     * @param showGPS the showGPS to set
     */
    public void setShowGPS(boolean showGPS) {
        this.showGPS = showGPS;
        putBoolean("showGPS", showGPS);
    }

}