package es.upm.fi.dia.oeg.obdi.core.test;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;

public class TestUtility {
	private static Logger logger = Logger.getLogger(TestUtility.class);
	
	private static String MAPPING_DIRECTORY_WINDOWS = "C:/Users/Freddy Priyatna/Dropbox/Documents/oeg/r2rml-mappings/";
	private static String MAPPING_DIRECTORY_LINUX = "/home/fpriyatna/Dropbox/Documents/oeg/r2rml-mappings/";

	public static String getMappingDirectoryByOS() {
		String osName = System.getProperty("os.name");
		if(osName.startsWith("Linux")) {
			return MAPPING_DIRECTORY_LINUX;
		} else if(osName.startsWith("Windows")) {
			return MAPPING_DIRECTORY_WINDOWS; 
		} else {
			return null;
		}
	}
	
	@Test
	public void testEncode() throws Exception {
		try {
			String str1 = "^Hello{Hi} | [Hola] <World> / %User #, How ~ are \\ you? I $hope,really ; you @are = doing : `well & great + happy!";
			System.out.println("str1 = " + str1);

//			String str2 = Utility.encodeUnsafeChars(str1);
//			System.out.println("str1 after encodeUnsafeChars = " + str2);
			
			String str3 = ODEMapsterUtility.encodeReservedChars(str1);
			System.out.println("str1 after encodeReservedChars = " + str3);

			String str4 = ODEMapsterUtility.encodeUnsafeChars(str1);
			String str5 = ODEMapsterUtility.encodeReservedChars(str4);
			System.out.println("str1 after encodeUnsafeChars,encodeReservedChars = " + str5);

			assertTrue(true);
		} catch(Exception e) {
			e.printStackTrace();
			if(e.getMessage() != null) {
				logger.error("Error : " + e.getMessage());
			}
			logger.error("------testEncode() FAILED------\n\n");
			assertTrue(e.getMessage(), false);
		} 
	}	
}
