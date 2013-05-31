import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
 */
public class GRDM_U5_s0539848 implements PlugIn {
	String[] items = {"Original", "Weichzeichner"};
	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;


	public static void main(String args[]) {
		IJ.open("sail.jpg");

		GRDM_U5_s0539848 pw = new GRDM_U5_s0539848();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null)
			return;
		
		CustomCanvas cc = new CustomCanvas(imp);
		storePixelValues(imp.getProcessor());
		new CustomWindow(imp, cc);
	}


	private void storePixelValues(ImageProcessor ip) {
		width = ip.getWidth();
		height = ip.getHeight();

		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {
		CustomCanvas(ImagePlus imp) {
			super(imp);
		}

	} // CustomCanvas inner class


	class CustomWindow extends ImageWindow implements ItemListener {
		private String method;
		
		CustomWindow(ImagePlus imp, ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			//JPanel panel = new JPanel();
			Panel panel = new Panel();

			JComboBox cb = new JComboBox(items);
			panel.add(cb);
			cb.addItemListener(this);

			add(panel);
			pack();
		}

		public void itemStateChanged(ItemEvent evt) {
			// Get the affected item
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED) {
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 
		}


		private void changePixelValues(ImageProcessor ip) {
			// Array zum Zurückschreiben der Pixelwerte
			int[] pixels = (int[])ip.getPixels();

			if (method.equals("Original")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						pixels[pos] = origPixels[pos];
					}
				}
			}
			
			
			if (method.equals("Weichzeichner")) {
				for (int y=0; y<height; y++) {		
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int[] ges = {0,0,0,0}; // amount rgb
						ges = addPixel(pos, ges); // pixel itself
						
						if (x>0) {
											ges = addPixel(pos-1, ges); 	  // links
							if (y>0) 		ges = addPixel(pos-width-1, ges); // l. oben
							if (y<height-1) ges = addPixel(pos+width-1, ges); // l. unten
						}
						
						if (x<width-1) { 
											ges = addPixel(pos+1, ges); 	  // rechts
							if (y>0) 		ges = addPixel(pos-width+1, ges); // r. soben
							if (y<height-1) ges = addPixel(pos+width+1, ges); // r. unten
						}
						
						if (y>0)		ges = addPixel(pos-width, ges); // oben
						if (y<height-1)	ges = addPixel(pos+width, ges); // unten
						
						
						ges[1]/=ges[0]; ges[2]/=ges[0]; ges[3]/=ges[0];
						pixels[pos] = (0xFF<<24) | (ges[1]<<16) | (ges[2] << 8) | ges[3];
					}
				}
			}
		}
		
		private int[] addPixel(int pos, int[] ges) {
			int argb=origPixels[pos];
			ges[0]++;
			ges[1]+= (argb >> 16) & 0xff;
			ges[2]+= (argb >>  8) & 0xff;
			ges[3]+=  argb        & 0xff;
			return ges;
		}
	}
} 
