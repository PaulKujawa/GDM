import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

//erste Uebung (elementare Bilderzeugung)

public class GLDM_U1_s0539848 implements PlugIn {
	
	final static String[] choices = {
		"Schwarzes Bild",
		"Gelbes Bild",
		"Schwarz/Weiss Verlauf",
		"Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf",
		"Italienische Fahne",
		"Bahamische Fahne",
		"Japanische Fahne",
		"Japanische Fahne mit weichen Kanten",
		"Streifenmuster"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1_s0539848 imageGeneration = new GLDM_U1_s0539848();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		int width  = 566, height = 400;
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		dialog();
		
		
		
		
		
		
		if ( choice.equals("Schwarzes Bild") ) {
			int r=0, g=0, b=0;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		
		
		} else if( choice.equals("Gelbes Bild")) {
			int r=255, g=255, b=0;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Italienische Fahne")) {
			double drittel = width/3;
			int r=0, g=0, b=0;
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					if (x <= drittel) {
						r = 0;
						g = 255;
						b = 0;
					} else if (x <= drittel*2) {
						r = 255;
						g = 255;
						b = 255;
					} else {
						r = 255;
						g = 0;
						b = 0;
					}
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Schwarz/Weiss Verlauf")) {
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = (x*255)/(width-1);
					int g = r;
					int b = r;
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf")) {
			for (int y=0; y<height; y++) {
				int b = (y*255)/(height-1);
				int g = 0;
				
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen	
					int r = (x*255)/(width-1);
		
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		else if( choice.equals("Bahamische Fahne")) {
			int r=0, g=0, b=0;
			int dreieckende = 0;
			
			for (int y=0; y<height; y++) {
				if (y<height/2)
					dreieckende++;
				else
					dreieckende--;
				
				for (int x=0; x<width; x++) {
					// schwarze dreieck
					if (x<=dreieckende) {
						r=0; g=0; b=0;
					}
					
					// farbstriche
					else if (y <= height/3) {
						r = 0;
						g = 0;
						b = 255;
					} else if (y <= height/3*2) {
						r = 255;
						g = 255;
						b = 0;
					} else {
						r = 0;
						g = 0;
						b = 255;
					}
				
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		else if( choice.equals("Japanische Fahne")) {
			int r = 255, g = 0, b = 0;
			int mx = width/2, my = height/2;
			double radius = height/3;
			
			for (int y=0; y<height; y++) {	
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
				
					// im kreis?
					int deltaY = Math.abs(y - my);
					int deltaX = Math.abs(x - mx);
					int radiusP = deltaY*deltaY + deltaX*deltaX;
					
					
					if (radiusP < radius*radius) {
						g=0;
						b=0;
					} else {
						g = 255;
						b = 255;
					}
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		else if( choice.equals("Japanische Fahne mit weichen Kanten")) {
			int r, g, b;
			int mx = width/2, my = height/2;
			double radiusInnen = height/3; radiusInnen *= radiusInnen;
			double radiusAußen = radiusInnen*2.30;
			
			for (int y=0; y<height; y++) {	
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
				
					// abstand zum Kreismittelpunkt berechnen
					int deltaY = Math.abs(y - my);
					int deltaX = Math.abs(x - mx);
					int radiusP = deltaY*deltaY + deltaX*deltaX;
					
					// rot
					if (radiusP < radiusInnen) {
						r=255;
						g=0;
						b=0;
					
					// verlauf
					} else if (radiusP < radiusAußen) {
						
						/*  m = (y2-y1) / (x2-x1)
						 *  f = mx+n
						 *              
						 *   | .(rI|255)        
						 *   |                .(rA|0)
						 *   ------------------------
						 *  rI      rP        rA
						 */
						
						
						g = (int) (255/(radiusAußen-radiusInnen) * (radiusP-radiusInnen)); 
						b=g;
						r=255;
									
					// weiß
					} else {
						r = 255;
						g = 255;
						b = 255;
					}
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		else if( choice.equals("Streifenmuster")) {
			int r = 0, g = 0, b = 0;
			
			for (int y=0; y<height; y++) {	
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					if (x%2 == 1) {
						r=0;
						g=0;
						b=0;
					} else {
						r=255;
						g=255;
						b=255;
					}
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		// neues Bild anzeigen
		imagePlus.show();
		imagePlus.updateAndDraw();
	}
	
	
	private void dialog() {
		// Dialog fuer Auswahl der Bilderzeugung
		GenericDialog gd = new GenericDialog("Bildart");
		
		gd.addChoice("Bildtyp", choices, choices[0]);
		
		
		gd.showDialog();	// generiere Eingabefenster
		
		choice = gd.getNextChoice(); // Auswahl uebernehmen
		
		if (gd.wasCanceled())
			System.exit(0);
	}
}