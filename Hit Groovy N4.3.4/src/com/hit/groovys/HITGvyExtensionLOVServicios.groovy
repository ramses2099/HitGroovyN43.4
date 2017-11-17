import com.navis.reference.ReferenceField
import com.navis.framework.portal.query.OuterQueryMetafieldId
import com.navis.external.framework.ui.lov.ELovKey
import com.navis.framework.portal.QueryUtils

public class CustomExtensionLovFactory extends AbstractExtensionLovFactory 
{

	public Lov getLov(ELovKey inKey)
	 {	
		 DomainQuery dq = QueryUtils.createDomainQuery("com.hit.almacenaje.CustomServicio")
		dq.addDqField("customEntityFields.customservicDescripcion");
		dq.addDqField("customEntityFields.customservicCodigo");

		//final DomainQuery dq =QueryFactory.createDomainQuery(DynamicHibernatingEntity.customEntityFields.CustomServicio);
		//dq.addDqField(customEntityFields.customservicCodigo);

		DomainQueryLov dqLov = new DomainQueryLov(dq, Style.LABEL_ONLY);

		return dqLov;
	}
}
