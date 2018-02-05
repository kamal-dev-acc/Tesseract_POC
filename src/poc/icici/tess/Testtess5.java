package poc.icici.tess;

import static net.sourceforge.tess4j.ITessAPI.TRUE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.sun.jna.Pointer;

import net.sourceforge.tess4j.ITessAPI.ETEXT_DESC;
import net.sourceforge.tess4j.ITessAPI.TessBaseAPI;
import net.sourceforge.tess4j.ITessAPI.TessPageIterator;
import net.sourceforge.tess4j.ITessAPI.TessPageSegMode;
import net.sourceforge.tess4j.ITessAPI.TessResultIterator;
import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.util.ImageIOHelper;
import net.sourceforge.tess4j.util.PdfUtilities;

public class Testtess5 {
	public static void main(String[] args) {
		
		
		String pdfFilePath ="C://Users//guptakam//Desktop//HP//HP CitiBank Corporate Card//PAC_APP_A0000332919_20170830011131_JvBu1GDGaMPU8vsfQfaUzji9RWSFY8x3WWRKPB1JQw=.pdf";

		File pdf = new File(pdfFilePath);

		File tiff = new File("C://Users//guptakam//Desktop//PROJECTS//ICICI_POC//tmp.tif");

		String datapath = "C://Users//guptakam//Desktop//DEV_TOOLS//ICICI_POC//Tess4J-3.4.3-src//Tess4J";
		String language = "eng";

		String wordtoFind = "Citibank";

		//findWord(pdf, tiff, wordtoFind, datapath, language);
		
		splitPdfFileAndFindWord(pdfFilePath,pdf, tiff, wordtoFind, datapath, language);
	}

	/**
	 * 
	 */
	public static void findWord(File pdf, File tiff, String wordToFind, String datapath, String language) {

		
		//System.out.println("File name  "+ pdf.getName());
		
		TessBaseAPI handle = TessAPI1.TessBaseAPICreate();

		try {
			tiff = PdfUtilities.convertPdf2Tiff(pdf);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		BufferedImage image = null;
		try {
			image = ImageIO.read(new FileInputStream(tiff));

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		ByteBuffer buf = ImageIOHelper.convertImageData(image);
		int bpp = image.getColorModel().getPixelSize();
		int bytespp = bpp / 8;
		int bytespl = (int) Math.ceil(image.getWidth() * bpp / 8.0);

		TessAPI1.TessBaseAPIInit3(handle, datapath, language);
		TessAPI1.TessBaseAPISetPageSegMode(handle, TessPageSegMode.PSM_AUTO);
		TessAPI1.TessBaseAPISetImage(handle, buf, image.getWidth(), image.getHeight(), bytespp, bytespl);

		ETEXT_DESC monitor = new ETEXT_DESC();
		TessAPI1.TessBaseAPIRecognize(handle, monitor);

		TessResultIterator ri = TessAPI1.TessBaseAPIGetIterator(handle);

		TessPageIterator pi = TessAPI1.TessResultIteratorGetPageIterator(ri);

		TessAPI1.TessPageIteratorBegin(pi);

		// System.out.println("Bounding boxes:\nchar(s) left top right bottom confidence
		// font-attributes ");

		int level = TessAPI1.TessPageIteratorLevel.RIL_WORD;

		int height = image.getHeight();

		do {
			Pointer ptr = TessAPI1.TessResultIteratorGetUTF8Text(ri, level);
			String word = ptr.getString(0);

			TessAPI1.TessDeleteText(ptr);

			float confidence = TessAPI1.TessResultIteratorConfidence(ri, level);
			IntBuffer leftB = IntBuffer.allocate(1);
			IntBuffer topB = IntBuffer.allocate(1);
			IntBuffer rightB = IntBuffer.allocate(1);
			IntBuffer bottomB = IntBuffer.allocate(1);

			TessAPI1.TessPageIteratorBoundingBox(pi, level, leftB, topB, rightB, bottomB);
			
			
			int left = leftB.get();
			int top = topB.get();
			int right = rightB.get();
			int bottom = bottomB.get();

			//System.out.println(word);

			if (word.equalsIgnoreCase(wordToFind)) {
				System.out.print(" Location Details for Word :  ");
				System.out.print(String.format("%s %d %d %d %d %f", word, left, top, right, bottom, confidence));
			}

			IntBuffer boldB = IntBuffer.allocate(1);
			IntBuffer italicB = IntBuffer.allocate(1);
			IntBuffer underlinedB = IntBuffer.allocate(1);
			IntBuffer monospaceB = IntBuffer.allocate(1);
			IntBuffer serifB = IntBuffer.allocate(1);
			IntBuffer smallcapsB = IntBuffer.allocate(1);
			IntBuffer pointSizeB = IntBuffer.allocate(1);
			IntBuffer fontIdB = IntBuffer.allocate(1);

			String fontName = TessAPI1.TessResultIteratorWordFontAttributes(ri, boldB, italicB, underlinedB,
					monospaceB, serifB, smallcapsB, pointSizeB, fontIdB);

			boolean bold = boldB.get() == TRUE;
			boolean italic = italicB.get() == TRUE;
			boolean underlined = underlinedB.get() == TRUE;
			boolean monospace = monospaceB.get() == TRUE;
			boolean serif = serifB.get() == TRUE;
			boolean smallcaps = smallcapsB.get() == TRUE;
			int pointSize = pointSizeB.get();
			int fontId = fontIdB.get();

			if (word.equalsIgnoreCase(wordToFind)) {
				System.out.print(" Font Details ");
				System.out.println(String.format(
						" font: %s, size: %d, font id: %d, bold: %b,"
								+ " italic: %b, underlined: %b, monospace: %b, serif: %b, smallcap: %b",
								fontName, pointSize, fontId, bold, italic, underlined, monospace, serif, smallcaps));
			}

		} while (TessAPI1.TessPageIteratorNext(pi, level) == TRUE);


	}
	
	
	
	
	public static void splitPdfFileAndFindWord(String pdfFilePath,File pdf, File tiff, String wordToFind, String datapath, String language) {
		try {
            String inFile =pdfFilePath.toLowerCase();
            System.out.println ("Reading " + inFile);
            PdfReader reader = new PdfReader(inFile);
            int n = reader.getNumberOfPages();
            
            System.out.println ("Number of pages : " + n);
            
           LinkedList<String> files = new LinkedList<String>();
            
            int i = 0;            
            while ( i < n ) {
            	
              
            	
            	String outFile = inFile.substring(0, inFile.indexOf(".pdf")) + "-" + String.format("%03d", i + 1) + ".pdf"; 
                
                //System.out.println ("Writing " + outFile);
               
                Document document = new Document(reader.getPageSizeWithRotation(1));
                
                PdfCopy writer = new PdfCopy(document, new FileOutputStream(outFile));
                
                document.open();
                PdfImportedPage page = writer.getImportedPage(reader, ++i);
                writer.addPage(page);
                document.close();
                writer.close();
                files.add(outFile);
                
            }
            
            
            //find word
            int j=1;
            for(String file : files) {
            	File file1 = new File(file);
            	System.out.println("Page " + j++);
                findWord(file1, tiff, wordToFind, datapath, language);
                
                if (file1.exists()) {
                    file1.delete();
                }
            }
            
            
            
            
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        /* example : 
            java SplitPDFFile d:\temp\x\tx.pdf

            Reading d:\temp\x\tx.pdf
            Number of pages : 3
            Writing d:\temp\x\tx-001.pdf
            Writing d:\temp\x\tx-002.pdf
            Writing d:\temp\x\tx-003.pdf
         */

	}
	

}