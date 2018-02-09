package poc.icici.tess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class MasterFileOps {
	
	/**
	 * @param ttObj
	 * @param extraFilePath
	 * @param textFilePath
	 * @param textFileName
	 * @param corpusDataPathInp
	 * @param outputFileFullPath
	 * @param childFilesPath
	 * @param masterFilesPath
	 * @param dbObj
	 * @param listOfChildFiles
	 * @param listOfmasterFiles
	 */
	public void executeMasterFile( String extraFilePath, String textFilePath, String textFileName,
			String corpusDataPathInp, String outputFileFullPath, String childFilesPath, String masterFilesPath,
			DbOps dbObj, File[] listOfChildFiles, File[] listOfmasterFiles) {
		HashMap<String, String> mapextractableFile;
		LinkedList<LinkedList<Details>> detailsOfMasterList;
		ChildFilesOps childObj = new ChildFilesOps();
		
		//Read Data from the Master file to compare with child files.
		for (File fileMaster : listOfmasterFiles) {
			if (fileMaster.isFile()) {
				LinkedList<MasterTableObject> masterList = new LinkedList<MasterTableObject>();
				LinkedList<ChildTableObject> childList = new LinkedList<ChildTableObject>();
				LinkedList<BusinessRuleObject> rulesList = new LinkedList<BusinessRuleObject>();

				String masterFileName = fileMaster.getName();
				int masterFileId;
				int childId;
				int ruleId;
				String businessRule="";

				//check for master file id in table.
				masterList = dbObj.getMasterDetails(masterFileName);
				for (MasterTableObject master : masterList) {
					masterFileId = master.getMasterFileId();
					
					String fileTypeMaster = fileMaster.getName().substring(fileMaster.getName().length() - 3);
					File tiffFilemaster = generateTifFile(fileMaster.getAbsolutePath(), fileTypeMaster);

					//Read data from the extractable_properties file
					mapextractableFile =  readPropertyFromMapExtractableFile(extraFilePath);	
					
					//Read Data from the Master File. 
					detailsOfMasterList = splitPdfFileAndFindWordForMaster(fileMaster.getAbsolutePath(), tiffFilemaster, corpusDataPathInp, textFileName,
																			textFilePath, fileTypeMaster, mapextractableFile);
					
					System.out.println(detailsOfMasterList);

					//check for child details.
					childObj.executeChildFiles( extraFilePath, textFilePath, textFileName, corpusDataPathInp,outputFileFullPath, childFilesPath, masterFilesPath, dbObj, listOfChildFiles,
												masterFileName, masterFileId,detailsOfMasterList);
				}

				
				//Move File to target directory : Master
				moveFileToTarget(masterFilesPath+masterFileName ,outputFileFullPath+masterFileName);
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
	public LinkedList<Details> findpageDetailsForMaster(String textFilePath,String wordToFind,String wordToFindKey,String page
			) throws IOException {

		File file = new File(textFilePath);
		FileInputStream fileStream = new FileInputStream(file);
		InputStreamReader input = new InputStreamReader(fileStream);
		BufferedReader reader = new BufferedReader(input);
		LinkedList<String> list = new LinkedList<String>();
		
		String prevWord = "";
		String valOfPrevWord ="";

		String line;
		Details details=null;
		LinkedList<Details> detailsList = new LinkedList<Details>();

		// Initializing counters
		int sentenceCount = 1;
		int paragraphCount = 1;

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
						wordToFind = word;
						
						//////
						if(word.equalsIgnoreCase(wordToFind) && !wordToFindKey.equalsIgnoreCase("Date") ) {
							System.out.println(word + " found at Paragraph "+ paragraphCount + " and line "+  sentenceCount +" Line is "+line);
							/////
							addDetails(wordToFind, wordToFindKey, page, line, detailsList, sentenceCount,
									paragraphCount);
							
						}
						/////
						//check if date in line.
						if(wordToFindKey.equalsIgnoreCase("Date") && word.equalsIgnoreCase(wordToFind)) {
							if(checkDate(word)) {
								System.out.println("Date Found in line is : "+ word +" which is found at Paragraph "+ paragraphCount + " and line "+  sentenceCount +" Line is "+line);
								
								addDetails(wordToFind, wordToFindKey, page, line, detailsList, sentenceCount,
										paragraphCount);
							}
						}
						
						z=0;
					}
					if(word.equalsIgnoreCase(wordToFindKey)) {
						prevWord = word;
						z+=1;
					}

				}
				sentenceCount += sentenceList.length;

			}
		}

		fileStream.close();
		input.close();
		reader.close();

		if(file.exists()) {
			file.delete();
		}

		return detailsList;

	}

	/**
	 * @param wordToFind
	 * @param wordToFindKey
	 * @param page
	 * @param line
	 * @param detailsList
	 * @param sentenceCount
	 * @param paragraphCount
	 */
	public void addDetails(String wordToFind, String wordToFindKey, String page, String line,
			LinkedList<Details> detailsList, int sentenceCount, int paragraphCount) {
		Details details;
		details = new Details();
		details.setWordKey(wordToFindKey);
		details.setWord(wordToFind);
		details.setPage(page);
		details.setParagraph(paragraphCount);
		details.setLineNo(sentenceCount);
		details.setLine(line);
		detailsList.add(details);
	}

	
	public LinkedList<Details>  findWordForMaster(String wordToFind,String textFileName,String textFilePath,String wordToFindKey,String result,
			String page) throws IOException {

		//Find the occurrence of word in the whole document.
		LinkedList<Details> detailsList = null;
		System.out.println();

		//Finding the Page,Paragraph and line for the word.
		try(  PrintWriter out = new PrintWriter(textFilePath+"//"+textFileName)  ){
			out.println( result );
			out.close();

			detailsList = findpageDetailsForMaster(textFilePath+"//"+textFileName,wordToFind,wordToFindKey,page);

		}
		return detailsList;

	}
	
	public LinkedList<LinkedList<Details>> splitPdfFileAndFindWordForMaster(String pdfFileFullPath,File tifFile, String datapath,String textFileName,
			String textFilePath,String fileType,HashMap<String,String> mapextractableFile
			) {

		LinkedList<LinkedList<Details>> detailsListofWords = new LinkedList<LinkedList<Details>>();
		try {


			if(fileType.equalsIgnoreCase("pdf")) {

				processPdfFile(pdfFileFullPath, datapath, textFileName, textFilePath, mapextractableFile,
						detailsListofWords);
			}else if(fileType.equalsIgnoreCase("tif")) {

				processTifFile(tifFile, datapath, textFileName, textFilePath, mapextractableFile, detailsListofWords);
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		//return listOfMaps;
		return detailsListofWords;
	}

	/**
	 * @param tifFile
	 * @param datapath
	 * @param textFileName
	 * @param textFilePath
	 * @param mapextractableFile
	 * @param detailsListofWords
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void processTifFile(File tifFile, String datapath, String textFileName, String textFilePath,
			HashMap<String, String> mapextractableFile, LinkedList<LinkedList<Details>> detailsListofWords)
			throws FileNotFoundException, IOException {
		LinkedList<Details> detailsList;
		String pageNum;
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
				detailsList = findWordForMaster(wordToFindKeyValue, textFileName, textFilePath,wordToFindKey,result,pageNum);
				if(detailsList.size() ==0 ) {

				}else {
					detailsListofWords.add(detailsList);
				}
			}
		}
	}

	/**
	 * @param pdfFileFullPath
	 * @param datapath
	 * @param textFileName
	 * @param textFilePath
	 * @param mapextractableFile
	 * @param detailsListofWords
	 * @throws IOException
	 * @throws DocumentException
	 * @throws FileNotFoundException
	 * @throws BadPdfFormatException
	 */
	public void processPdfFile(String pdfFileFullPath, String datapath, String textFileName, String textFilePath,
			HashMap<String, String> mapextractableFile, LinkedList<LinkedList<Details>> detailsListofWords)
			throws IOException, DocumentException, FileNotFoundException, BadPdfFormatException {
		LinkedList<Details> detailsList;
		String pageNum;
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

					detailsList = findWordForMaster( wordToFindKeyValue, textFileName, textFilePath,wordToFindKey,result,pageNum);

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
	
	
//	/**
//	 * @param ttObj
//	 * @param mapnonextractableFile
//	 * @param textFilePath
//	 * @param textFileName
//	 * @param corpusDataPathInp
//	 * @param inputFileFullPath
//	 * @param fileType
//	 * @param tiffFile
//	 */
//	public LinkedList<LinkedList<Details>> findDetailsForNonExtractableData(HashMap<String, String> mapnonextractableFile,
//			String textFilePath, String textFileName, String corpusDataPathInp, String inputFileFullPath,
//			String fileType, File tiffFile,String businessFilePa,String businessRule ) {
//
//		LinkedList<LinkedList<Details>> detailsListOfWords = null;
//		detailsListOfWords  = splitPdfFileAndFindWord(inputFileFullPath, tiffFile,corpusDataPathInp,textFileName,textFilePath,fileType,mapnonextractableFile,businessRule);
//		return detailsListOfWords;
//	}
//
//
//	/**
//	 * @param ttObj
//	 * @param mapextractableFile
//	 * @param textFilePath
//	 * @param textFileName
//	 * @param corpusDataPathInp
//	 * @param inputFileFullPath
//	 * @param fileType
//	 * @param tiffFile
//	 */
//	public LinkedList<LinkedList<Details>> findDetailsForExtractableData(TestTess ttObj, HashMap<String, String> mapextractableFile,
//			String textFilePath, String textFileName, String corpusDataPathInp, String inputFileFullPath,
//			String fileType, File tiffFile,String businessRule) {
//
//		LinkedList<LinkedList<Details>> detailsListOfWords = null;
//		detailsListOfWords = splitPdfFileAndFindWord(inputFileFullPath, tiffFile,corpusDataPathInp,textFileName,textFilePath,fileType,mapextractableFile,businessRule);
//		return detailsListOfWords;
//	}
	
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


}
