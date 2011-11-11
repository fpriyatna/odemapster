package test.r2rml;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import test.r2o.ODEMapsterTest;

import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementDataTranslateVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;


public class R2RMLTC {
	private static Logger logger = Logger.getLogger(R2RMLTC.class);

	
	private String mappingDirectory = ODEMapsterTest.getMappingDirectoryByOS();
	private String configurationDirectory = mappingDirectory + "r2rml/R2RMLTC/";

	static {
		PropertyConfigurator.configure("log4j.properties");
	}
	
//	public void testR2RMLParser() throws Exception {
//		String configurationDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster2/mappings/r2rml/R2RMLTC000/";
//		String configurationFile = "R2RMLTC000.r2rml.properties";
//
//		AbstractRunner runner = new R2RMLRunner();
//		runner.run(configurationDirectory, configurationFile);
//	}
//
//	
//	public void testR2RMLParser2() throws Exception {
//		String configurationDirectory = "C:/Users/Freddy/Dropbox/oeg/odemapster2/mappings/r2rml/R2RMLTC000/";
//		String mappingDocumentFile = configurationDirectory + "R2RMLTC000.ttl"; 
//		R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
//		md.parse();
//		md.accept(new R2RMLElementDataTranslateVisitor());
//	}

	public void run(String configurationFile, String mappingDocumentFile, String testName) {
		try {
			R2RMLMappingDocument md = new R2RMLMappingDocument(mappingDocumentFile);
			md.accept(new R2RMLElementDataTranslateVisitor(configurationDirectory
					, configurationFile));
			logger.info("------" + testName + " DONE------\n\n");
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error : " + e.getMessage());
			logger.info("------" + testName + " FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		}
	}

	@Test
	public void testR2RMLTCGraph01A() throws Exception {
		String testName = "R2RMLTC-Graph01A";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTCGraph01B() throws Exception {
		String testName = "R2RMLTC-Graph01B";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0000() throws Exception {
		String testName = "R2RMLTC0000";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0001a() throws Exception {
		String testName = "R2RMLTC0001a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0001b() throws Exception {
		String testName = "R2RMLTC0001b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0002a() throws Exception {
		String testName = "R2RMLTC0002a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0002b() {
		String testName = "R2RMLTC0002b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0002c() {
		String testName = "R2RMLTC0002c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0002d() throws Exception {
		String testName = "R2RMLTC0002d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0002e() throws Exception {
		String testName = "R2RMLTC0002e";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0002f() throws Exception {
		String testName = "R2RMLTC0002f";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0002g() throws Exception {
		String testName = "R2RMLTC0002g";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	
	@Test
	public void testR2RMLTC0002h() throws Exception {
		String testName = "R2RMLTC0002h";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0002i() throws Exception {
		String testName = "R2RMLTC0002i";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0003a() throws Exception {
		String testName = "R2RMLTC0003a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0003b() throws Exception {
		String testName = "R2RMLTC0003b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0003c() throws Exception {
		String testName = "R2RMLTC0003c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	
	
	@Test
	public void testR2RMLTC0003d() throws Exception {
		String testName = "R2RMLTC0003d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0004a() throws Exception {
		String testName = "R2RMLTC0004a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0004b() throws Exception {
		String testName = "R2RMLTC0004b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0005a() throws Exception {
		String testName = "R2RMLTC0005a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0006a() throws Exception {
		String testName = "R2RMLTC0006a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0007a() throws Exception {
		String testName = "R2RMLTC0007a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0007b() throws Exception {
		String testName = "R2RMLTC0007b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	
	
	@Test
	public void testR2RMLTC0007c() throws Exception {
		String testName = "R2RMLTC0007c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0007d() throws Exception {
		String testName = "R2RMLTC0007d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0007e() throws Exception {
		String testName = "R2RMLTC0007e";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0007f() throws Exception {
		String testName = "R2RMLTC0007f";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0008a() throws Exception {
		String testName = "R2RMLTC0008a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0009a() throws Exception {
		String testName = "R2RMLTC0009a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0009b() throws Exception {
		String testName = "R2RMLTC0009b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0010a() throws Exception {
		String testName = "R2RMLTC0010a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0010b() throws Exception {
		String testName = "R2RMLTC0010b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0010c() throws Exception {
		String testName = "R2RMLTC0010c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	
	
	@Test
	public void testR2RMLTC0011a() throws Exception {
		String testName = "R2RMLTC0011a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0011b() throws Exception {
		String testName = "R2RMLTC0011b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0012a() throws Exception {
		String testName = "R2RMLTC0012a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0012b() throws Exception {
		String testName = "R2RMLTC0012b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0012c() throws Exception {
		String testName = "R2RMLTC0012c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0012d() throws Exception {
		String testName = "R2RMLTC0012d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0013a() throws Exception {
		String testName = "R2RMLTC0013a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0014a() throws Exception {
		String testName = "R2RMLTC0014a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0014b() throws Exception {
		String testName = "R2RMLTC0014b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0014c() throws Exception {
		String testName = "R2RMLTC0014c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0014d() throws Exception {
		String testName = "R2RMLTC0014d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0014e() throws Exception {
		String testName = "R2RMLTC0014e";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	

	@Test
	public void testR2RMLTC0015a() throws Exception {
		String testName = "R2RMLTC0015a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}

	@Test
	public void testR2RMLTC0015b() throws Exception {
		String testName = "R2RMLTC0015b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}	
	
	@Test
	public void testR2RMLTC0016a() throws Exception {
		String testName = "R2RMLTC0016a";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0016b() throws Exception {
		String testName = "R2RMLTC0016b";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0016c() throws Exception {
		String testName = "R2RMLTC0016c";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0016d() throws Exception {
		String testName = "R2RMLTC0016d";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
	
	@Test
	public void testR2RMLTC0016e() throws Exception {
		String testName = "R2RMLTC0016e";
		String configurationFile = testName + ".r2rml.properties";
		String mappingDocumentFile = configurationDirectory + testName + ".ttl";
		this.run(configurationFile, mappingDocumentFile, testName);
	}
}
