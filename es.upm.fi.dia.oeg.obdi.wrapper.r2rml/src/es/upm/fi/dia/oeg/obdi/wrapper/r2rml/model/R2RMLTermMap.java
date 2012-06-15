package es.upm.fi.dia.oeg.obdi.wrapper.r2rml.model;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import es.upm.fi.dia.oeg.obdi.core.engine.AbstractRunner;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.R2RMLUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2rml.engine.R2RMLElementVisitor;

public abstract class R2RMLTermMap implements R2RMLElement {
	public enum TermMapPosition {SUBJECT, PREDICATE, OBJECT, GRAPH}
	
	//public enum TermType {IRI, BLANK_NODE, LITERAL};
	public enum TermMapType {CONSTANT, COLUMN, TEMPLATE};
	private TermMapType termMapType;
	
	private static Logger logger = Logger.getLogger(R2RMLTermMap.class);;
	
	private String constantValue;
	private String columnName;

	private String termType;//IRI, BlankNode, or Literal
	private String languageTag;
	private String datatype;


	private String template;
	private String inverseExpression;
	


	private TermMapPosition termMapPosition;
	//private String alias;
	
	R2RMLTermMap(Resource resource, TermMapPosition termMapPosition) throws R2RMLInvalidTermMapException {
		this.termMapPosition = termMapPosition;
		
		Statement constantStatement = resource.getProperty(R2RMLConstants.R2RML_CONSTANT_PROPERTY);
		if(constantStatement != null) {
			this.termMapType = TermMapType.CONSTANT;
			this.constantValue = constantStatement.getObject().toString();
		} else {
			Statement columnStatement = resource.getProperty(R2RMLConstants.R2RML_COLUMN_PROPERTY);
			if(columnStatement != null) {
				this.termMapType = TermMapType.COLUMN;
				this.columnName = columnStatement.getObject().toString();
			} else {
				Statement templateStatement = resource.getProperty(R2RMLConstants.R2RML_TEMPLATE_PROPERTY);
				if(templateStatement != null) {
					this.termMapType = TermMapType.TEMPLATE;
					this.template = templateStatement.getObject().toString();
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
		this.termMapPosition = termMapPosition;
		this.termMapType = TermMapType.CONSTANT;
		this.constantValue = constantValue;
		this.termType = this.getDefaultTermType();
	}
	
	@Override
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
	
	public Collection<String> getDatabaseColumnsString() {
		Collection<String> result = new HashSet<String>();
		
		if(this.termMapType == TermMapType.COLUMN) {
			result.add(this.getOriginalValue());
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			String template = this.getOriginalValue();
			Collection<String> attributes = R2RMLUtility.getAttributesFromStringTemplate(template);
			if(attributes != null) {
				for(String attribute : attributes) {
					result.add(attribute);
				}
			}
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
			return this.template;
		} else {
			return null;
		}
	}
	
	public String getResultSetValue(ResultSet rs, String columnName)  {
		String result = null;
		
		try {
			String dbType = AbstractRunner.getConfigurationProperties().getDatabaseType();
			//SQLSelectItem selectItem = new SQLSelectItem(columnName);
			SQLSelectItem selectItem = SQLSelectItem.createSQLItem(dbType, columnName);
			columnName = selectItem.getColumn();
			if(columnName.startsWith("")) {columnName.substring(1);}
			if(columnName.endsWith("")) {columnName.subSequence(0, columnName.length()-1);}
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
			logger.error("error occured when translating result, check your database values for " + columnName);
			logger.error("error message = " + e.getMessage());
		}

		return result;
	}

	public String getTemplate() {
		return template;
	}

	public String getTemplateColumn() {
		TermMapType termMapValueType = this.getTermMapType();
		
		if(termMapValueType == TermMapType.COLUMN) {
			return this.getColumnName();
		} else if(termMapValueType == TermMapType.TEMPLATE) {
			String stringTemplate = this.getTemplate();
			Collection<String> attributes = 
					R2RMLUtility.getAttributesFromStringTemplate(stringTemplate);

			return attributes.iterator().next();
		} else {
			return null;
		}
	}

	public String getTemplateValue(String uri) {
		String result = null;
		
		TermMapType termMapValueType = this.getTermMapType();
		
		if(termMapValueType == TermMapType.TEMPLATE) {
			String stringTemplate = this.getTemplate();
			int beginIndex = stringTemplate.indexOf("{");
			int endIndex = stringTemplate.indexOf("}");
			
			result = uri.substring(beginIndex);
			//result = uri.substring(beginIndex -1 , uri.length());
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
			Collection<String> attributes = 
					R2RMLUtility.getAttributesFromStringTemplate(originalValue);
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
		
		return result;
	}


	public boolean hasWellDefinedURIExpression() {
		boolean result = false;

		String columnName = this.getColumnName();
		if(columnName != null) {
			result = true;
		} else {
			String stringTemplate = this.getTemplate();
			if(stringTemplate != null) {
				Collection<String> attributes = 
						R2RMLUtility.getAttributesFromStringTemplate(stringTemplate);
				if(attributes != null && attributes.size() == 1) {
					result = true;
				}
			} else {
				result = false;
			}
		}

		return result;
	}
	
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
	
	void setConstantValue(String constantValue) {
		this.constantValue = constantValue;
	}
	
	void setTermType(String termType) {
		this.termType = termType;
	}
	
	@Override
	public String toString() {
		String result = "";
		if(this.termMapType == TermMapType.CONSTANT) {
			result = "Constant";
		} else if(this.termMapType == TermMapType.COLUMN) {
			result = "Column";
		} else if(this.termMapType == TermMapType.TEMPLATE) {
			result = "Template";
		}
		
		result += ":" + this.getOriginalValue();
		
		return result;
	}	
}
