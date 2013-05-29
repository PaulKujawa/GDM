import ij.*;
import ij.io.*;
import ij.process.*;
import ij.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;


public class GRDM_U4_s0539848 implements PlugInFilter {

	protected ImagePlus imp;
	final static String[] choices = {"Wischen", "Weiche Blende", "Overlay", "Schieb-Blende", "Chroma-Keying", "Eigenes", "Jalousie"};

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB+STACK_REQUIRED;
	}
	
	public static void main(String args[]) {
		ImageJ ij = new ImageJ(); // neue ImageJ Instanz starten und anzeigen 
		ij.exitWhenQuitting(true);
		
		IJ.open("StackB.zip");
		
		GRDM_U4_s0539848 sd = new GRDM_U4_s0539848();
		sd.imp = IJ.getImage();
		ImageProcessor B_ip = sd.imp.getProcessor();
		sd.run(B_ip);
	}

	public void run(ImageProcessor B_ip) {
		// Film B wird uebergeben
		ImageStack stack_B = imp.getStack();
		
		int length = stack_B.getSize();
		int width  = B_ip.getWidth();
		int height = B_ip.getHeight();
		
		// ermoeglicht das Laden eines Bildes / Films
		Opener o = new Opener();
		OpenDialog od_A = new OpenDialog("AuswÃ¤hlen des 2. Filmes ...",  "");
				
		// Film A wird dazugeladen
		String dateiA = od_A.getFileName();
		if (dateiA == null) return; // Abbruch
		String pfadA = od_A.getDirectory();
		ImagePlus A = o.openImage(pfadA,dateiA);
		if (A == null) return; // Abbruch

		ImageProcessor A_ip = A.getProcessor();
		ImageStack stack_A  = A.getStack();

		if (A_ip.getWidth() != width || A_ip.getHeight() != height) {
			IJ.showMessage("Fehler", "Bildgrößen passen nicht zusammen");
			return;
		}
		
		// Neuen Film (Stack) "Erg" mit der kleineren Laenge von beiden erzeugen
		length = Math.min(length,stack_A.getSize());

		ImagePlus Erg = NewImage.createRGBImage("Ergebnis", width, height, length, NewImage.FILL_BLACK);
		ImageStack stack_Erg  = Erg.getStack();

		// Dialog fuer Auswahl des Ueberlagerungsmodus
		GenericDialog gd = new GenericDialog("Überlagerung");
		gd.addChoice("Methode",choices,"");
		gd.showDialog();

		int methode = 0;		
		String s = gd.getNextChoice();
		if (s.equals("Wischen")) methode = 1;
		else if (s.equals("Weiche Blende")) methode = 2;
		else if (s.equals("Overlay")) methode = 3;
		else if (s.equals("Schieb-Blende")) methode = 4;
		else if (s.equals("Chroma-Keying")) methode = 5;
		else if (s.equals("Eigenes")) methode = 6;
		else if (s.equals("Jalousie")) methode = 7;

		// Arrays fuer die einzelnen Bilder
		int[] pixels_B;
		int[] pixels_A;
		int[] pixels_Erg;

		// Schleife ueber alle Bilder
		for (int z=1; z<=length; z++) {
			pixels_B   = (int[]) stack_B.getPixels(z);
			pixels_A   = (int[]) stack_A.getPixels(z);
			pixels_Erg = (int[]) stack_Erg.getPixels(z);

			int pos=0, streifen=0;
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++, pos++) {
					int cA = pixels_A[pos];
					int rA = (cA & 0xff0000) >> 16;
					int gA = (cA & 0x00ff00) >> 8;
					int bA = (cA & 0x0000ff);

					int cB = pixels_B[pos];
					int rB = (cB & 0xff0000) >> 16;
					int gB = (cB & 0x00ff00) >> 8;
					int bB = (cB & 0x0000ff);

	
					if (methode == 1) {
						System.out.println(length);
						if (y+1 > (z-1) * (double)height/(length-1) )
							pixels_Erg[pos] = pixels_B[pos];
						else
							pixels_Erg[pos] = pixels_A[pos];
					}

					else if (methode == 2) {
						double a = (z-1d) / (length-1d);
						// (A*E1 + (Amax-A)*E2) / Amax
						int r = (int) (a*rA + (1-a)*rB);
						int g = (int) (a*gA + (1-a)*gB);
						int b = (int) (a*bA + (1-a)*bB);
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}
					
					else if (methode == 3) {
						// A über B
						int r = overlay(rA, rB);
						int g = overlay(gA, gB);
						int b = overlay(bA, bB);
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}
					
					else if (methode == 4) {
						int diff = (int) ((z-1)*(double)width/(length-1));
						int pos2 = pos-diff;
						
						if (x+1>diff)
							pixels_Erg[pos] = pixels_B[pos2];
						else
							pixels_Erg[pos] = pixels_A[width+pos2];
					}
					
					else if (methode == 5) {
						if ( (rA>170 && rA<260) && (gA>100 && gA<200) && (bA>30 && bA<100)) // wenn orange
							pixels_Erg[pos] = pixels_B[pos]; // galaxy
						else
							pixels_Erg[pos] = pixels_A[pos]; // Kreisel
					}
					
					else if (methode == 6) {
						if ( (rA>170 && rA<260) && (gA>100 && gA<200) && (bA>30 && bA<100)) // wenn orange
							pixels_Erg[pos] = pixels_B[pos]; // galaxy
						else
							pixels_Erg[pos] = pixels_A[pos]; // Kreisel
						
						if (x<=y)
							pixels_Erg[pos] = pixels_A[pos]-128-z;
					}
					
					else if (methode == 7) {
						int r=rA, g=gA, b=bA;
						
						if (x % 48 == 0) { // ca. 5 mal
							r=rB;
							g=gB;
							b=bB;
							streifen=z/2; // breite des streifens definieren
						}
						else if (streifen>0) { // breite des streifens ablaufen
							r=rB;
							g=gB;
							b=bB;
							streifen--;
						}
						
						pixels_Erg[pos] = 0xFF000000 + ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);
					}
				}
			}
		}

		// neues Bild anzeigen
		Erg.show();
		Erg.updateAndDraw();
	}
	
	
	private int overlay(int e1, int e2) {
		if (e2 <= 128)
			return e1*e2 /128;
		
		return 255 -( (255-e1)*(255-e2) /128);
	}
}