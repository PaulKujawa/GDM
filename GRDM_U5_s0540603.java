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
public class GRDM_U5_s0540603 implements PlugIn {
	String[] items = {"Original", "Weichzeichner", "Hochpassfilter", "verst‰rkte Kanten"};
	ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;


	public static void main(String args[]) {

		IJ.open("sail.jpg");

		GRDM_U5_s0540603 pw = new GRDM_U5_s0540603();
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

			
			/*
			 * "Weichzeichner"
			 */
			if (method.equals("Weichzeichner")) {
				float ninth = 1.0f/9.0f;
				float[] filter = { ninth, ninth, ninth,
								   ninth, ninth, ninth,
								   ninth, ninth, ninth, };
				
//				float[] filter = { 0, -1, 0,
//						   		  -1, 5, -1,		//example
//						   		   0, -1, 0, };
				
				int fHeight = 3;
				int fWidth = 3;
				int fHeightHalf = fHeight/2;
				int fWidthHalf = fWidth/2;
				
				int pos = 0;
				for(int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++, pos++) {
						
						float r = 0, g = 0, b = 0;
						
						for(int fRow = -fHeightHalf; fRow <= fHeightHalf; fRow++){
							//current y pos in picture + filter pos
							int fy = y+fRow; 
							//set y offset for pixel array
							int origOffset;
							if (0 <= fy && fy < height)
								origOffset = fy*width;
							else continue; //skip if out of range
							//set offset for filter array
							int fOffset = fWidth * (fRow + fHeightHalf) + fWidthHalf;
						
							for(int fCol = -fWidthHalf; fCol <= fWidthHalf; fCol++) {
								//get filter value at offset position
								float f = filter[fOffset+fCol];
								if(f != 0){
									//current x pos in picture + filter pos
									int fx = x+fCol;
									//skip if out of range
									if (!(0 <= fx && fx < width)) continue;
									//if(mx < 0) mx = 0;
									//else if(mx >= width) mx = width-1;
									//append filter on pixel and sum up
									int argb = origPixels[origOffset+fx];
									r += f * ((argb >> 16) & 0xff);
									g += f * ((argb >> 8) & 0xff);
									b += f * (argb & 0xff);
								}
							}
						}
						//normalize values
						int ir = normalize((int)(r));
						int ig = normalize((int)(g));
						int ib = normalize((int)(b));
						//set new pixel
						pixels[pos] = (0xFF << 24) | (ir << 16) | (ig << 8) | ib;
					}
				}
			}//if Weichzeichner
			
