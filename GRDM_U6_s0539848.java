import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class GRDM_U6_s0539848 implements PlugInFilter {
	ImagePlus imp; // ImagePlus object
	private int width = 0;
	private int height = 0;
		
	public static void main(String args[]) {
		IJ.open("component.jpg");

		GRDM_U6_s0539848 pw = new GRDM_U6_s0539848();
		pw.imp = IJ.getImage();
		pw.run(pw.imp.getProcessor());
	}
	
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout(); 
			return DONE;
		}
		return DOES_RGB+NO_CHANGES;
	}

	public void run(ImageProcessor ip) {
		String[] dropdownmenue = {"Kopie", "Pixelwiederholung", "Bilinear"};

		GenericDialog gd = new GenericDialog("scale");
		gd.addChoice("Methode",dropdownmenue,dropdownmenue[0]);
		gd.addNumericField("Hoehe:",500,0);
		gd.addNumericField("Breite:",400,0);
		gd.showDialog();
		String choice = gd.getNextChoice();
		
		// rahmen bzw. anzeigefläche
		int height_n = (int)gd.getNextNumber();
		int width_n =  (int)gd.getNextNumber();
		
		// das darin angezeigte bild
		width  = ip.getWidth();
		height = ip.getHeight();
		
		ImagePlus neu = NewImage.createRGBImage("Skaliertes Bild", width_n, height_n, 1, NewImage.FILL_BLACK);
		ImageProcessor ip_n = neu.getProcessor();

		int[] pix = (int[])ip.getPixels();
		int[] pix_n = (int[])ip_n.getPixels();

		
		if (choice.equals("Kopie")) {		
			for (int y_n=0; y_n<height_n; y_n++) {
				for (int x_n=0; x_n<width_n; x_n++) {
					int y = y_n;
					int x = x_n;
					
					if (y < height && x < width) {
						int pos_n = y_n*width_n + x_n;
						int pos  =  y  *width   + x;			
						pix_n[pos_n] = pix[pos];
					}
				}
			} 
		
		} else if (choice.equals("Pixelwiederholung")) {
			for (int y_n=0; y_n<height_n; y_n++) {
				for (int x_n=0; x_n<width_n; x_n++) {
					float h = (float)(width)/width_n;
					float v = (float)(height)/height_n;
					
					int pos =  Math.round(v*y_n)*width + Math.round(h*x_n);
					int pos_n = y_n*width_n + x_n;
					
					if (pos >= width*height)
						pos = width*height-1;
					
					pix_n[pos_n] = pix[pos];
				}	
			}
		
		} else if (choice.equals("Bilinear")) {
			for (int y_n=0, y=0; y_n<height_n; y_n++) {
				for (int x_n=0, x=0; x_n<width_n; x_n++) {
					x = (int) ((float)width/width_n * x_n);
					y = (int) ((float)height/height_n*y_n);
					
					// farbwerte des kernels
					int a = pix[checkPixel(y, x)];
					int ar = (a & 0xff0000) >> 16;
					int ag = (a & 0x00ff00) >> 8;
					int ab = (a & 0x0000ff);
				
					int b = pix[checkPixel(y, x+1)];
					int br = (b & 0xff0000) >> 16;
					int bg = (b & 0x00ff00) >> 8;
					int bb = (b & 0x0000ff);
				
					int c = pix[checkPixel(y+1, x)];
					int cr = (c & 0xff0000) >> 16;
					int cg = (c & 0x00ff00) >> 8;
					int cb = (c & 0x0000ff);
				
					int d = pix[checkPixel(y+1, x+1)];
					int dr = (d & 0xff0000) >> 16;
					int dg = (d & 0x00ff00) >> 8;
					int db = (d & 0x0000ff);
				
					// verhältnis
					float deltaH = (float)width_n/width;
					float deltaV = (float)height_n/height;
					
					// von den abständen nur den rest (mod.)
					float h = ( (float)x_n/deltaH ) %1;
					float v = ( (float)y_n/deltaV ) %1;
					
					// anteile an den rgb-werten (0 für 'fehlende' randpixel)
					int pr = (int) (ar*(1-h)*(1-v)+br*h*(1-v)+cr*(1-h)*v+dr*h*v);
					int pg = (int) (ag*(1-h)*(1-v)+bg*h*(1-v)+cg*(1-h)*v+dg*h*v);
					int pb = (int) (ab*(1-h)*(1-v)+bb*h*(1-v)+cb*(1-h)*v+db*h*v);

					int pos_n = y_n*width_n + x_n;
					pix_n[pos_n] = 0xFF000000 + ((pr & 0xff) << 16) + ((pg & 0xff) << 8) + (pb & 0xff);			
				}	
			}
		}
		
		
		// neues Bild anzeigen
		neu.show();
		neu.updateAndDraw();
	}
	
	private int checkPixel(int y, int x) {
		return (y>= height || x >= width) ?0 :y*width+x;
	}

	void showAbout() {
		IJ.showMessage("");
	}
}