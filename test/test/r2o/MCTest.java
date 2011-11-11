package test.r2o;

import static org.junit.Assert.*;

import org.junit.Test;

public class MCTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testMcsuarez02() throws Exception {
		String dir = ODEMapsterTest.getMappingDirectoryByOS() + "mcsuarez02/";
		String r2oConfigurationFile = "Person(mysql).r2o.properties";
		
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}
	
	@Test
	public void testMcsuarez03() throws Exception {
		String dir = ODEMapsterTest.getMappingDirectoryByOS() + "mcsuarez03/";
		String r2oConfigurationFile = "Person(mysql).r2o.properties";
		
		ODEMapsterTest.testProcess(r2oConfigurationFile, dir);
	}

}
