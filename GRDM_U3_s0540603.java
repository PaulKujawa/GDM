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
public class GRDM_U3_s0540603 implements PlugIn {

	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;

	String[] items = {"Original", "Rot-Kanal", "Negativ", "Graustufen", "Binär", "5 Töne", "10 Töne", "Binär mit Diffusion", "Sepia", "Sechs Farben"};


	public static void main(String args[]) {

		IJ.open("bear.jpg");
		GRDM_U3_s0540603 pw = new GRDM_U3_s0540603();
		pw.imp = IJ.getImage();
		pw.run("");
	}

	public void run(String arg) {
		if (imp==null) 
			imp = WindowManager.getCurrentImage();
		if (imp==null) {
			return;
		}
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
				//System.out.println("Selected: " + item.toString());
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
			
			
			if (method.equals("Rot-Kanal")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int rn = (argb >> 16) & 0xff;
						int gn = 0;
						int bn = 0;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}	
			}
			
			
			if (method.equals("Negativ")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;

						// invertieren
						int rn = 255-r;
						int gn = 255-g;
						int bn = 255-b;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}	
			}
			

			if (method.equals("Graustufen")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz

						// mit luminanz gleichsetzen
						int rn = Y;
						int gn = Y;
						int bn = Y;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}			

			
			if (method.equals("Binär")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); // Luminanz
						int rn=0, gn=0, bn=0; // weiß
						
						// Y >= 128 = schwarz
						if (Y >= 128) {						
							rn = 255;
							gn = 255;
							bn = 255;
						}
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			
			if (method.equals("5 Töne")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); // Luminanz
						
						int calc1 = (int) (Y * 4 / 255 + 0.5); 	//Die Luminanz wird in 5 Blocke geteilt
						int calc2 = calc1 * 255 / 4;			//Jeder Pixel wird einem Block zugeteilt
												
						int rn = calc2;
						int gn = calc2;
						int bn = calc2;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			
			if (method.equals("10 Töne")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz
						
						int calc1 = (int) (Y * 9 / 255 + 0.5); 	//Die Luminanz wird in 10 Blocke geteilt
						int calc2 = calc1 * 255 / 9;			//Jeder Pixel wird einem Block zugeteilt
												
						int rn = calc2;
						int gn = calc2;
						int bn = calc2;

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Binär mit Diffusion")) {
				
				int Y = 0;
				int dif = 0;;

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						if (x==0)
							dif = 0;
						else if (Y > 128)
							dif =  (-255 + Y);
						else
							dif = Y;
						
						Y = (int) (0.299*r + 0.587*g + 0.114*b + dif);
						
						int rn=0, gn=0, bn=0;
						
						if (Y >= 128) {							
							rn = 255;
							gn = 255;
							bn = 255;
						}
						else {
							rn = 0;
							gn = 0;
							bn = 0;
						}
						
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Sepia")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];   

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b)*9; //Die Helligkeit wird angepasst

						int rn = (int) (Y * 0.112); //Farbwert wird angepasst
						int gn = (int) (Y * 0.066);	//Farbwert wird angepasst
						int bn = (int) (Y * 0.020);	//Farbwert wird angepasst

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			
			if (method.equals("Sechs Farben")) {
				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos]; 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;
						
						int[] colors = getColor(r,g,b);
						int rn = colors[0];
						int gn = colors[1];
						int bn = colors[2];

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
		}
	}

	
	private int[] getColor (int r, int g, int b) {
		int[][] colors = {{211,209,208}, {159,141,128}, {138,155,173}, {55,98,130}, {32,35,33}, {93,83,75}};
		int[] rgb = {0,0,0};
		int min = 0;
		
		for (int[] i : colors) {	
			if (min == 0) {
				min = Math.abs(r-i[0]) + Math.abs(g-i[1]) + Math.abs(b-i[2]);
				rgb[0]=i[0]; rgb[1]=i[1]; rgb[2]=i[2];
			}
			else {
				int neu = Math.abs(r-i[0]) + Math.abs(g-i[1]) + Math.abs(b-i[2]);
				if (min > neu) {
					min = neu;
					rgb[0]=i[0]; rgb[1]=i[1]; rgb[2]=i[2];
				}
			}
		}
		return rgb;
	}
} 
