package es.upm.fi.dia.oeg.obdi.core;

public class Constants {
	public static final String JOINS_TYPE_INNER = "INNER";
	public static final String JOINS_TYPE_LEFT = "LEFT";
	
	public static final String DATABASE_MONETDB = "MonetDB";
	public static final String DATABASE_ORACLE = "Oracle";
	public static final String DATABASE_MYSQL = "MySQL";
	public static final String DATABASE_SQLSERVER = "SQLServer";
	
	//jena specific properties
	public static final String JENA_MODE_TYPE = "jena.mode.type";
	public static final String JENA_MODE_TYPE_MEMORY = "memory";
	public static final String JENA_MODE_TYPE_HSQL = "hsql";
	public static final String JENA_MODE_TYPE_TDB = "tdb";
	public static final String JENA_TDB_DIRECTORY = "jena.tdb.dir";
	public static final String JENA_TDB_FILEBASE = "jena.tdb.filebase";

	//rdf format
	public static final String OUTPUT_FORMAT_RDFXML = "RDF/XML";
	public static final String OUTPUT_FORMAT_RDFXML_ABBREV = "RDF/XML-ABBREV";
	public static final String OUTPUT_FORMAT_NTRIPLE = "N-TRIPLE";
	public static final String OUTPUT_FORMAT_NQUAD= "N-QUAD";
	public static final String OUTPUT_FORMAT_TURTLE = "TURTLE";
	public static final String OUTPUT_FORMAT_N3 = "N3";

	
	//database
	public static final String NO_OF_DATABASE_NAME_PROP_NAME = "no_of_database";
	public static final String DATABASE_NAME_PROP_NAME = "database.name";
	public static final String DATABASE_DRIVER_PROP_NAME = "database.driver";
	public static final String DATABASE_URL_PROP_NAME = "database.url";
	public static final String DATABASE_USER_PROP_NAME = "database.user";
	public static final String DATABASE_PWD_PROP_NAME = "database.pwd";
	public static final String DATABASE_TYPE_PROP_NAME = "database.type";
	public static final String DATABASE_TIMEOUT_PROP_NAME = "database.timeout";

	
	public static final String MAPPINGDOCUMENT_FILE_PATH = "mappingdocument.file.path";
	public static final String QUERYFILE_PROP_NAME = "query.file.path";
	public static final String ONTOFILE_PROP_NAME = "onto.file.path";
	public static final String ONTOURL_PROP_NAME = "onto.url.path";
	public static final String OUTPUTFILE_PROP_NAME = "output.file.path";
	public static final String OUTPUTFILE_RDF_LANGUAGE = "output.rdflanguage";
	public static final String SPLIT_OUTPUT_PER_CONCEPT = "split_output_per_concept";
	
	public static final String OPTIMIZE_TB = "querytranslator.selfjoinelimination";
	public static final String SUBQUERY_ELIMINATION = "querytranslator.subqueryelimination";
	public static final String SUBQUERY_AS_VIEW = "querytranslator.subqueryasview";
	public static final String QUERY_TRANSLATOR_CLASSNAME = "querytranslator.class.name";
	public static final String QUERY_TRANSLATOR_CLASSNAME_DEFAULT = 
			"es.upm.fi.dia.oeg.obdi.wrapper.r2rml.rdb.querytranslator.R2RMLQueryTranslator";
	public static final String QUERY_EVALUATOR_CLASSNAME = "queryevaluator.class.name";
	public static final String QUERY_EVALUATOR_CLASSNAME_DEFAULT = 
			"es.upm.fi.dia.oeg.obdi.core.engine.RDBQueryEvaluator";
	public static final String QUERY_RESULT_WRITER_CLASSNAME = 
			"queryresult.writer.class.name";
	public static final String QUERY_RESULT_WRITER_CLASSNAME_DEFAULT = 
			"es.upm.fi.dia.oeg.obdi.core.engine.XMLWriter";
	public static final String QUERY_RESULT_XMLWRITER_OUTPUT_DEFAULT = "output.rdf.xml";
	
	public static final String REMOVE_STRANGE_CHARS_FROM_LITERAL = "literal.removestrangechars";
	public static final String ENCODE_UNSAFE_CHARS_IN_URI_COLUMN = "uricolumn.encodeunsafecharacters";
	public static final String ENCODE_RESERVED_CHARS_IN_URI_COLUMN = "uricolumn.encodereserveccharacters";
	
	//aliases
	public static final String URI_AS_ALIAS="uri_";
	public static final String RANGE_TABLE_ALIAS="rt_";
	public static final String VIEW_ALIAS="v_";
	public static final String TABLE_ALIAS_PREFIX="t_";
	public static final String KEY_SUFFIX = "_key";
	
}
