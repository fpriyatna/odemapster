package es.upm.fi.dia.oeg.obdi.wrapper.r2o.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import es.upm.fi.dia.oeg.obdi.core.test.TestUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2OFreddyPostProcessor;


public class MCTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testMcsuarez02() throws Exception {
		String dir = TestUtility.getMappingDirectoryByOS() + "mcsuarez02/";
		String r2oConfigurationFile = "Person(mysql).r2o.properties";
		ODEMapsterTest.setDataTranslator(new R2OFreddyPostProcessor());
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	@Test
	public void testMcsuarez03() throws Exception {
		String dir = TestUtility.getMappingDirectoryByOS() + "mcsuarez03/";
		String r2oConfigurationFile = "mcsuarez03.r2o.properties";
		ODEMapsterTest.setDataTranslator(new R2OFreddyPostProcessor());
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

}
