package demo.sdmxsource.webservice.main.chapter2;

import java.io.File;

import org.sdmxsource.sdmx.api.factory.ReadableDataLocationFactory;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.superbeans.codelist.CodeSuperBean;
import org.sdmxsource.sdmx.api.model.superbeans.datastructure.DataStructureSuperBean;
import org.sdmxsource.sdmx.api.model.superbeans.datastructure.DimensionSuperBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class Chapter2PrintSuperBeans {

	@Autowired
	private StructureParsingManager structureParsingManager;

	@Autowired
	private ReadableDataLocationFactory rdlFactory;

	private void printStructures(File structureFile) {
		ReadableDataLocation rdl = rdlFactory.getReadableDataLocation(structureFile);
		StructureWorkspace workspace = structureParsingManager.parseStructures(rdl);

		for(DataStructureSuperBean dsd : workspace.getSuperBeans().getDataStructures()) {
			printSuperBean(dsd) ; 
		}
	}

	private void printSuperBean(DataStructureSuperBean dsdSb) {
		System.out.println(dsdSb.getName());
		int i = 1;
		for(DimensionSuperBean dimSb : dsdSb.getDimensions()) {
			System.out.println(i +": "+ dimSb.getConcept().getName());
			if(dimSb.getCodelist(true) != null) {
				int j = 1;
				for(CodeSuperBean code : dimSb.getCodelist(true).getCodes()) {
					printCodeSuperBean(code, j, 0);
					j++;
				}
			}
			i++;
		}
	}

	private void printCodeSuperBean(CodeSuperBean code, int idx, int depth) {
		depth++;
		printCodeName(code.getName(), idx, depth);
		for(CodeSuperBean child : code.getChildren()) {
			printCodeSuperBean(child, idx, depth);
		}
	}

	private void printCodeName(String name, int idx, int depth) {
		StringBuilder sb = new StringBuilder();
		for(int a = 0; a < depth; a++) {
			sb.append("--");
		}
		System.out.println(sb.toString() + idx +" :" + name);
	}

	public static void main(String[] args) {
		//Step 1 - Get the Application Context
		ClassPathXmlApplicationContext applicationContext = 
				new ClassPathXmlApplicationContext("spring/spring-beans-chapter1.xml");

		//Step 2 - Get the main class from the Spring beans container
		Chapter2PrintSuperBeans main = 
				applicationContext.getBean(Chapter2PrintSuperBeans.class);

		//Step 3 - Create a Readable Data Location from the File
		File structureFile = new File("src/main/resources/structures/chapter2/structures_full.xml");

		main.printStructures(structureFile);
		
		applicationContext.close();
	}
	
}
