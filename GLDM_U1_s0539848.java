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
		"Japanische Fahne mit weichen Kanten"
	};
	
	private String choice;
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		GLDM_U1_s0539848 imageGeneration = new GLDM_U1_s0539848();
		imageGeneration.run("");
	}
	
	public void run(String arg) {
		
		int width  = 566;  // Breite
		int height = 400;  // Hoehe
		
		// RGB-Bild erzeugen
		ImagePlus imagePlus = NewImage.createRGBImage("GLDM_U1", width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ip = imagePlus.getProcessor();
		
		// Arrays fuer den Zugriff auf die Pixelwerte
		int[] pixels = (int[])ip.getPixels();
		
		dialog();
		
		////////////////////////////////////////////////////////////////
		// Hier bitte Ihre Aenderungen / Erweiterungen
		
		if ( choice.equals("Schwarzes Bild") ) {
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = 0;
					int g = 0;
					int b = 0;
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		
		
		} else if( choice.equals("Gelbes Bild")) {
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = 255;
					int g = 255;
					int b = 0;
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Italienische Fahne")) { // grün, weiß, rot
			double drittel = width/3;
			int r=0, g=0, b=0;
			
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				// Schleife ueber die x-Werte
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
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Schwarz/Weiss Verlauf")) {
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = (x*255)/(width-1);
					int g = (x*255)/(width-1);
					int b = (x*255)/(width-1);
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		else if( choice.equals("Horiz. Schwarz/Rot vert. Schwarz/Blau Verlauf")) {
			// Schleife ueber die y-Werte
			for (int y=0; y<height; y++) {
				int b = (y*255)/(height-1);
				
				// Schleife ueber die x-Werte
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					
					int r = (x*255)/(width-1);
					int g = 0;
					
					
					// Werte zurueckschreiben
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		else if( choice.equals("Bahamische Fahne")) {
			double drittel = height/3;
			int r=0, g=0, b=0;
			
			// 3 streifen
			for (int y=0; y<height; y++) {	
				if (y <= drittel) {
					r = 0;
					g = 0;
					b = 255;
				} else if (y <= drittel*2) {
					r = 255;
					g = 255;
					b = 0;
				} else {
					r = 0;
					g = 0;
					b = 255;
				}
				
				for (int x=0; x<width; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
			
			int y=0;
			r = 0;
			g = 0;
			b = 0;
			
			// obere dreieck
			for (; y<height/2; y++) {
				for (int x=0; x<y; x++) {			
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
			
			// untere dreieck
			int k = y;
			for (; y<height; y++) {
				for (int x=0; x<k; x++) {
					int pos = y*width + x; // Arrayposition bestimmen
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
				k--;
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
			double radiusAußen = radiusInnen*1.30;
			double radiusAußenBreite = radiusAußen - radiusInnen; // ~5306
			
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
						
						// r = z-punkte im außenradius / 
						r = (int) ( ((radiusP - radiusInnen) / radiusAußenBreite) *255);
						System.out.println( ((radiusP - radiusInnen) / radiusAußenBreite) *255);
						g = 0;
						b = 0;
						
					
					// weiß
					} else {
						r=255;
						g = 255;
						b = 255;
					}
					
					pixels[pos] = 0xFF000000 | (r << 16) | (g << 8) |  b;
				}
			}
		}
		
		
		////////////////////////////////////////////////////////////////////
		
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

