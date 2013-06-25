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
	String[] items = {"Original", "Weichzeichner", "Hochpassfilter", "Verst‰rkte Kanten"};
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
			// Array zum Zur√ºckschreiben der Pixelwerte
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
						int[] rgb = {0,0,0};
						short pixelDiv = 0;
						
						for (int py=y-1; py <= y+1; py++) {
							for (int px=x-1; px <= x+1; px++) {
								int[] ret = addPixel(px, py, rgb);
								if (ret != null) {
									rgb = ret;
									pixelDiv++;
								}
							}
						}
						
						// 1/9
						rgb[0]/=pixelDiv; 
						rgb[1]/=pixelDiv; 
						rgb[2]/=pixelDiv;
						pixels[pos] = (0xFF<<24) | (rgb[0]<<16) | (rgb[1] << 8) | rgb[2];
					}
				}
			}
			
			
			else if (method.equals("Hochpassfilter")) {
				for (int y=0; y<height; y++) {	
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int[] rgb = {0,0,0};
						short pixelDiv = 1;
						
						for (int py=y-1; py <= y+1; py++) {
							for (int px=x-1; px <= x+1; px++) {
								
								if (py!=y || px!=x) { // mitte auslassen
									int[] ret = addPixel(px, py, rgb);
									if (ret != null) {
										rgb = ret;
										pixelDiv++;
									}
								}
							}
						}
						
						// rand is negativ -1/9
						int rand[] = {-rgb[0]/pixelDiv, -rgb[1]/pixelDiv, -rgb[2]/pixelDiv}; 
						
						// mitte ist positiv 8/9
						int[] mitte = {0,0,0};
						mitte = addPixel(x, y, mitte);
						mitte[0] *= (pixelDiv-1d)/pixelDiv;
						mitte[1] *= (pixelDiv-1d)/pixelDiv;
						mitte[2] *= (pixelDiv-1d)/pixelDiv;
						
						// ges
						rgb[0] = normalize(rand[0]+mitte[0]+128);
						rgb[1] = normalize(rand[1]+mitte[1]+128);
						rgb[2] = normalize(rand[2]+mitte[2]+128);
						
						pixels[pos] = (0xFF<<24) | (rgb[0]<<16) | (rgb[1] << 8) | rgb[2];
					}
				}
			}
			
			
			else if (method.equals("Verst‰rkte Kanten")) {
				for (int y=0; y<height; y++) {	
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int[] rgb = {0,0,0};
						short pixelDiv = 1;
						
						for (int py=y-1; py <= y+1; py++) {
							for (int px=x-1; px <= x+1; px++) {
								
								if (py!=y || px!=x) { // mitte auslassen
									int[] ret = addPixel(px, py, rgb);
									if (ret != null) {
										rgb = ret;
										pixelDiv++;
									}
								}
							}
						}
						
						// rand is negativ -1/9
						int rand[] = {-rgb[0]/pixelDiv, -rgb[1]/pixelDiv, -rgb[2]/pixelDiv}; 
						
						// mitte ist positiv 8/9
						int[] mitte = {0,0,0};
						mitte = addPixel(x, y, mitte);
						mitte[0] *= (pixelDiv+8.0)/pixelDiv;
						mitte[1] *= (pixelDiv+8.0)/pixelDiv;
						mitte[2] *= (pixelDiv+8.0)/pixelDiv;
						
						// ges
						rgb[0] = normalize(rand[0]+mitte[0]);
						rgb[1] = normalize(rand[1]+mitte[1]);
						rgb[2] = normalize(rand[2]+mitte[2]);
						
						pixels[pos] = (0xFF<<24) | (rgb[0]<<16) | (rgb[1] << 8) | rgb[2];
					}
				}
			}
		}
		
		private int normalize(int n){
			if (n<0)
				return 0;
			else if (n>255)
				return 255;
			else return n;
		}
		
		
		private int[] addPixel(int px, int py, int[] rgb) {
			if (px < 0 || px >= width || py < 0 || py >= height)
				return null;
			
			if (py>0) py--;
			int argb=origPixels[py*width +px];
			rgb[0]+= (argb >> 16) & 0xff;
			rgb[1]+= (argb >>  8) & 0xff;
			rgb[2]+=  argb        & 0xff;
			return rgb;
		}
	}
} 
