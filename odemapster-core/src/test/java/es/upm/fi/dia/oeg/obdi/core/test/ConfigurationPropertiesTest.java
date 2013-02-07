package es.upm.fi.dia.oeg.obdi.core.test;

import static org.junit.Assert.*;

import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;

public class ConfigurationPropertiesTest {

	@Test
	public void test() throws Exception {
		String configurationPropertiesFilePath = 
				"C:/Users/Freddy/Dropbox/Documents/oeg/odemapster2/mappings/r2rml-mappings/r2rml-mysql-pssa/pssa.r2rml.properties";
		
		ConfigurationProperties cp = new ConfigurationProperties(configurationPropertiesFilePath);
	}

}
