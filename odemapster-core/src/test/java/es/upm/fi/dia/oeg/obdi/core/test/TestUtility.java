package es.upm.fi.dia.oeg.obdi.core.test;

public class TestUtility {
	private static String MAPPING_DIRECTORY_WINDOWS = "C:/Users/Freddy/Dropbox/Documents/oeg/odemapster2/mappings/";
	private static String MAPPING_DIRECTORY_LINUX = "/home/fpriyatna/Dropbox/Documents/oeg/odemapster2/mappings/";
	private static String MAPPING_DIRECTORY_MAC = "/Users/freddypriyatna/Dropbox/Documents/oeg/odemapster2/mappings/";

	public static String getMappingDirectoryByOS() {
		String osName = System.getProperty("os.name");
		if(osName.startsWith("Linux")) {
			return MAPPING_DIRECTORY_LINUX;
		} else if(osName.startsWith("Windows")) {
			return MAPPING_DIRECTORY_WINDOWS; 
		} else if (osName.equalsIgnoreCase("Mac OS X")) {
			return MAPPING_DIRECTORY_MAC;
		} else {
			return null;
		}
	}
}
