/**
 * 
 */
package ch.unizh.ini.jaer.projects.apsdvsfusion;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelCreator.FiringModelType;
import ch.unizh.ini.jaer.projects.apsdvsfusion.gui.ParameterBrowserPanel;

/**
 * @author Dennis
 *
 */
public abstract class SchedulableFiringModelMap extends FiringModelMap {

	
	DynamicHeap<PostponedFireEvent> heap = new DynamicHeap<PostponedFireEvent>();
	
	/**
	 * 
	 */
	public SchedulableFiringModelMap(int sizeX, int sizeY, SignalHandler spikeHandler, Preferences parentPrefs, String nodeName) {
		super(sizeX, sizeY, spikeHandler, parentPrefs, nodeName);
	}
	public SchedulableFiringModelMap(int sizeX, int sizeY, SignalHandler spikeHandler, Preferences prefs) {
		super(sizeX, sizeY, spikeHandler, prefs);
	}
	
	public void clearHeap() {
		if (heap != null) {
			synchronized (heap) {
				heap.clear();
			}
			
		}
	}
	
	public void processScheduledEvents(int uptoTime) {
		if (enabled) {
			while (!heap.isEmpty() && heap.peek().getContent().getFireTime() <= uptoTime) {
				PostponedFireEvent event = heap.poll().getContent();
				// TODO: 
				event.getFiringModel().executeScheduledEvent(event.getFireTime());
			}
		} 
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		clearHeap();
	}

	public DynamicHeap<PostponedFireEvent>.Entry createEntry(PostponedFireEvent event) {
		return heap.createEntry(event);
	}
	/* (non-Javadoc)
	 * @see ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelMap#get(int, int)
	 */
////	@Override
//	public abstract SchedulableFiringModel get(int x, int y);

	/* (non-Javadoc)
	 * @see ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelMap#getSizeX()
	 */

	/* (non-Javadoc)
	 * @see ch.unizh.ini.jaer.projects.apsdvsfusion.FiringModelMap#reset()
	 */
//	@Override
//	public void reset() {
//		super.reset();
//		// TODO Auto-generated method stub
//
//	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
