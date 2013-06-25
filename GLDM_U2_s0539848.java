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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GLDM_U2_s0539848 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
    	IJ.open("orchid.jpg");
		GLDM_U2_s0539848 pw = new GLDM_U2_s0539848();
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
    }
      
    class CustomWindow extends ImageWindow implements ChangeListener {         
        private JSlider jSliderBrightness, jSliderKontrast, jSliderSaettigung, jSliderHue;
		private double brightness=0, kontrast=1, saettigung = 1, hue=0;

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 200, 100);
            jSliderKontrast = makeTitledSilder("Kontrast", 0, 20, 10);
            jSliderSaettigung = makeTitledSilder("Saettigung", 0, 5, 2);
            jSliderHue = makeTitledSilder("Hue", 0, 360, 0);
            
            Panel panel = new Panel();
            panel.setLayout(new GridLayout(4, 1));
            panel.add(jSliderBrightness);
            panel.add(jSliderKontrast);
            panel.add(jSliderSaettigung);
            panel.add(jSliderHue);

            add(panel);
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

        
        /**
         *  Frontend beim Verschieben
         */
		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-100;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			else if (slider == jSliderKontrast) {
				kontrast = slider.getValue();
				kontrast /= 10;
				String str = "Kontrast " + kontrast; 
				setSliderTitle(jSliderKontrast, str); 
			}
			
			else if (slider == jSliderSaettigung) {
				saettigung = slider.getValue();
				String str = "Sättigung " + saettigung; 
				setSliderTitle(jSliderSaettigung, str); 
			}
			
			else if (slider == jSliderHue) {
				hue = slider.getValue();
				String str = "Hue " + hue + "°"; 
				setSliderTitle(jSliderHue, str); 
			}
			
			changePixelValues(imp.getProcessor());
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					// Farbtransformation hin
					int Y  = (int) (0.299*r + 0.587*g + 0.114*b);
					int Cb = (int) (-0.168736*r - 0.331264*g + 0.5*b);
					int Cr = (int) (0.5*r - 0.418688*g - 0.081312*b);
					
					
					
					// Helligkeit
					Y += brightness;
					
					// Kontrast
					Y = (int) (kontrast*(Y-128)+128);
					
					// Sättigung
					Cb *= saettigung;
					Cr *= saettigung;
					
					
					
					// Hue
					/*			cbOld
					 * 			crOld
					 *  cos -sin	  Cb = cos*cbOld - sin*crOld
					 *  sin cos		  Cr = sin*cbOld + cos*crOld
					 */
					int CbOld = Cb;
					int CrOld = Cr;
					double phi = Math.toRadians(hue);
					
					Cb = (int) (Math.cos(phi)*CbOld - Math.sin(phi)*CrOld);
					Cr = (int) (Math.sin(phi)*CbOld + Math.cos(phi)*CrOld);


					
					
					
					// Farbtransformation zurück
					int rn 	= (int) (Y + 1.402 * Cr);
					int gn = (int) (Y - 0.3441*Cb- 0.7141*Cr);
					int bn = (int) (Y + 1.772*Cb);
					
					// Werte begrenzen
					if (rn > 255)
						rn = 255;
					else if (rn < 0)
						rn= 0;
					if (gn > 255)
						gn=255;
					else if(gn < 0)
						gn=0;
					if(bn > 255)
						bn = 255;
					else if (bn < 0)
						bn=0;
					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
    }
} 
