package demo.sdmxsource.webservice.main.chapter1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sdmxsource.sdmx.api.constants.STRUCTURE_OUTPUT_FORMAT;
import org.sdmxsource.sdmx.api.model.format.StructureFormat;
import org.sdmxsource.sdmx.sdmxbeans.model.SdmxStructureFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import demo.sdmxsource.webservice.main.chapter1.builder.AgencySchemeBuilder;
import demo.sdmxsource.webservice.main.chapter1.writer.SampleStructureWriter;

@Service
public class Chapter1WritingStructures {
	@Autowired
	private SampleStructureWriter fileWriter;

	@Autowired
	private AgencySchemeBuilder agencySchemeBuilder;
	
	public static void main(String[] args) throws IOException {
		//Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = 
				new ClassPathXmlApplicationContext("spring/spring-beans-chapter1.xml");
		
		//Step 2 - Get the main class from the Spring beans container
		Chapter1WritingStructures main = 
				applicationContext.getBean(Chapter1WritingStructures.class);
		
		//Step 3 - Get an Output Stream to the File
		OutputStream out = main.getFileOutputStream();

		//Step 4 - Define the output format
		STRUCTURE_OUTPUT_FORMAT sdmxFormat = STRUCTURE_OUTPUT_FORMAT.SDMX_V21_STRUCTURE_DOCUMENT;
		StructureFormat outputFormat = new SdmxStructureFormat(sdmxFormat);

		//Step 5 - Write the structures out to the fie
		main.fileWriter.writeStructureToFile(outputFormat, out);
		
		applicationContext.close();
		
	}
	
	private OutputStream getFileOutputStream() throws IOException {
		File structureFile = new File("src/main/resources/webservice_structures.xml");
		System.out.println("File Deleted : "+ structureFile.delete());
		System.out.println("File Created : "+structureFile.createNewFile());
		return new FileOutputStream(structureFile);
	}
}


