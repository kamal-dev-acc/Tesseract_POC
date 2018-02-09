package poc.icici.tess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class TestTess {


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

		String childFilesPath = "childFilesPath";
		String masterFilesPath = "masterFilesPath";


		//Read data from the mainPropFile file
		mapMainFile =  ttObj.readPropertyFromMainFile(mainPropFile);				

		org.apache.log4j.PropertyConfigurator.configure(mapMainFile.get(log4j_file)); // sets properties file for log4j


		String extraFilePath = mapMainFile.get(extractable_properties);
		String nonExtraFilePath = mapMainFile.get(nonextractable_properties);
		String textFilePath = mapMainFile.get(ocrTextFilePath);  
		String textFileName = mapMainFile.get(ocrTextFileName);
		String corpusDataPathInp =  mapMainFile.get(corpusDataPath);
		String businessFilepa =  mapMainFile.get(businessFilePath);

		String inputFileFullPath = mapMainFile.get(source_directory); 
		String outputFileFullPath = mapMainFile.get(target_directory);

		String childFileFullPath = mapMainFile.get(childFilesPath);
		String masterFileFullPath = mapMainFile.get(masterFilesPath);

		ttObj.execute(ttObj, extraFilePath, textFilePath, textFileName, corpusDataPathInp, inputFileFullPath,outputFileFullPath,childFileFullPath,masterFileFullPath);

		//ttObj.generateOCRTextFile(new File("C:\\Users\\guptakam\\Desktop\\test.doc"), "C://Users//guptakam//Desktop//DEV_TOOLS//ICICI_POC//Tess4J-3.4.3-src//Tess4J");

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
			String corpusDataPathInp, String inputFileFullPath, String outputFileFullPath,String childFilesPath,String masterFilesPath)
					throws InterruptedException, IOException {
		//HashMap<String, String> mapextractableFile;
		
		DbOps dbObj = new DbOps();
		MasterFileOps master=new MasterFileOps();
		
		while(true) {

			//File childFolder = new File(inputFileFullPath);
			
			File childFolder = new File(childFilesPath);
			File[] listOfChildFiles = childFolder.listFiles();
			
			//LinkedList<LinkedList<Details>> detOfComparisonList = null;

			File masterFolder = new File(masterFilesPath);
			File[] listOfmasterFiles = masterFolder.listFiles();

			if(listOfChildFiles.length ==0  || listOfmasterFiles.length==0) {
				Thread.sleep(4000);
				System.out.println("Sleeping for 4000ms.");
			}else {
				//executing the child and master.
				master.executeMasterFile(extraFilePath, textFilePath, textFileName, corpusDataPathInp,
						outputFileFullPath, childFilesPath, masterFilesPath, dbObj, listOfChildFiles,
						listOfmasterFiles);

			}

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










	
	

	
	
	



	
}

