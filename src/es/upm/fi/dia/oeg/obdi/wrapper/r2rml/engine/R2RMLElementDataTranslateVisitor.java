package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import Zql.ZSelectItem;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;

import es.upm.fi.dia.oeg.obdi.Utility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.engine.QueryEvaluator;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.materializer.NTripleMaterializer;
import es.upm.fi.dia.oeg.obdi.core.materializer.RDFXMLMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLQuery;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.PostProcessorException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2ORunner;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLDatatypeMapper;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.exception.R2RMLTranslateException;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLGraphMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model.R2RMLTermMap.TermMapType;

public class R2RMLElementDataTranslateVisitor extends AbstractDataTranslator implements R2RMLElementVisitor {
	private static Logger logger = Logger.getLogger(R2RMLElementUnfoldVisitor.class);
	private ConfigurationProperties properties;
	//private Writer out;
	//private OutputStream fileOut;
	private R2RMLElementUnfoldVisitor unfolder;

	//private Connection conn;
	//private String outputFileName;

	public R2RMLElementDataTranslateVisitor() {	}

	public R2RMLElementDataTranslateVisitor(String configurationDirectory
			, String configurationFile, R2RMLElementUnfoldVisitor unfolder) {
		try {
			this.properties = new ConfigurationProperties(configurationDirectory, configurationFile);
			this.unfolder = unfolder;
		} catch (IOException e) {
			logger.error("IO error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (InvalidConfigurationPropertiesException e) {
			logger.error("invalid configuration error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("Database error while loading configuration file : " + configurationFile);
			logger.error("error message = " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected Object processCustomFunctionTransformationExpression(
			Object argument) throws PostProcessorException {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public void setMaterializer(AbstractMaterializer materializer) {
		this.materializer = materializer;
		
	}

	@Override
	public void translateData(AbstractMappingDocument mappingDocument)
			throws Exception {
		this.visit((R2RMLMappingDocument) mappingDocument);
		
	}



	public void translateObjectMap(R2RMLTermMap objectMap, ResultSet rs
			, Map<String, String> mapColumnType, String subjectGraphName, String predicateobjectGraphName
			, String predicateMapUnfoldedValue, String objectMapUnfoldedValue
			) throws SQLException {

		
		if(objectMap != null && objectMapUnfoldedValue != null) {
			String objectMapTermType = objectMap.getTermType();

			if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(objectMapTermType)) {
				try {
					objectMapUnfoldedValue = Utility.encodeURI(objectMapUnfoldedValue);
				} catch(Exception e) {
					logger.warn("Error encoding object value : " + objectMapUnfoldedValue);
				}					
			}

			if(R2RMLConstants.R2RML_LITERAL_URI.equalsIgnoreCase(objectMapTermType)) {
				String datatype = objectMap.getDatatype();
				String language = null;
				language = objectMap.getLanguageTag();
				
				if(objectMap.getTermMapType() == TermMapType.COLUMN) {
					if(datatype == null) {
						String columnName = objectMap.getColumnName();
						String dbType = this.properties.getDatabaseType();
						//SQLSelectItem selectItem = new SQLSelectItem(columnName);
						SQLSelectItem selectItem = SQLSelectItem.createSQLItem(dbType, columnName);
						
						datatype = mapColumnType.get(columnName);
						if(datatype == null) {
							datatype = mapColumnType.get(selectItem.getColumn());
						}
					}
				}
				
				if(datatype != null) {
					if(XSDDatatype.XSDdateTime.getURI().toString().equals(datatype)) {
						objectMapUnfoldedValue = objectMapUnfoldedValue.replaceAll(" ", "T");
					}
				}
				
				if(subjectGraphName == null && predicateobjectGraphName == null) {
					this.materializer.materializeDataPropertyTriple(predicateMapUnfoldedValue
							, objectMapUnfoldedValue, datatype, language, null );
				} else {
					if(subjectGraphName != null) {
						this.materializer.materializeDataPropertyTriple(
								predicateMapUnfoldedValue, objectMapUnfoldedValue
								, datatype, language, subjectGraphName );
					}
					
					if(predicateobjectGraphName != null) {
						if(subjectGraphName == null || 
								!predicateobjectGraphName.equals(subjectGraphName)) {
							this.materializer.materializeDataPropertyTriple(
									predicateMapUnfoldedValue, objectMapUnfoldedValue
									, datatype, language, predicateobjectGraphName);							
						}

					}
				}
			} else if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(objectMapTermType)) {
				if(subjectGraphName == null && predicateobjectGraphName == null) {
					this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, false, null );
				} else {
					if(subjectGraphName != null) {
						this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, false, subjectGraphName );
					}
					if(predicateobjectGraphName != null) {
						if(subjectGraphName == null || 
								!predicateobjectGraphName.equals(subjectGraphName)) {
							this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, false, predicateobjectGraphName );
						}
						
					}
				}
			} else if(R2RMLConstants.R2RML_BLANKNODE_URI.equalsIgnoreCase(objectMapTermType)) {
				if(subjectGraphName == null && predicateobjectGraphName == null) {
					this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, true, null );
				} else {
					if(subjectGraphName != null) {
						this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, true, subjectGraphName );
					}
					if(predicateobjectGraphName != null) {
						if(subjectGraphName == null || 
								!predicateobjectGraphName.equals(subjectGraphName)) {
							this.materializer.materializeObjectPropertyTriple(predicateMapUnfoldedValue, objectMapUnfoldedValue, true, predicateobjectGraphName );
						}
						
					}
				}					
			} else {
				logger.warn("Undefined term type for object map : " + objectMap);
			}

		}
	}

	@Override
	public Object visit(R2RMLLogicalTable logicalTable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(R2RMLMappingDocument mappingDocument) throws R2RMLTranslateException {
		String outputFileName = properties.getOutputFilePath();
		String rdfLanguage = properties.getRdfLanguage();

		try {
			if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_NTRIPLE)) {
				materializer = new NTripleMaterializer(outputFileName);
			} else if(rdfLanguage.equalsIgnoreCase(R2OConstants.OUTPUT_FORMAT_RDFXML)) {
				String jenaMode = properties.getJenaMode();
				Model model = Utility.createJenaModel(jenaMode);
				materializer = new RDFXMLMaterializer(outputFileName, model);
			} else {
				materializer = new NTripleMaterializer(outputFileName);
			}

			Collection<AbstractConceptMapping> triplesMaps = 
					mappingDocument.getTriplesMaps();
			if(triplesMaps != null) {
				for(AbstractConceptMapping triplesMap : triplesMaps) {
					try {
						((R2RMLTriplesMap)triplesMap).accept(this);
					} catch(Exception e) {
						logger.error("error while translating data of triplesMap : " + triplesMap);
						logger.error("error message = " + e.getMessage());
						e.printStackTrace();
						throw new R2RMLTranslateException(e.getMessage(), e);
					}
				}
			}
			this.materializer.materialize();
		} catch (FileNotFoundException fnfe) {
			logger.error("File not found : " + outputFileName);
			logger.error("error message = " + fnfe.getMessage());
			//fnfe.printStackTrace();
			throw new R2RMLTranslateException(fnfe.getMessage());
		} catch (IOException e) {
			logger.error("IO error while loading file : " + outputFileName);
			logger.error("error message = " + e.getMessage());
			//e.printStackTrace();
			throw new R2RMLTranslateException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("error during data translation process : " + e.getMessage());
			//e.printStackTrace();
			throw new R2RMLTranslateException(e.getMessage());
		}

		return null;
	}

	@Override
	public Object visit(R2RMLObjectMap objectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(R2RMLRefObjectMap refObjectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(R2RMLTermMap r2rmlTermMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(R2RMLTriplesMap triplesMap) throws Exception {
//		String sqlQuery = triplesMap.accept(
//				new R2RMLElementUnfoldVisitor()).toString();
		String sqlQuery = triplesMap.accept(this.unfolder).toString();




		
		Connection conn = this.properties.openConnection();
		ResultSet rs = QueryEvaluator.evaluateQuery(sqlQuery, conn);
		Map<String, String> mapXMLDatatype = new HashMap<String, String>();
		Map<String, Integer> mapDBDatatype = new HashMap<String, Integer>();
		ResultSetMetaData rsmd = null;
		try {
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			for (int i=0; i<columnCount; i++) {
				String columnName = rsmd.getColumnName(i+1);
				int columnType= rsmd.getColumnType(i+1);
				
				//logger.info("rsmd.getColumnClassName(i+1) = " + rsmd.getColumnClassName(i+1));
				//logger.info("rsmd.getColumnTypeName(i+1) = " + rsmd.getColumnTypeName(i+1));
				
				String mappedDatatype = R2RMLDatatypeMapper.getMappedType(columnType);
				mapXMLDatatype.put(columnName, mappedDatatype);
				mapDBDatatype.put(columnName, new Integer(columnType));
			}
		} catch(Exception e) {
			//e.printStackTrace();
			logger.warn("Unable to detect database columns!");
		}

		while(rs.next()) {
			//translate subject map
			R2RMLSubjectMap subjectMap = triplesMap.getSubjectMap();
			String subjectGraphName = null;
			if(subjectMap != null) {
				//String logicalTableAlias = subjectMap.getAlias();
				String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
				
				String subjectValue = subjectMap.getUnfoldedValue(
						rs, logicalTableAlias, rsmd);
				if(subjectValue != null && R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(
						subjectMap.getTermType())) {
					try {
						subjectValue = Utility.encodeURI(subjectValue);
					} catch(Exception e) {
						logger.warn("Error encoding subject value : " + subjectValue);
					}
				}
				this.materializer.createSubject(subjectMap.isBlankNode(), subjectValue);
				R2RMLGraphMap subjectGraph = subjectMap.getGraphMap();
				if(subjectGraph != null) {
					//String subjectGraphAlias = subjectGraph.getAlias();
					subjectGraphName = subjectGraph.getUnfoldedValue(rs, null, rsmd);
					if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(subjectGraph.getTermType())) {
						try {
							subjectGraphName = Utility.encodeURI(subjectGraphName);
						} catch(Exception e) {
							logger.warn("Error encoding subject graph value : " + subjectGraphName);
						}					
					}
				}

				//rdf:type
				Collection<String> classURIs = subjectMap.getClassURIs();
				if(classURIs != null) {
					for(String classURI : classURIs) {
						this.materializer.materializeRDFTypeTriple(subjectValue, classURI, subjectMap.isBlankNode(), subjectGraphName );
					}				
				}				
			}


			//translate predicate object map
			Collection<R2RMLPredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
			logger.debug("predicateObjectMaps.size() = " + predicateObjectMaps.size());

			for(R2RMLPredicateObjectMap predicateObjectMap : predicateObjectMaps){
				R2RMLPredicateMap predicateMap = predicateObjectMap.getPredicateMap();
				String predicateMapUnfoldedValue = 
						predicateMap.getUnfoldedValue(rs, null, rsmd);

				R2RMLGraphMap predicateobjectGraph = predicateObjectMap.getGraphMap();
				String predicateobjectGraphName = null;
				if(predicateobjectGraph != null ) {
					predicateobjectGraphName = 
							predicateobjectGraph.getUnfoldedValue(rs, null, rsmd);
					if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(predicateobjectGraph.getTermType())) {
						try {
							predicateobjectGraphName = Utility.encodeURI(predicateobjectGraphName);
						} catch(Exception e) {
							logger.warn("Error encoding object graph value : " + predicateobjectGraphName);
						}					
					}
				}

				//translate object map
				R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
				if(objectMap != null) {
					//String alias = objectMap.getAlias();
					String alias = triplesMap.getLogicalTable().getAlias();
					
					String objectMapUnfoldedValue = 
							objectMap.getUnfoldedValue(rs, alias, rsmd);
					this.translateObjectMap(objectMap, rs, mapXMLDatatype
							, subjectGraphName, predicateobjectGraphName
							, predicateMapUnfoldedValue, objectMapUnfoldedValue
							);

				}


				//translate refobject map
				R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap();
				if(refObjectMap != null) {
					String joinQueryAlias = refObjectMap.getAlias();
					String joinQueryAlias2 = this.unfolder.getMapRefObjectMapAlias().get(refObjectMap);
					
					R2RMLSubjectMap parentSubjectMap = 
							refObjectMap.getParentTriplesMap().getSubjectMap();
					//String parentSubjectValue = parentSubjectMap.getUnfoldedValue(rs, refObjectMap.getAlias());
					String parentSubjectValue = parentSubjectMap.getUnfoldedValue(
							rs, joinQueryAlias, rsmd);

					if(parentSubjectValue != null) {
						this.translateObjectMap(parentSubjectMap, rs, mapXMLDatatype, subjectGraphName
								, predicateobjectGraphName, predicateMapUnfoldedValue, parentSubjectValue
								);
						
					}


					//					if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(parentSubjectMap.getTermType())) {
					//						try {
					//							parentSubjectValue = Utility.encodeURI(parentSubjectValue);
					//							logger.info("parentSubjectValue = " + parentSubjectValue);
					//						} catch(Exception e) {
					//							logger.warn("Error encoding subject value : " + parentSubjectValue);
					//						}
					//					}

				}


			}
		}

		rs.close();
		conn.close();



		return null;
	}

}
