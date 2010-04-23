package es.upm.fi.dia.oeg.obdi.wrapper.r2o;

public class R2OConstants {
	public static final String R2O_TAG="r2o";
	public static final String CONCEPTMAP_DEF_TAG="conceptmap-def";
	public static final String ATTRIBUTEMAP_DEF_TAG="attributemap-def";
	public static final String URI_AS_TAG="uri-as";
	public static final String SELECTOR_TAG="selector";
	public static final String ARG_RESTRICTION_TAG="arg-restriction";
	public static final String HAS_VALUE_TAG="has-value";
	public static final String HAS_COLUMN_TAG="has-column";
	public static final String HAS_TRANSFORMATION_TAG="has-transformation";
	public static final String APPLIES_IF_TAG="applies-if";
	public static final String APPLIES_IF_TOP_TAG="applies-if-top";
	public static final String CONDITION_TAG="condition";
	public static final String AFTERTRANSFORM_TAG="aftertransform";
	public static final String DESCRIBED_BY_TAG="described-by";
	public static final String AND_TAG = "and";
	public static final String OR_TAG = "or";
	public static final String USE_DBCOL_TAG = "use-dbcol";
	public static final String OPERATION_TAG = "operation";

	//constants related to database mapping elements
	public static final String DBSCHEMA_DESC_TAG = "dbschema-desc";
	public static final String HAS_TABLE_TAG = "has-table";
	public static final String KEYCOL_DESC_TAG = "keycol-desc";
	public static final String FORKEYCOL_DESC_TAG = "forkeycol-desc";
	public static final String NONKEYCOL_DESC_TAG = "nonkeycol-desc";
	
	
	
	
	
	public static final String ON_PARAM_ATTRIBUTE="on-param";
	public static final String NAME_ATTRIBUTE="name";
	public static final String DOCUMENTATION_ATTRIBUTE="documentation";
	public static final String IDENTIFIED_BY_ATTRIBUTE="identified-by";
	public static final String OPER_ID_ATTRIBUTE="oper-id";

	public static final String CONDITIONAL_OPERATOR_EQUALS_NAME = "equals";
	public static final String CONDITIONAL_OPERATOR_EQUALS_NO_ARGUMENTS = "2";
	public static final String CONDITIONAL_OPERATOR_MATCH_REGEXP_NAME = "match-regexp";
	
	public static final String TRANSFORMATION_OPERATOR_CONSTANT_NAME = "constant";
	
	public static final String ONPARAM_STRING = "string";
	public static final String ONPARAM_REGEXP = "regexp";
	
	public static final String[] DELEGABLE_OPERATIONS = {"equals"};
	
	
	public static final String R2OFILE_PROP_NAME = "r2o.file.path";
	public static final String QUERYFILE_PROP_NAME = "query.file.path";
	public static final String ONTOFILE_PROP_NAME = "onto.file.path";
	public static final String ONTOURL_PROP_NAME = "onto.url.path";
	public static final String OUTPUTFILE_PROP_NAME = "output.file.path";
	
	public static final String DATABASE_NAME_PROP_NAME = "database.name";
	public static final String DATABASE_DRIVER_PROP_NAME = "database.driver";
	public static final String DATABASE_URL_PROP_NAME = "database.url";
	public static final String DATABASE_USER_PROP_NAME = "database.user";
	public static final String DATABASE_PWD_PROP_NAME = "database.pwd";
	public static final String DATABASE_TYPE_PROP_NAME = "database.type";
	
	public static final String SPLIT_OUTPUT_PER_CONCEPT = "split_output_per_concept";
	
	//jena specific properties
	public static final String JENA_MODE_TYPE = "jena.mode.type";
	public static final String JENA_MODE_TYPE_MEMORY = "memory";
	public static final String JENA_MODE_TYPE_TDB = "tdb";
	public static final String JENA_MODE_TYPE_HSQL = "hsql";
}