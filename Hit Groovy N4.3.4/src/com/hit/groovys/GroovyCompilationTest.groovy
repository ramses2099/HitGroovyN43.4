package com.hit.groovys

import com.navis.argo.ArgoAssetsEntity
import com.navis.argo.ArgoAssetsField
import com.navis.argo.business.api.GroovyApi
import com.navis.argo.business.atoms.DigitalAssetTypeEnum
import com.navis.argo.business.event.groovy.GroovyClassCache
import com.navis.framework.business.Roastery
import com.navis.framework.portal.QueryUtils
import com.navis.framework.portal.query.DomainQuery
import com.navis.framework.portal.query.PredicateFactory
import com.navis.argo.business.reports.DigitalAsset
/**
 * Author : Navis
 * Simple Groovy to go over all the groovy codes in the digital assets table
 and report error if it can't be compiled on the deployed system where it is
 run.
 * The code can be enhanced to cover more aspects, this is just a sample.
 Tested this with 1.0 version groovy,
 * to keep it portable. Should you change this, please don't use 1.6 version
 API's like advanced loop syntax etc here to keep it runnable in all versions
 of N4.
 */

public class GroovyCompilationTest extends GroovyApi {
	public String execute() {
		DomainQuery dq =
				QueryUtils.createDomainQuery(ArgoAssetsEntity.DIGITAL_ASSET);
		dq.addDqPredicate(PredicateFactory.eq(ArgoAssetsField.DA_FORMAT,
				DigitalAssetTypeEnum.GROOVY));
		List list = Roastery.getHibernateApi().findEntitiesByDomainQuery(dq);
		Iterator iterator = list.iterator()
		StringBuilder sb = new StringBuilder();
		sb.append("Summary of Groovy Compilation\n");
		String groovyId;
		while (iterator.hasNext()) {
			DigitalAsset da = (DigitalAsset) iterator.next();
			String groovyCode = da.getDaGroovyCode();
			groovyId = da.getDaId();
			try {
				Class groovyClass = new GroovyClassCache().parseGroovy(groovyCode)
				String className = groovyClass.getSimpleName()
				sb.append("SUCCESS : " + groovyId + "\n")
				// in 2.4 following lines can give some hints for runtime violation	of groovy 1.8 rules (used in 2.4)
				if (groovyCode.contains("static final") ||
				groovyCode.contains("final static")){
					log("A known bug with Groovy 1.8 prohibits including the 'static' and 'final' modifiers in the same line: " + className);
				}
			} catch (Exception e) {
				sb.append("FAILED : " + groovyId + "\n").append("Stack" +
						e.getMessage()).append("\n");
			}
		}
		return sb.toString();
	}
}