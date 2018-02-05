package poc.icici.tess;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import net.sourceforge.tess4j.*;

public class TestTess {
	
	
	//Second Commit

	public static void main(String[] args) throws IOException, InterruptedException {

		TestTess ttObj = new TestTess();
		HashMap<String,String> mapMainFile = new HashMap<String,String>();

		String mainPropFile = "C://Users//guptakam//Desktop//PROJECTS//ICICI_POC//MainFile.properties";

		String extractable_properties = "extractable_properties";
		String nonextractable_properties = "nonextractable_properties";
		String source_directory = "source_directory";
		String target_directory="target_directory";
		String log4j_file = "log4j_file";
		String ocrTextFilePath = "ocrTextFilePath";
		String ocrTextFileName = "ocrTextFileName";
		String corpusDataPath = "corpusDataPath";
		String businessFilePath = "businessFilePath";

		
		
		//Read data from the mainPropFile file
		mapMainFile =  ttObj.readPropertyFromMainFile(mainPropFile);				

		org.apache.log4j.PropertyConfigurator.configure(mapMainFile.get(log4j_file)); // sets properties file for log4j

		
		String extraFilePath = mapMainFile.get(extractable_properties);
		String nonExtraFilePath = mapMainFile.get(nonextractable_properties);
		String textFilePath = mapMainFile.get(ocrTextFilePath);  
		String textFileName = mapMainFile.get(ocrTextFileName);
		String corpusDataPathInp =  mapMainFile.get(corpusDataPath);
		String businessFile =  mapMainFile.get(businessFilePath);

		String inputFileFullPath = mapMainFile.get(source_directory); 
		String outputFileFullPath = mapMainFile.get(target_directory);


		ttObj.execute(ttObj, extraFilePath, textFilePath, textFileName, corpusDataPathInp, inputFileFullPath,outputFileFullPath,businessFile);

	}