			/*
			 * "Hochpassfilter"
			 */
			if (method.equals("Hochpassfilter")) {

				float ninth = 1.0f/9.0f;
				float[] filter = { ninth, ninth, ninth,
								   ninth, ninth, ninth,
								   ninth, ninth, ninth, };
				
				float[] filter2 = { -ninth, -ninth, -ninth,
								    -ninth, 1.0f-ninth, -ninth,
							  	    -ninth, -ninth, -ninth, };
				
//				float[] filter2 = { 0, 0, 0, 0, 0,
//									0, 0, 0, 0, 0,
//					    			1, 1, 1, 1, 1,
//					    			0, 0, 0, 0, 0,
//					    			0, 0, 0, 0, 0,};
				
				
//				float[] filter = { 0, -1, 0,
//						   		  -1, 5, -1,		//sharp example
//						   		   0, -1, 0, };
				
				int fHeight = 3;
				int fWidth = 3;
				int fHeightHalf = fHeight/2;
				int fWidthHalf = fWidth/2;
				
				double u = 301 / 201;
				double p = (double)(301 / 201);
				System.out.println(u + " vs " + p);
						
				
				int pos = 0;
				for(int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++, pos++) {
						
						float r = 0, g = 0, b = 0;
						
						for(int fRow = -fHeightHalf; fRow <= fHeightHalf; fRow++){
							//current y pos in picture + filter pos
							int fy = y+fRow; 
							//set y offset for pixel array
							int origOffset;
							if (0 <= fy && fy < height)
								origOffset = fy*width;
							else continue; //skip if out of range
							//set offset for filter array
							int fOffset = fWidth * (fRow + fHeightHalf) + fWidthHalf;
						
							for(int fCol = -fWidthHalf; fCol <= fWidthHalf; fCol++) {
								//get filter value at offset position
								float f = 1;//filter[fOffset+fCol];
								float f2 = filter2[fOffset+fCol];
								if(f != 0 && f2 != 0){
									//current x pos in picture + filter pos
									int fx = x+fCol;
									//skip if out of range
									if (!(0 <= fx && fx < width)) continue;
									//if(mx < 0) mx = 0;
									//else if(mx >= width) mx = width-1;
									
									//append filter on pixel and sum up
									int argb = origPixels[origOffset+fx];
									int ar = ((argb >> 16) & 0xff);
									int ag = ((argb >> 8) & 0xff);
									int ab = (argb & 0xff);
									
									r += f2 * ar;
									g += f2 * ag;
									b += f2 * ab;
								}
							}
						}
						//normalize values
						int ir = normalize((int)(r+128));
						int ig = normalize((int)(g+128));
						int ib = normalize((int)(b+128));
						//set new pixel
						pixels[pos] = (0xFF << 24) | (ir << 16) | (ig << 8) | ib;
					}
				}
			}//if Hochpassfilter
			
			/*
			 * "verst‰rkte Kanten"
			 */
			if (method.equals("verst‰rkte Kanten")) {

				float ninth = -1.0f/9.0f;
				float[] filter = { ninth, ninth, ninth,
								   ninth, 17.0f/9.0f, ninth,
								   ninth, ninth, ninth, };
				
				//examples: http://wiki.delphigl.com/index.php/Convolution-Filter
//				float[] filter = { 0, -1, 0,
//						   		  -1, 5, -1,		//sharp example
//						   		   0, -1, 0, };
				
				int fHeight = 3;
				int fWidth = 3;
				int fHeightHalf = fHeight/2;
				int fWidthHalf = fWidth/2;
				
				int pos = 0;
				for(int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++, pos++) {
						
						float r = 0, g = 0, b = 0;
						
						for(int fRow = -fHeightHalf; fRow <= fHeightHalf; fRow++){
							//current y pos in picture + filter pos
							int fy = y+fRow; 
							//set y offset for pixel array
							int origOffset;
							if (0 <= fy && fy < height)
								origOffset = fy*width;
							else continue; //skip if out of range
							//set offset for filter array
							int fOffset = fWidth * (fRow + fHeightHalf) + fWidthHalf;
						
							for(int fCol = -fWidthHalf; fCol <= fWidthHalf; fCol++) {
								//get filter value at offset position
								float f = filter[fOffset+fCol];
								if(f != 0){
									//current x pos in picture + filter pos
									int fx = x+fCol;
									//skip if out of range
									if (!(0 <= fx && fx < width)) continue;
									//if(mx < 0) mx = 0;
									//else if(mx >= width) mx = width-1;
									
									//append filter on pixel and sum up
									int argb = origPixels[origOffset+fx];
									r += f * ((argb >> 16) & 0xff);
									g += f * ((argb >> 8) & 0xff);
									b += f * (argb & 0xff);
								}
							}
						}
						//normalize values
						int ir = normalize((int)(r));
						int ig = normalize((int)(g));
						int ib = normalize((int)(b));
						//set new pixel
						pixels[pos] = (0xFF << 24) | (ir << 16) | (ig << 8) | ib;
					}
				}
			}//if "verst‰rkte Kanten"
		}


	} // CustomWindow inner class
	
	private int normalize(int n){
		 return n < 0 ? 0 : n > 255 ? 255 : n;
	}
} 
