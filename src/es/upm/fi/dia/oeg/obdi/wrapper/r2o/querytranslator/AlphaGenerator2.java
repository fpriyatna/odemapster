package es.upm.fi.dia.oeg.obdi.wrapper.r2o.querytranslator;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import Zql.ZExp;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import es.upm.fi.dia.oeg.obdi.wrapper.r2o.R2OMappingDocument;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2OConditionalExpression;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.element.R2ODatabaseTable;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.model.mapping.R2OConceptMapping;
import es.upm.fi.dia.oeg.obdi.wrapper.r2o.unfolder.R2OConditionalExpressionUnfolder;

public class AlphaGenerator2 extends AbstractAlphaGenerator {
	private static Logger logger = Logger.getLogger(AlphaGenerator2.class);


	public AlphaGenerator2(Map<Node, R2OConceptMapping> mapNodeConceptMapping,
			R2OMappingDocument mappingDocument) {
		super(mapNodeConceptMapping, mappingDocument);
	}

	@Override
	ZQuery calculateAlpha(Triple tp) throws Exception {
		Node subject = tp.getSubject();

		R2OConceptMapping cm = this.mapNodeConceptMapping.get(subject);
		R2ODatabaseTable databaseTable = cm.getHasTable();
		ZQuery query = new ZQuery();

		ZSelectItem selectItem = new ZSelectItem("*");
		Vector<ZSelectItem> selectItems = new Vector<ZSelectItem>();
		selectItems.add(selectItem);
		query.addSelect(selectItems);

		ZFromItem fromItem = new ZFromItem(databaseTable.getName());
		String alias = databaseTable.getAlias();
		if(alias != null) {
			fromItem.setAlias(databaseTable.getAlias());
		}

		Vector<ZFromItem> fromItems = new Vector<ZFromItem>();
		fromItems.add(fromItem);

		query.addFrom(fromItems);

		R2OConditionalExpression appliesIf = cm.getAppliesIf();
		if(appliesIf != null) {
			R2OConditionalExpressionUnfolder ceUnfolder = new R2OConditionalExpressionUnfolder(appliesIf);
			ZExp whereExpression = ceUnfolder.unfoldDelegableConditionalExpression();
			query.addWhere(whereExpression);
		}

		return query;
	}

	@Override
	ZQuery calculateAlpha(Collection<Triple> triples) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