	/**
	 * @param ttObj
	 * @param extraFilePath
	 * @param textFilePath
	 * @param textFileName
	 * @param corpusDataPathInp
	 * @param inputFileFullPath
	 * @param outputFileFullPath
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void execute(TestTess ttObj, String extraFilePath, String textFilePath, String textFileName,
			String corpusDataPathInp, String inputFileFullPath, String outputFileFullPath,String businessFile)
					throws InterruptedException, IOException {
		HashMap<String, String> mapextractableFile;
		HashMap<String, String> mapBusinessFile;
		
		while(true) {

			File folder = new File(inputFileFullPath);
			File[] listOfFiles = folder.listFiles();
			LinkedList<LinkedList<Details>> detList = null;

			if(listOfFiles.length ==0) {
				Thread.sleep(4000);
				System.out.println("Sleeping for 4000ms.");
			}else {
				for (File file : listOfFiles) {
					if (file.isFile()) {

						//check tif or pdf
						String fileType = file.getName().substring(file.getName().length() - 3);
						File tiffFile = generateTifFile(file.getAbsolutePath(), fileType);

						//Read data from the extractable_properties file
						mapextractableFile =  ttObj.readPropertyFromMapExtractableFile(extraFilePath);	
						
						
						//Read data from Business File
						mapBusinessFile = ttObj.readPropertyFromBusinessFile(businessFile);
						
						
						//Execute the OCR and find the data
						//listOfMappedData = ttObj.findDetailsForExtractableData(ttObj, mapextractableFile, textFilePath, textFileName, corpusDataPathInp,file.getAbsolutePath(), fileType, tiffFile);
						detList = ttObj.findDetailsForExtractableData(ttObj, mapextractableFile, textFilePath, textFileName, corpusDataPathInp,file.getAbsolutePath(), fileType, tiffFile,mapBusinessFile);
						
						System.out.println(detList);
						
						//Read data from the non-extractable_properties file
							//mapnonextractableFile =  ttObj.readPropertyFromMapNonExtractableFile(nonExtraFilePath);	
						//Execute the OCR and find the data		
							//mapOfData = ttObj.findDetailsForNonExtractableData(ttObj, mapnonextractableFile, textFilePath, textFileName, corpusDataPathInp,file.getAbsolutePath(), fileType, tiffFile);

						//Move File to target directory
						ttObj.moveFileToTarget(inputFileFullPath+file.getName(), outputFileFullPath+file.getName());

					}
				}
				
			}
			
		}
	}

	/**
	 * @param inputFileFullPath
	 * @param outputFileFullPath
	 * @throws IOException
	 */
	public void moveFileToTarget(String inputFileFullPath, String outputFileFullPath)  {
		
		Path temp;
		try {
			temp = Files.move(Paths.get(inputFileFullPath), Paths.get(outputFileFullPath));
			
			if(temp != null)
			{
				System.out.println("File renamed and moved successfully");
			}
			else
			{
				System.out.println("Failed to move the file");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @param inputFileFullPath
	 * @param fileType
	 * @return
	 */
	public static File generateTifFile(String inputFileFullPath, String fileType) {
		File tiffFile = null;
		if(fileType.equalsIgnoreCase("tif")) {
			tiffFile = new File(inputFileFullPath);
		}
		return tiffFile;
	}

	/**
	 * @param ttObj
	 * @param mapnonextractableFile
	 * @param textFilePath
	 * @param textFileName
	 * @param corpusDataPathInp
	 * @param inputFileFullPath
	 * @param fileType
	 * @param tiffFile
	 */
	public LinkedList<LinkedList<Details>> findDetailsForNonExtractableData(TestTess ttObj, HashMap<String, String> mapnonextractableFile,
			String textFilePath, String textFileName, String corpusDataPathInp, String inputFileFullPath,
			String fileType, File tiffFile,HashMap<String, String> mapBusinessFile ) {

		LinkedList<LinkedList<Details>> detailsListOfWords = null;
		detailsListOfWords  = ttObj.splitPdfFileAndFindWord(inputFileFullPath, tiffFile,corpusDataPathInp,textFileName,textFilePath,fileType,mapnonextractableFile,mapBusinessFile);
		return detailsListOfWords;
	}


	/**
	 * @param ttObj
	 * @param mapextractableFile
	 * @param textFilePath
	 * @param textFileName
	 * @param corpusDataPathInp
	 * @param inputFileFullPath
	 * @param fileType
	 * @param tiffFile
	 */
	public LinkedList<LinkedList<Details>> findDetailsForExtractableData(TestTess ttObj, HashMap<String, String> mapextractableFile,
			String textFilePath, String textFileName, String corpusDataPathInp, String inputFileFullPath,
			String fileType, File tiffFile,HashMap<String, String> mapBusinessFile) {
		
		LinkedList<LinkedList<Details>> detailsListOfWords = null;
		detailsListOfWords = ttObj.splitPdfFileAndFindWord(inputFileFullPath, tiffFile,corpusDataPathInp,textFileName,textFilePath,fileType,mapextractableFile,mapBusinessFile);
		return detailsListOfWords;
	}


	public HashMap<String, String> readPropertyFromMapNonExtractableFile(String nonExtraFilePath) {
		String value = "NULL";
		HashMap<String,String> map = new HashMap<String,String>(); 

		try (InputStream in = new FileInputStream(nonExtraFilePath)) {
			Properties prop = new Properties();
			prop.load(in);

			for (String property : prop.stringPropertyNames()) {
				value = prop.getProperty(property,"NULL");
				map.put(property, value);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	public HashMap<String, String> readPropertyFromMapExtractableFile(String extraFilePath) {

		String value = "NULL";
		HashMap<String,String> map = new HashMap<String,String>(); 

		try (InputStream in = new FileInputStream(extraFilePath)) {
			Properties prop = new Properties();
			prop.load(in);

			for (String property : prop.stringPropertyNames()) {
				value = prop.getProperty(property,"NULL");
				map.put(property, value);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public LinkedList<Details>  findWord(String wordToFind,String textFileName,String textFilePath,String wordToFindKey,String result,
									     String page,HashMap<String,String> mapBusinessFile) throws IOException {

		//Find the occurrence of word in the whole document.
		LinkedList<Details> detailsList = null;
			System.out.println();
			
			//Finding the Page,Paragraph and line for the word.
			try(  PrintWriter out = new PrintWriter(textFilePath+"//"+textFileName)  ){
				out.println( result );
				out.close();

				detailsList = findpageDetails(textFilePath+"//"+textFileName,wordToFind,wordToFindKey,page,mapBusinessFile);

			}
		return detailsList;

	}

	/**
	 * @param fileToProcess
	 * @param datapath
	 * @param wordToFind
	 * @param textFileName
	 * @param textFilePath
	 * @throws FileNotFoundException
	 */
	public String generateOCRTextFile(File fileToProcess, String datapath) throws FileNotFoundException {
		String result = "";
		Tesseract tessInst = new Tesseract();
		tessInst.setDatapath(datapath);

		try {
			result= tessInst.doOCR(fileToProcess);
			//System.out.println(result);

		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}

		return result;
	}


	public LinkedList<Details> findpageDetails(String textFilePath,String wordToFind,String wordToFindKey,String page,HashMap<String,String> mapBusinessFile) throws IOException {

		File file = new File(textFilePath);
		FileInputStream fileStream = new FileInputStream(file);
		InputStreamReader input = new InputStreamReader(fileStream);
		BufferedReader reader = new BufferedReader(input);
		LinkedList<String> list = new LinkedList<String>();

		String line;
		Details details=null;
		LinkedList<Details> detailsList = new LinkedList<Details>();

		// Initializing counters
		int countWord = 0;
		int sentenceCount = 1;
		int characterCount = 0;
		int paragraphCount = 1;
		int whitespaceCount = 0;
		String prevWord = "";
		String valOfPrevWord ="";

		// Reading line by line from the 
		// file until a null is returned
		while((line = reader.readLine()) != null)
		{

			if(line.equals(""))
			{
				paragraphCount++;
			}

			if(!(line.equals("")))
			{

				// [!?.:]+ is the sentence delimiter in java				
				String[] sentenceList = line.split("[!?.:]+");

				//characterCount += line.length();

				// \\s+ is the space delimiter in java
				String[] wordList = line.split("\\s+");
				int z=0;
				
				
				for(String word : wordList) {
					
					//Finding the key and value for business logic
					if(z==1) {
						valOfPrevWord = word;
						String businessRule = readLineFromBusinessFile("C://Users//guptakam//Desktop//PROJECTS//ICICI_POC//businessrules.properties");
						businessRuleExecution(businessRule,word);
						z=0;
					}
					if(word.equalsIgnoreCase(wordToFindKey)) {
						prevWord = word;
						z+=1;
					}
					
					

					if(word.equalsIgnoreCase(wordToFind) && !wordToFindKey.equalsIgnoreCase("Date") ) {
						System.out.println(word + " found at Paragraph "+ paragraphCount + " and line "+  sentenceCount +" Line is "+line);
						list.add("page number is : "+page);
						list.add(String.valueOf("paragraph number is "+paragraphCount));
						list.add(String.valueOf("line number is "+sentenceCount));
						list.add("line is "+line);
						
						/////
						details = new Details();
						details.setWordKey(wordToFindKey);
						details.setWord(wordToFind);
						details.setPage(page);
						details.setParagraph(paragraphCount);
						details.setLineNo(sentenceCount);
						details.setLine(line);
						detailsList.add(details);
						
						
					}

					//check if date in line.
					if(wordToFindKey.equalsIgnoreCase("Date") && word.equalsIgnoreCase(wordToFind)) {
						if(checkDate(word)) {
							System.out.println("Date Found in line is : "+ word +" which is found at Paragraph "+ paragraphCount + " and line "+  sentenceCount +" Line is "+line);
							list.add("page number is : "+page);
							list.add(String.valueOf("paragraph number is "+paragraphCount));
							list.add(String.valueOf("line number is "+sentenceCount));
							list.add("line is "+line);
							
							/////
							details = new Details();
							details.setWordKey(wordToFindKey);
							details.setWord(wordToFind);
							details.setPage(page);
							details.setParagraph(paragraphCount);
							details.setLineNo(sentenceCount);
							details.setLine(line);
							detailsList.add(details);
						}
					}
				}
				sentenceCount += sentenceList.length;
				//countWord += wordList.length;
				//whitespaceCount += countWord -1;

			}
		}

		//System.out.println("Total word count = " + countWord);
		//System.out.println("Total number of sentences = " + sentenceCount);
		//System.out.println("Total number of characters = " + characterCount);
		//System.out.println("Number of paragraphs = " + paragraphCount);
		//System.out.println("Total number of whitespaces = " + whitespaceCount);

		fileStream.close();
		input.close();
		reader.close();

		if(file.exists()) {
			file.delete();
			//System.out.println("File Deleted" + file.getName() + " " + file.getPath());
		}

		return detailsList;

	}

	/**
	 * @param pdfFileFullPath
	 * @param tifFile
	 * @param datapath
	 * @param textFileName
	 * @param textFilePath
	 * @param fileType
	 * @param mapextractableFile
	 * @return
	 */
	public LinkedList<LinkedList<Details>> splitPdfFileAndFindWord(String pdfFileFullPath,File tifFile, String datapath,String textFileName,
													String textFilePath,String fileType,HashMap<String,String> mapextractableFile ,HashMap<String,String> mapBusinessFile) {
		
		LinkedList<Details> detailsList = null;
		LinkedList<LinkedList<Details>> detailsListofWords = new LinkedList<LinkedList<Details>>();
		try {

			String pageNum ="";

			if(fileType.equalsIgnoreCase("pdf")) {

				String inFile =pdfFileFullPath.toLowerCase();
				System.out.println ("Reading " + inFile);

				PdfReader reader = new PdfReader(inFile);
				int n = reader.getNumberOfPages();
				System.out.println ("Number of pages : " + n);

				LinkedList<String> files = new LinkedList<String>();

				int i = 0;            
				while ( i < n ) {

					String outFile = inFile.substring(0, inFile.indexOf(".pdf")) + "-" + String.format("%03d", i + 1) + ".pdf"; 
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

				String result="";
				for(String file : files) {
					File pdfFileToProcess = new File(file);

					pageNum =String.valueOf(j);
					System.out.println("Page " + j++);

					String wordToFindKeyValue="";
					String wordToFindKey="";
					result = generateOCRTextFile(pdfFileToProcess, datapath);
					
					for (Map.Entry<String,String> entry : mapextractableFile.entrySet()) 
					{	
						// search  for value
						wordToFindKeyValue = entry.getValue();
						wordToFindKey= entry.getKey();

						if(wordToFindKeyValue == null || wordToFindKeyValue.equalsIgnoreCase("") ||  wordToFindKeyValue.equalsIgnoreCase(" ")) {

						}else {

							detailsList = findWord( wordToFindKeyValue, textFileName, textFilePath,wordToFindKey,result,pageNum,mapBusinessFile);
							
							if(detailsList.size() ==0 ) {
								
							}else {
								detailsListofWords.add(detailsList);
							}
						}
					}

					if (pdfFileToProcess.exists()) {
						pdfFileToProcess.delete();
					}
				}

				// tif file		
			}else if(fileType.equalsIgnoreCase("tif")) {

				String wordToFindKeyValue="";
				String wordToFindKey="";
				pageNum ="1";
				String result = generateOCRTextFile(tifFile, datapath);
				for (Map.Entry<String,String> entry : mapextractableFile.entrySet()) 
				{	
					// search  for value
					wordToFindKeyValue = entry.getValue();
					wordToFindKey= entry.getKey();
					if(wordToFindKeyValue == null || wordToFindKeyValue.equalsIgnoreCase("") ||  wordToFindKeyValue.equalsIgnoreCase(" ")) {

					}else {
						detailsList = findWord(wordToFindKeyValue, textFileName, textFilePath,wordToFindKey,result,pageNum,mapBusinessFile);
						if(detailsList.size() ==0 ) {
							
						}else {
							detailsListofWords.add(detailsList);
						}
					}
				}
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		//return listOfMaps;
		return detailsListofWords;


	}

	public boolean checkDate(String word) {

		String regex1 = "^((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])$";
		String regex2 = "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)";


		if(Pattern.matches(regex1, word)) {
			return true;
		}else if(Pattern.matches(regex2, word)){
			return true;
		}else {
			return false;
		}

	}


	public HashMap<String,String> readPropertyFromMainFile(String mainFilePath) {
		String value = "NULL";
		HashMap<String,String> map = new HashMap<String,String>(); 


		
		
		try (InputStream in = new FileInputStream(mainFilePath)) {
			Properties prop = new Properties();
			prop.load(in);

			for (String property : prop.stringPropertyNames()) {
				value = prop.getProperty(property,"NULL");
				map.put(property, value);

				System.out.println(property + "=" + value);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	public HashMap<String,String> readPropertyFromBusinessFile(String bussiFilePath) {
		String value = "NULL";
		HashMap<String,String> map = new HashMap<String,String>(); 


		try (InputStream in = new FileInputStream(bussiFilePath)) {
			
			Properties prop = new Properties();
			prop.load(in);

			for (String property : prop.stringPropertyNames()) {
				value = prop.getProperty(property,"NULL");
				map.put(property, value);

				System.out.println(property + "=" + value);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
	
	public String readLineFromBusinessFile(String bussiFilePath) {
		BufferedReader br = null;
		String strLine = "";
		String line = "";
		try {
			br = new BufferedReader( new FileReader(bussiFilePath));
			while( (strLine = br.readLine()) != null){
				line = strLine;
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("Unable to find the file: fileName");
		} catch (IOException e) {
			System.err.println("Unable to read the file: fileName");
		}
		return line;
	}
	
	
	public void businessRuleExecution(String businessRule,String word) {
		ScriptEngineManager mgr = new ScriptEngineManager();
	    ScriptEngine engine = mgr.getEngineByName("JavaScript");
	    try {
	    	
	    	System.out.println("Before ---"+ businessRule);
	    	
	    	businessRule = businessRule.replace("Balance:", word);  //Balance:
	    	
	    	System.out.println("New Business Rule "+ businessRule);
	    	
			System.out.println("Evaluating Business Rule: "+engine.eval(businessRule));
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    } 
		
}

