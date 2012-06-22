package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

import org.apache.log4j.Logger;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.datatranslator.R2ODataTranslator;

public class R2ORunner extends AbstractRunner {


	public R2ORunner() {
		this.dataTranslator = new R2ODataTranslator();
		this.parser = new R2OParser();
	}

	public R2ORunner(AbstractDataTranslator dataTranslator) {
		this.dataTranslator = dataTranslator;
	}

	private static Logger logger = Logger.getLogger(R2ORunner.class);
	private static final String PRIMITIVE_OPERATIONS_FILE = "primitiveOperations.cfg";
	//public static ConfigurationProperties configurationProperties;
	public static R2OPrimitiveOperationsProperties primitiveOperationsProperties;




	private R2OPrimitiveOperationsProperties loadPrimitiveOperationsFile(String primitiveOperationsFile) throws Exception{
		logger.debug("Loading R2O operations file : " + primitiveOperationsFile);
		try {
			R2OPrimitiveOperationsProperties primitiveOperationsProperties = 
					new R2OPrimitiveOperationsProperties(primitiveOperationsFile);
			return primitiveOperationsProperties;
		} catch(Exception e) {
			logger.error("Error loading primitive operations file : " + PRIMITIVE_OPERATIONS_FILE);
			throw e;
		}
	}

