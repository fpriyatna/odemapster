package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.DatatypeMapper;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractDataTranslator;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.engine.AbstractUnfolder;
import es.upm.fi.dia.oeg.obdi.core.engine.RDBQueryEvaluator;
import es.upm.fi.dia.oeg.obdi.core.exception.InvalidConfigurationPropertiesException;
import es.upm.fi.dia.oeg.obdi.core.exception.PostProcessorException;
import es.upm.fi.dia.oeg.obdi.core.exception.QueryTranslatorException;
import es.upm.fi.dia.oeg.obdi.core.materializer.AbstractMaterializer;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractMappingDocument;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLGraphMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLLogicalTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLPredicateObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLRefObjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLSubjectMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTriplesMap;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model.R2RMLTermMap.TermMapType;

public class R2RMLElementDataTranslateVisitor extends AbstractDataTranslator 
implements R2RMLElementVisitor {
	private static Logger logger = Logger.getLogger(R2RMLElementUnfoldVisitor.class);
	

	public R2RMLElementDataTranslateVisitor(
			ConfigurationProperties properties) {
		super(properties);
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		this.setUnfolder(unfolder);
	}
	
	public R2RMLElementDataTranslateVisitor(String configurationDirectory
			, String configurationFile) {
		super(configurationDirectory, configurationFile);
		AbstractUnfolder unfolder = new R2RMLElementUnfoldVisitor();
		this.setUnfolder(unfolder);
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
		Collection<AbstractConceptMapping> triplesMaps = 
				mappingDocument.getConceptMappings();
		if(triplesMaps != null) {
			for(AbstractConceptMapping triplesMap : triplesMaps) {
				try {
					((R2RMLTriplesMap)triplesMap).accept(this);
				} catch(Exception e) {
					logger.error("error while translating data of triplesMap : " + triplesMap);
					if(e.getMessage() != null) {
						logger.error("error message = " + e.getMessage());
					}
					
					//e.printStackTrace();
					throw new QueryTranslatorException(e.getMessage(), e);
				}
			}
		}
		//this.materializer.materialize();
	}



	public void translateObjectMap(R2RMLTermMap objectMap, ResultSet rs
			, Map<String, String> mapColumnType, String subjectGraphName, String predicateobjectGraphName
			, String predicateMapUnfoldedValue, String objectMapUnfoldedValue
			) throws SQLException {

		
		if(objectMap != null && objectMapUnfoldedValue != null) {
			String objectMapTermType = objectMap.getTermType();

			if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(objectMapTermType)) {
				try {
					objectMapUnfoldedValue = ODEMapsterUtility.encodeURI(objectMapUnfoldedValue);
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
						SQLSelectItem selectItem = SQLSelectItem.createSQLItem(dbType, columnName, null);
						
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
				
				objectMapUnfoldedValue = ODEMapsterUtility.encodeLiteral(objectMapUnfoldedValue);
				if(AbstractRunner.configurationProperties != null) {
					if(AbstractRunner.configurationProperties.isLiteralRemoveStrangeChars()) {
						objectMapUnfoldedValue = ODEMapsterUtility.removeStrangeChars(objectMapUnfoldedValue);
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
				try {
					objectMapUnfoldedValue = ODEMapsterUtility.encodeURI(objectMapUnfoldedValue);
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
				} catch(Exception e) {
					
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

	public Object visit(R2RMLLogicalTable logicalTable) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(R2RMLMappingDocument mappingDocument) throws QueryTranslatorException {
		try {
			this.translateData(mappingDocument);
		} catch (Exception e) {
			logger.error("error during data translation process : " + e.getMessage());
			throw new QueryTranslatorException(e.getMessage());
		}

		return null;
	}

	public Object visit(R2RMLObjectMap objectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(R2RMLRefObjectMap refObjectMap) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(R2RMLTermMap r2rmlTermMap) {
		// TODO Auto-generated method stub
		return null;
	}

	public void translateData(R2RMLTriplesMap triplesMap, String sqlQuery) throws SQLException {
		Connection conn = this.properties.openConnection();
		ResultSet rs = RDBQueryEvaluator.evaluateQuery(sqlQuery, conn);
		this.translateData(triplesMap, rs);
		rs.close();
		conn.close();		
	}

	public void translateData(R2RMLTriplesMap triplesMap, ResultSet rs) throws SQLException {
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
				
				String mappedDatatype = DatatypeMapper.getMappedType(columnType);
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
				R2RMLGraphMap subjectGraph = subjectMap.getGraphMap();
				if(subjectGraph != null) {
					//String subjectGraphAlias = subjectGraph.getAlias();
					subjectGraphName = subjectGraph.getUnfoldedValue(rs, null);
					if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(subjectGraph.getTermType())) {
						try {
							subjectGraphName = ODEMapsterUtility.encodeURI(subjectGraphName);
						} catch(Exception e) {
							logger.warn("Error encoding subject graph value : " + subjectGraphName);
						}					
					}
				}
				
				//String logicalTableAlias = subjectMap.getAlias();
				String logicalTableAlias = triplesMap.getLogicalTable().getAlias();
				
				String subjectValue = subjectMap.getUnfoldedValue(
						rs, logicalTableAlias);
				if(subjectValue == null) {
					logger.debug("null value in the subject triple!");
				} else {
					if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(subjectMap.getTermType())) {
						try {
							subjectValue = ODEMapsterUtility.encodeURI(subjectValue);
						} catch(Exception e) {
							logger.warn("Error encoding subject value : " + subjectValue);
						}
					}

					this.materializer.createSubject(subjectMap.isBlankNode(), subjectValue);

					//rdf:type
					Collection<String> classURIs = subjectMap.getClassURIs();
					if(classURIs != null) {
						for(String classURI : classURIs) {
							this.materializer.materializeRDFTypeTriple(subjectValue, classURI, subjectMap.isBlankNode(), subjectGraphName );
						}				
					}
					
					//translate predicate object map
					Collection<R2RMLPredicateObjectMap> predicateObjectMaps = triplesMap.getPredicateObjectMaps();
					logger.debug("predicateObjectMaps.size() = " + predicateObjectMaps.size());

					for(R2RMLPredicateObjectMap predicateObjectMap : predicateObjectMaps){
						R2RMLPredicateMap predicateMap = predicateObjectMap.getPredicateMap();
						String predicateMapUnfoldedValue = 
								predicateMap.getUnfoldedValue(rs, null);

						R2RMLGraphMap predicateobjectGraph = predicateObjectMap.getGraphMap();
						String predicateobjectGraphName = null;
						if(predicateobjectGraph != null ) {
							predicateobjectGraphName = 
									predicateobjectGraph.getUnfoldedValue(rs, null);
							if(R2RMLConstants.R2RML_IRI_URI.equalsIgnoreCase(predicateobjectGraph.getTermType())) {
								try {
									predicateobjectGraphName = ODEMapsterUtility.encodeURI(predicateobjectGraphName);
								} catch(Exception e) {
									logger.warn("Error encoding object graph value : " + predicateobjectGraphName);
								}					
							}
						}

						//translate object map
						R2RMLObjectMap objectMap = predicateObjectMap.getObjectMap();
						if(objectMap != null) {
							//retrieve the alias from predicateObjectMap, not triplesMap!
							String alias = predicateObjectMap.getAlias();
							//String alias = triplesMap.getLogicalTable().getAlias();
							
							String objectMapUnfoldedValue = 
									objectMap.getUnfoldedValue(rs, alias);
							this.translateObjectMap(objectMap, rs, mapXMLDatatype
									, subjectGraphName, predicateobjectGraphName
									, predicateMapUnfoldedValue, objectMapUnfoldedValue
									);

						}


						//translate refobject map
						R2RMLRefObjectMap refObjectMap = predicateObjectMap.getRefObjectMap();
						if(refObjectMap != null) {
							R2RMLElementUnfoldVisitor r2rmlUnfolder = 
									(R2RMLElementUnfoldVisitor) this.unfolder;
//							String joinQueryAlias = refObjectMap.getAlias();
							String joinQueryAlias2 = 
									r2rmlUnfolder.getMapRefObjectMapAlias().get(refObjectMap);
							
							R2RMLSubjectMap parentSubjectMap = 
									refObjectMap.getParentTriplesMap().getSubjectMap();
							//String parentSubjectValue = parentSubjectMap.getUnfoldedValue(rs, refObjectMap.getAlias());
							String parentSubjectValue = parentSubjectMap.getUnfoldedValue(rs, joinQueryAlias2);

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
				
			}



		}

	}
	
	public Object visit(R2RMLTriplesMap triplesMap) throws Exception {
//		String sqlQuery = triplesMap.accept(
//				new R2RMLElementUnfoldVisitor()).toString();
		R2RMLElementUnfoldVisitor r2rmlUnfolder = (R2RMLElementUnfoldVisitor) this.unfolder;
		String sqlQuery = triplesMap.accept(r2rmlUnfolder).toString();
		this.translateData(triplesMap, sqlQuery);
		return null;
	}

}
