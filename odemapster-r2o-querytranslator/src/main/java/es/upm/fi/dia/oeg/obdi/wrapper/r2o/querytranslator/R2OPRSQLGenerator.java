package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.core.engine.Constants;
import es.upm.fi.dia.oeg.obdi.core.model.AbstractConceptMapping;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractBetaGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractPRSQLGenerator;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.AbstractQueryTranslator.POS;
import es.upm.fi.dia.oeg.obdi.core.querytranslator.NameGenerator;
import es.upm.fi.dia.oeg.obdi.core.sql.SQLSelectItem;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OConstants;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.URIUtility;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OColumnRestriction;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseColumn;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OTransformationExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;

public class R2OPRSQLGenerator extends AbstractPRSQLGenerator {
	private Map<Triple, R2OConceptMapping> mapTripleCM;
	private Map<Node, String> mapNodeKey;

	
	public R2OPRSQLGenerator(
			Map<Node, Set<AbstractConceptMapping>> mapInferredTypes,
			Map<Triple, R2OConceptMapping> mapTripleCM,
			Map<Node, String> mapNodeKey) {
		super(mapInferredTypes);
		this.mapTripleCM = mapTripleCM;
		this.mapNodeKey = mapNodeKey;
	}


	@Override
	public Collection<ZSelectItem> genPRSQL(
			Triple tp, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		R2OConceptMapping cmSubject = this.mapTripleCM.get(tp);
		return this.genPRSQL(tp, betaGenerator, nameGenerator, cmSubject);
	}
	
	@Override
	protected Collection<ZSelectItem> genPRSQLObject(Node object, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		
		Collection<ZSelectItem> selectItems = super.genPRSQLObject(object, tp, betaGenerator, cmSubject, nameGenerator);
		ZSelectItem selectItem = selectItems.iterator().next();
		
		Collection<AbstractConceptMapping> cmObjects = this.mapInferredTypes.get(object);
		if(cmObjects != null) {
			AbstractConceptMapping cmObject = cmObjects.iterator().next();
			if(cmObject != null) {
				
				if(cmSubject.hasWellDefinedURIExpression()) {
					String selectItemColumn2 = selectItem.getExpression() + Constants.KEY_SUFFIX;
					String selectItemAlias2 = selectItem.getAlias() + Constants.KEY_SUFFIX;
					ZSelectItem selectItem2 = new SQLSelectItem(selectItemColumn2);
					selectItem2.setAlias(selectItemAlias2);
					selectItems.add(selectItem2);
					this.mapNodeKey.put(object, selectItemAlias2);
				} 
			}			
		} 
		
		return selectItems;
	}
	
	@Override
	protected ZSelectItem genPRSQLPredicate(Node predicate, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		ZSelectItem selectItem = betaGenerator.calculateBeta(tp, POS.pre);
		selectItem.setAlias(nameGenerator.generateName(tp, predicate));
		return selectItem;
	}
	
	
	@Override
	protected Collection<ZSelectItem> genPRSQLSubject(Node subject, Triple tp,
			AbstractBetaGenerator betaGenerator,
			AbstractConceptMapping cmSubject, NameGenerator nameGenerator) throws Exception {
		Collection<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		
		R2OTransformationExpression cmSubjectURIAs = ((R2OConceptMapping)cmSubject).getURIAs();
		ZSelectItem selectItemSubject = betaGenerator.calculateBeta(tp, POS.sub);
		String selectItemSubjectAlias = nameGenerator.generateName(tp, subject); 
		selectItemSubject.setAlias(selectItemSubjectAlias);
		selectItems.add(selectItemSubject); //line 21
		
		if(URIUtility.isWellDefinedURIExpression(cmSubjectURIAs)) {
			R2OColumnRestriction pkColumnRestriction = (R2OColumnRestriction) cmSubjectURIAs.getLastRestriction();
			R2ODatabaseColumn pkColumn = pkColumnRestriction.getDatabaseColumn();
			//ZSelectItem selectItemSubjectPK = new R2OSelectItem(pkColumn.getFullColumnName());
			ZSelectItem selectItemSubjectPK = new SQLSelectItem(((R2OConceptMapping)cmSubject).generatePKColumnAlias());
			String selectItemSubjectPKAlias = selectItemSubjectAlias + Constants.KEY_SUFFIX;
			selectItemSubjectPK.setAlias(selectItemSubjectPKAlias);
			selectItems.add(selectItemSubjectPK);
			this.mapNodeKey.put(subject, selectItemSubjectPKAlias);
		} 

		return selectItems;
	}
	
	
	public Collection<ZSelectItem> genPRSQLTB(
			Collection<Triple> tripleBlock, AbstractBetaGenerator betaGenerator, NameGenerator nameGenerator)
			throws Exception {
		Triple firstTriple = tripleBlock.iterator().next();
		R2OConceptMapping cmSubject = this.mapTripleCM.get(firstTriple);

		Collection<ZSelectItem> prList = this.genPRSQLTB(tripleBlock, betaGenerator, nameGenerator, cmSubject);
		return prList;
	}

}
