package demo.sdmxsource.webservice.main.chapter2;

import java.io.File;

import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class Chapter2ReadingStructures {

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;

	private void readStructures(File structureFile) {
		ReadableDataLocation rdl = rdlFactory.getReadableDataLocation(structureFile);
		StructureWorkspace workspace = structureParsingManager.parseStructures(rdl);

		// Print the contents to the console
		SdmxBeans beans = workspace.getStructureBeans(false);
		for (MaintainableBean currentMaintainable : beans.getAllMaintainables()) {
			SDMX_STRUCTURE_TYPE structureType = currentMaintainable.getStructureType();
			String structureName = currentMaintainable.getName();

			System.out.println(currentMaintainable.getUrn());
			System.out.println(structureType.getType() + " - " + structureName);
			System.out.println(" --- ");
		}
	}

	public static void main(String[] args) {
		// Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"spring/spring-beans-chapter1.xml");

		// Step 2 - Get the main class from the Spring beans container
		Chapter2ReadingStructures main = applicationContext.getBean(Chapter2ReadingStructures.class);

		// Step 3 - Create a Readable Data Location from the File
		File structureFile = new File("src/main/resources/structures/chapter2/structures_full.xml");

		main.readStructures(structureFile);

		applicationContext.close();
	}
}