	private void materializeSPARQLSolution(String outputFileName, String sql) {
		try {

		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error in materializing sparql solution.");
		}

	}



//	public String run(String mappingDirectory, String r2oConfigurationFile) throws Exception {
//		String status = null;
//
//		ZUtils.addCustomFunction("concat", 2);
//		ZUtils.addCustomFunction("substring", 3);
//		ZUtils.addCustomFunction("convert", 2);
//		ZUtils.addCustomFunction("coalesce", 2);
//		ZUtils.addCustomFunction("abs", 1);
//		ZUtils.addCustomFunction("lower", 1);
//		ZUtils.addCustomFunction("length", 1);
//
//		//loading operations file
//		R2ORunner.primitiveOperationsProperties = 
//				this.loadPrimitiveOperationsFile(PRIMITIVE_OPERATIONS_FILE);
//
//		//Loading R2O configuration file
//		R2ORunner.configurationProperties = 
//				this.loadConfigurationFile(mappingDirectory, r2oConfigurationFile);
//
//		//loading ontology file
//		String ontologyFilePath = configurationProperties.getOntologyFilePath();
//
//		//parsing r2o mapping document
//		R2OParser parser = new R2OParser(); 
//		String r2oMappingDocumentPath = configurationProperties.getMappingDocumentFilePath();
//		R2OMappingDocument originalMappingDocument = 
//				(R2OMappingDocument) parser.parse(r2oMappingDocumentPath);
//
//		//test the parsing result
//		parser.testParseResult(configurationProperties.getMappingDocumentFilePath(), originalMappingDocument);
//
//		String outputFileName = configurationProperties.getOutputFilePath();
//
//
//		//parsing sparql file path
//		String queryFilePath = configurationProperties.getQueryFilePath();
//		//		R2OMappingDocument translationResultMappingDocument = null;
//		Collection<R2OMappingDocument> translationResultMappingDocuments = new ArrayList<R2OMappingDocument>();
//
//		if(this.sparqQuery == null) {
//			//process SPARQL file
//			if(queryFilePath != null && !queryFilePath.equals("") ) {
//				logger.info("Parsing query file : " + queryFilePath);
//				this.sparqQuery = QueryFactory.read(queryFilePath);
//			}
//		}
//
//		if(this.sparqQuery == null) {
//			//			translationResultMappingDocument = originalMappingDocument;
//			translationResultMappingDocuments.add(originalMappingDocument);
//			this.materializeMappingDocuments(outputFileName, originalMappingDocument);
//		} else {
//			//rewrite the SPARQL query if necessary
//			List<Query> queries = new ArrayList<Query>();
//			if(ontologyFilePath == null || ontologyFilePath.equals("")) {
//				queries.add(sparqQuery);
//			} else {
//				//rewrite the query based on the mappings and ontology
//				logger.info("Rewriting query...");
//				Collection <String> mappedOntologyElements = 
//						MappingsExtractor.getMappedPredicatesFromR2O(r2oMappingDocumentPath);
//				String rewritterWrapperMode = RewriterWrapper.fullMode;
//				//RewriterWrapper rewritterWapper = new RewriterWrapper(ontologyFilePath, rewritterWrapperMode, mappedOntologyElements);
//				//queries = rewritterWapper.rewrite(originalQuery);
//				queries = RewriterWrapper.rewrite(sparqQuery, ontologyFilePath, RewriterWrapper.fullMode, mappedOntologyElements, RewriterWrapper.globalMatchMode);
//
//
//				logger.debug("No of rewriting query result = " + queries.size());
//				logger.debug("queries = " + queries);
//			}			
//
//
//			//translate sparql into mappings
//			//			SPARQL2MappingTranslator translator = 
//			//				new SPARQL2MappingTranslator(originalMappingDocument);
//
//			//translate sparql into sql
//			R2OQueryTranslator translator = new R2OQueryTranslator(originalMappingDocument);
//			translator.setOptimizeTripleBlock(this.configurationProperties.isOptimizeTB());
//			translator.setSubQueryElimination(this.configurationProperties.isSubQueryElimination());
//			Document xmlDoc = XMLUtility.createNewXMLDocument();
//			Connection conn = this.configurationProperties.getConn();
//
//			Element rootElement = null;
//			Element headElement = null;
//			Element resultsElement = null;
//			List<Element> headElements = null;
//			List<String> headElementsString = null;
//
//
//
//			for(Query query : queries) {
//				//				translationResultMappingDocument = translator.processQuery(query);
//				try {
//
//
//					logger.debug("query(i) = " + query);
//					String sparql2SQLResult = translator.translate(query).toString();
//					logger.debug("sparql2sql = \n" + sparql2SQLResult);
//
//					ResultSet rs = DBUtility.executeQuery(conn, sparql2SQLResult);
//					ResultSetMetaData rsmd = rs.getMetaData();
//
//
//					//create root
//					if(rootElement == null) {
//						String rootString = "sparql";
//						rootElement = xmlDoc.createElement(rootString);
//						xmlDoc.appendChild(rootElement);
//					}
//
//					//create header
//					if(headElement == null) {
//						String headString = "head";
//						headElement = xmlDoc.createElement(headString);
//						rootElement.appendChild(headElement);
//						headElements = super.createHeadElementFromColumnNames(rsmd, xmlDoc);
//						headElementsString = new ArrayList<String>();
//						for(Element element : headElements) {
//							headElement.appendChild(element);
//							headElementsString.add(element.getAttribute("name"));
//						}						
//					}
//
//					//create results
//					if(resultsElement == null) {
//						String resultsString = "results";
//						resultsElement = xmlDoc.createElement(resultsString);
//						rootElement.appendChild(resultsElement);						
//					}
//
//
//					String resultString = "result";
//					String bindingString = "binding";
//					int i=0;
//					while(rs.next()) {
//						Element resultElement = xmlDoc.createElement(resultString);
//						resultsElement.appendChild(resultElement);
//						Iterator<String> headElementsStringIterator = headElementsString.iterator(); 
//						while(headElementsStringIterator.hasNext()) {
//							String columnLabel = headElementsStringIterator.next();
//							Element bindingElement = xmlDoc.createElement(bindingString);
//							bindingElement.setAttribute("name", columnLabel);
//							bindingElement.setTextContent(rs.getString(columnLabel));
//							resultElement.appendChild(bindingElement);
//						}
//						i++;
//					}
//					status = i  + " instance(s) retrieved ";
//					logger.info(status);
//					//translationResultMappingDocuments.add(translator.processQuery(query));
//				} catch(Exception e) {
//					//e.printStackTrace();
//					logger.error("error processing query, error message = " + e.getMessage());
//					throw e;
//				}
//
//			}
//			XMLUtility.saveXMLDocument(xmlDoc, outputFileName);
//			//logger.debug("translationResult = " + translationResultMappingDocument);			 
//		}
//
//		logger.info("**********************DONE****************************\n\n");
//		return status;
//
//
//	}





	public static void main(String args[]) {
		try {
			R2ORunner runner = new R2ORunner();
			String r2oFile = null;
			if(args == null || args.length == 0) {
				r2oFile = "r2o.properties";
			} else {
				r2oFile = args[0];
			}
			runner.run(null, r2oFile);
		} catch(Exception e) {
			logger.error("Exception occured!");
			logger.error("Error message = " + e.getMessage());
		}
	}

}
