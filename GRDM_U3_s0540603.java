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
		//IJ.open("Z:/Pictures/Beispielbilder/orchid.jpg");

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
				System.out.println("Selected: " + item.toString());
				method = item.toString();
				changePixelValues(imp.getProcessor());
				imp.updateAndDraw();
			} 

		}


		private void changePixelValues(ImageProcessor ip) {

			// Array zum ZurÃ¼ckschreiben der Pixelwerte
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
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						//int g = (argb >>  8) & 0xff;
						//int b =  argb        & 0xff;

						int rn = r;
						int gn = 0;
						int bn = 0;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}	
			}
			
			if (method.equals("Negativ")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 8) & 0xff;
						int b = (argb >> 0) & 0xff;

						int rn = 255-r; //Wert invertiert
						int gn = 255-g; //Wert invertiert
						int bn = 255-b; //Wert invertiert

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}	
			}
			
			if (method.equals("Graustufen")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz

						int rn = Y; //Rotwert wird mit der Luminanz gleichgesetzt
						int gn = Y; //Grünwert wird mit der Luminanz gleichgesetzt
						int bn = Y; //Blauwert wird mit der Luminanz gleichgesetzt

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}			

			if (method.equals("Binär")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz
						
						if (Y >= 128) {	//Alle Pixel mit einer Luminanz größer als 127 werden schwarz eingefärbt						
							r = 255;
							g = 255;
							b = 255;
						}
						else {			//Alle Pixel mit einer Luminanz kleiner als 122 werden weiß eingefärbt
							r = 0;
							g = 0;
							b = 0;
						}

						int rn = r;
						int gn = g;
						int bn = b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("5 Töne")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz
						
						int calc1 = (int) (Y * 4 / 255 + 0.5); 	//Die Luminanz wird in 5 Blocke geteilt
						int calc2 = calc1 * 255 / 4;			//Jeder Pixel wird einem Block zugeteilt
												
							r = calc2;
							g = calc2;
							b = calc2;
						
						int rn = r;
						int gn = g;
						int bn = b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}

			if (method.equals("10 Töne")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b); //Formel zur Berechnung der Luminanz
						
						int calc1 = (int) (Y * 9 / 255 + 0.5); 	//Die Luminanz wird in 10 Blocke geteilt
						int calc2 = calc1 * 255 / 9;			//Jeder Pixel wird einem Block zugeteilt
												
							r = calc2;
							g = calc2;
							b = calc2;
						
						int rn = r;
						int gn = g;
						int bn = b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Binär mit Diffusion")) {
				
				int New;
				int Y = 0;

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
												
						if (Y > 128){
							New =  (int) ((0.299*r + 0.587*g + 0.114*b) - (255 - Y));
						}
						
						else {
							New =  (int) ((0.299*r + 0.587*g + 0.114*b) + Y);	
						}

						Y = (int) (0.299*r + 0.587*g + 0.114*b);
						
						System.out.println(Y);
						
						if (New >= 128) {							
							r = 255;
							g = 255;
							b = 255;
						}
						else {
							r = 0;
							g = 0;
							b = 0;
						}

						int rn = r;
						int gn = g;
						int bn = b;
						

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
			
			if (method.equals("Sepia")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b)*9; //Die Helligkeit wird angepasst

						int rn = (int) (Y * 0.112); //Farbwert wird angepasst
						int gn = (int) (Y * 0.066);	//Jeder Pixel wird einem Block zugeteilt
						int bn = (int) (Y * 0.020);	//Jeder Pixel wird einem Block zugeteilt

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
						
					}
				}
			}
			
			if (method.equals("Sechs Farben")) {

				for (int y=0; y<height; y++) {
					for (int x=0; x<width; x++) {
						int pos = y*width + x;
						int argb = origPixels[pos];  // Lesen der Originalwerte 

						int r = (argb >> 16) & 0xff;
						int g = (argb >> 16) & 0xff;
						int b = (argb >> 16) & 0xff;
						
						int Y = (int) (0.299*r + 0.587*g + 0.114*b);
						
						int calc1 = (int) (Y * 4 / 255 + 0.5);
						int calc2 = calc1 * 255 / 4;
												
							r = calc2;
							g = calc2;
							b = calc2;
						
						int rn = r;
						int gn = g;
						int bn = b;

						// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden

						pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
					}
				}
			}
		}
	} // CustomWindow inner class
} 
