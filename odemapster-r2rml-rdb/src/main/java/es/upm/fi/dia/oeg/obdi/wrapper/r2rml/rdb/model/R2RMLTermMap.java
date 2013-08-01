package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.core.ConfigurationProperties;
import es.upm.fi.dia.oeg.obdi.core.ODEMapsterUtility;
import es.upm.fi.dia.oeg.obdi.core.sql.ColumnMetaData;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElement;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.engine.R2RMLElementVisitor;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.exception.R2RMLInvalidTermMapException;
import es.upm.fi.oeg.obdi.core.utility.RegexUtility;

public class R2RMLTermMap implements R2RMLElement
, IConstantTermMap, IColumnTermMap, ITemplateTermMap {
	private static Logger logger = Logger.getLogger(R2RMLTermMap.class);
	public enum TermMapPosition {SUBJECT, PREDICATE, OBJECT, GRAPH}

	private String termType;//IRI, BlankNode, or Literal
	private String languageTag;
	private String datatype;

	R2RMLTriplesMap owner;

	private ConfigurationProperties configurationProperties;

	//public enum TermType {IRI, BLANK_NODE, LITERAL};
	public enum TermMapType {CONSTANT, COLUMN, TEMPLATE};
	private TermMapType termMapType;

	//for constant type TermMap
	private String constantValue;

	//for column type TermMap
	private String columnName;
	private String columnTypeName;

	//for template type TermMap
	private String templateString;

	private boolean isNullable = true;

	R2RMLTermMap(Resource resource, TermMapPosition termMapPosition, R2RMLTriplesMap owner) 
			throws R2RMLInvalidTermMapException {
		this.configurationProperties = owner.getOwner().getConfigurationProperties();

		this.owner = owner;

		R2RMLLogicalTable logicalTable = this.owner.getLogicalTable();
		//		ResultSetMetaData rsmd = logicalTable.getRsmd();
		Map<String, ColumnMetaData> columnsMetaData = logicalTable.getColumnsMetaData();

		if(columnsMetaData == null) {
			Connection conn = this.owner.getOwner().getConn();
			try {
				logicalTable.buildMetaData(conn);
				columnsMetaData = logicalTable.getColumnsMetaData();				
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}

		Statement constantStatement = resource.getProperty(R2RMLConstants.R2RML_CONSTANT_PROPERTY);
		if(constantStatement != null) {
			this.termMapType = TermMapType.CONSTANT;
			this.constantValue = constantStatement.getObject().toString();
		} else {
			Statement columnStatement = resource.getProperty(R2RMLConstants.R2RML_COLUMN_PROPERTY);
			if(columnStatement != null) {
				this.termMapType = TermMapType.COLUMN;
				this.columnName = columnStatement.getObject().toString();
				if(columnsMetaData != null) {
					ColumnMetaData cmd = columnsMetaData.get(this.columnName);
					if(cmd != null) {
						this.columnTypeName = cmd.getDataType();
						this.isNullable = cmd.isNullable();
					}
				}
			} else {
				Statement templateStatement = resource.getProperty(R2RMLConstants.R2RML_TEMPLATE_PROPERTY);
				if(templateStatement != null) {
					this.termMapType = TermMapType.TEMPLATE;
					this.templateString = templateStatement.getObject().toString();

					Collection<String> pkColumnStrings = this.getTemplateColumns();
					
					boolean isNullableAux = false;
					for(String pkColumnString : pkColumnStrings) {
						if(columnsMetaData != null) {
							ColumnMetaData cmd = columnsMetaData.get(pkColumnString);
							if(cmd != null) {
								this.columnTypeName = cmd.getDataType();
								if(cmd.isNullable()) {
									isNullableAux = true;
								}
							} else {
								logger.warn("metadata not found for: " + pkColumnString);
								isNullableAux = true;
							}
						}
					}
					this.isNullable = isNullableAux;
				} else {
					String termMapType;
					if(this instanceof R2RMLSubjectMap) {
						termMapType = "SubjectMap";
					} else if(this instanceof R2RMLPredicateMap) {
						termMapType = "PredicateMap";
					} else if(this instanceof R2RMLObjectMap) {
						termMapType = "ObjectMap";
					} else if(this instanceof R2RMLGraphMap) {
						termMapType = "GraphMap";
					} else {
						termMapType = "TermMap";
					}
					String errorMessage = "Invalid " + termMapType + " for " + resource.getLocalName();
					logger.error(errorMessage);
					throw new R2RMLInvalidTermMapException(errorMessage);
				}
			}
		}

		Statement datatypeStatement = resource.getProperty(R2RMLConstants.R2RML_DATATYPE_PROPERTY);
		if(datatypeStatement != null) {
			this.datatype = datatypeStatement.getObject().toString();
		}

		Statement languageStatement = resource.getProperty(R2RMLConstants.R2RML_LANGUAGE_PROPERTY);
		if(languageStatement != null) {
			this.languageTag = languageStatement.getObject().toString();
		}

		Statement termTypeStatement = resource.getProperty(R2RMLConstants.R2RML_TERMTYPE_PROPERTY);
		if(termTypeStatement == null) {
			this.termType = this.getDefaultTermType();
		} else {
			this.termType = termTypeStatement.getObject().toString();
		}
	}

	R2RMLTermMap(TermMapPosition termMapPosition, String constantValue) {
		this.termMapType = TermMapType.CONSTANT;
		this.constantValue = constantValue;
		this.termType = this.getDefaultTermType();
	}

	public Object accept(R2RMLElementVisitor visitor) throws Exception {
		Object result = visitor.visit(this);
		return result;
	}


	private String getDefaultTermType() {
		//		if(this.termMapPosition == TermMapPosition.OBJECT && this.termMapType == TermMapType.COLUMN) {
		//			return R2RMLConstants.R2RML_LITERAL_URI;
		//		} else {
		//			return R2RMLConstants.R2RML_IRI_URI;
		//		}
		String result;

		if(this instanceof R2RMLObjectMap) {
			if(this.termMapType == TermMapType.COLUMN || this.languageTag != null || this.datatype != null) {
				result = R2RMLConstants.R2RML_LITERAL_URI;
			} else { 
				result = R2RMLConstants.R2RML_IRI_URI;
			}
		} else {
			result = R2RMLConstants.R2RML_IRI_URI; 
		}

		return result;
	}



	//	public String getAlias() {
	//		return alias;
	//	}

	//	public String getUnfoldedValue(ResultSet rs, Map<String, Integer> mapDBDatatype) throws SQLException {
	//		//return this.getUnfoldedValue(rs, this.alias);
	//		return this.getUnfoldedValue(rs, null, mapDBDatatype);
	//	}




	public String getColumnName() {
		return columnName;
	}

	public List<String> getDatabaseColumnsString() {
		List<String> result = new LinkedList<String>();

		if(this.termMapType == TermMapType.COLUMN) {
			result = new LinkedList<String>();
			result.add(this.getOriginalValue());
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			String template = this.getOriginalValue();
			//Collection<String> attributes = R2RMLUtility.getAttributesFromStringTemplate(template);
			RegexUtility regexUtility = new RegexUtility();
			result = regexUtility.getTemplateColumns(template, true);
		}

		return result;
	}



	public String getDatatype() {
		return datatype;
	}

	public String getLanguageTag() {
		return languageTag;
	}

	public String getOriginalValue() {
		if(this.termMapType == TermMapType.CONSTANT) {
			return this.constantValue;
		} else if(this.termMapType == TermMapType.COLUMN) {
			return this.columnName;
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			return this.templateString;
		} else {
			return null;
		}
	}

	public String getResultSetValue(ResultSet rs, String columnName)  {
		String result = null;

		try {
			String dbType = this.configurationProperties.getDatabaseType();
			//SQLSelectItem selectItem = new SQLSelectItem(columnName);
			SQLSelectItem selectItem = SQLSelectItem.createSQLItem(dbType, columnName, null);
			columnName = selectItem.columnToString();
			if(selectItem.getTable() != null) {
				columnName = selectItem.getTable() + "." + columnName;
			}

			if(this.getDatatype() == null) {
				result = rs.getString(columnName);
			} else if(this.getDatatype().equals(XSDDatatype.XSDdateTime.getURI())) {
				result = rs.getDate(columnName).toString();
			} else {
				result = rs.getString(columnName);
			}			
		} catch(Exception e) {
			//e.printStackTrace();
			logger.error("error occured when translating result: " + e.getMessage());
		}

		return result;
	}

	public String getTemplateString() {
		return templateString;
	}

	public List<String> getTemplateColumns() {
		List<String> result = new Vector<String>();

		TermMapType termMapValueType = this.getTermMapType();

		if(termMapValueType == TermMapType.COLUMN) {
			result.add(this.getColumnName());
		} else if(termMapValueType == TermMapType.TEMPLATE) {
			//Collection<String> attributes = R2RMLUtility.getAttributesFromStringTemplate(stringTemplate);
			RegexUtility regexUtility = new RegexUtility();
			Collection<String> attributes = regexUtility.getTemplateColumns(this.templateString, true);

			result.addAll(attributes);
		}

		return result;
	}

	public Map<String, String> getTemplateValues(String uri) {
		Map<String, String> result = new HashMap<String, String>();

		TermMapType termMapValueType = this.getTermMapType();

		if(termMapValueType == TermMapType.TEMPLATE) {
			String templateString = this.templateString;
			this.getDatabaseColumnsString();
			RegexUtility regexUtility = new RegexUtility();
			result = regexUtility.getTemplateMatching(templateString, uri);
		}

		return result;
	}


	public TermMapType getTermMapType() {
		return termMapType;
	}

	public String getTermType() {
		return termType;
	}


	public String getUnfoldedValue(ResultSet rs, String logicalTableAlias) {
		String result = null;
		String originalValue = this.getOriginalValue();

		if(this.termMapType == TermMapType.COLUMN) {

			if(logicalTableAlias != null && !logicalTableAlias.equals("")) {
				String[] originalValueSplit = originalValue.split("\\.");
				String columnName = originalValueSplit[originalValueSplit.length - 1];
				originalValue = logicalTableAlias + "." + columnName;
			}
			result = this.getResultSetValue(rs, originalValue);
		} else if(this.termMapType == TermMapType.CONSTANT) {
			result = originalValue;
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			//Collection<String> attributes = R2RMLUtility.getAttributesFromStringTemplate(originalValue);
			RegexUtility regexUtility = new RegexUtility();
			Collection<String> attributes = regexUtility.getTemplateColumns(originalValue, true);

			Map<String,String> replacements = new HashMap<String, String>();
			for(String attribute : attributes) {
				String databaseValue;
				String databaseColumn = null;
				if(logicalTableAlias != null) {
					String attributeSplit[] = attribute.split("\\.");
					if(attributeSplit.length == 1) {
						databaseColumn = logicalTableAlias + "." + attribute;
						attribute = attributeSplit[attributeSplit.length - 1];
					} else if(attributeSplit.length > 1) {
						databaseColumn = logicalTableAlias + "." + attributeSplit[attributeSplit.length - 1];
					}
				} else {
					databaseColumn = attribute;
				}
				databaseValue = this.getResultSetValue(rs, databaseColumn);


				if(databaseValue != null) {
					if(R2RMLConstants.R2RML_IRI_URI.equals(this.termType)) {
						if(this.configurationProperties.isEncodeUnsafeChars()) {
							databaseValue = ODEMapsterUtility.encodeUnsafeChars(databaseValue);
						}

						if(this.configurationProperties.isEncodeReservedChars()) {
							databaseValue = ODEMapsterUtility.encodeReservedChars(databaseValue);
						}							

					}
					replacements.put(attribute, databaseValue);
					result = R2RMLUtility.replaceTokens(originalValue, replacements);
				}

				//				if(databaseValue == null) {
				//					result = null;
				//				} else {
				//					replacements.put(attribute, databaseValue);
				//					result = R2RMLUtility.replaceTokens(originalValue, replacements);
				//				}

			}

		}	

		if(result == null) {
			//logger.warn("Unfolded value returns NULL!");
		}
		return result;
	}


	//	public boolean hasWellDefinedURIExpression() {
	//		boolean result = false;
	//
	//		String columnName = this.getColumnName();
	//		if(columnName != null) {
	//			result = true;
	//		} else {
	//			String stringTemplate = this.templateString;
	//			if(stringTemplate != null) {
	//				Collection<String> attributes = 
	//						R2RMLUtility.getAttributesFromStringTemplate(stringTemplate);
	//				if(attributes != null && attributes.size() == 1) {
	//					result = true;
	//				}
	//			} else {
	//				result = false;
	//			}
	//		}
	//
	//		return result;
	//	}

	public boolean isBlankNode() {
		if(R2RMLConstants.R2RML_BLANKNODE_URI.equals(this.getTermType())) {
			return true;
		} else {
			return false;
		}
	}

	//	public void setAlias(String alias) {
	//		this.alias = alias;
	//	}

	public void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}

	void setTermType(String termType) {
		this.termType = termType;
	}

	@Override
	public String toString() {
		String result = "";
		if(this.termMapType == TermMapType.CONSTANT) {
			result = "rr:constant";
		} else if(this.termMapType == TermMapType.COLUMN) {
			result = "rr:column";
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			result = "rr:template";
		}

		result += "::" + this.getOriginalValue();

		if(this.termMapType == TermMapType.COLUMN) {
			result += ":" + this.columnTypeName;
		}

		return result;
	}

	public String getConstantValue() {
		return constantValue;
	}

	//	private int getColumnDataType() {
	//		if(this.columnDataType == null) {
	//			this.columnDataType = QueryTranslatorUtility.getColumnType(rsmd, columnName);
	//		}
	//		return columnDataType;
	//	}
	//
	//	private String getColumnDataTypeName() {
	//		if(this.columnDataTypeName == null) {
	//			this.columnDataTypeName = QueryTranslatorUtility.getColumnTypeName(rsmd, columnName);
	//		}
	//		return columnDataTypeName;
	//	}



	public void setConfigurationProperties(
			ConfigurationProperties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	public void setColumnTypeName(String columnTypeName) {
		this.columnTypeName = columnTypeName;
	}

	public String getColumnTypeName() {
		return columnTypeName;
	}



}
