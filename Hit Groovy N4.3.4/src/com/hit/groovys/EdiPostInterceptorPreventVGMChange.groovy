
import java.util.List;
import com.navis.cargo.business.model.BillOfLading;
import com.navis.cargo.business.model.BlGoodsBl;
import com.navis.cargo.business.model.GoodsBl;
import com.navis.inventory.InventoryField;
import com.navis.inventory.business.units.Unit;
import com.navis.inventory.business.units.UnitFacilityVisit;
import com.navis.cargo.business.model.InventoryCargoManagerPea;
import com.navis.edi.business.edimodel.EdiPostManagerPea;
import com.navis.external.edi.entity.AbstractEdiPostInterceptor;
import com.navis.external.edi.entity.EEdiPostInterceptor;
import com.navis.framework.persistence.DatabaseHelper;
import com.navis.framework.persistence.HibernatingEntity;
import com.navis.framework.metafields.MetafieldIdFactory;
import com.navis.framework.business.Roastery;
import java.util.Map;
import com.navis.framework.util.ValueObject;

import com.navis.inventory.business.atoms.UfvTransitStateEnum;
import com.navis.argo.business.atoms.FreightKindEnum;
import com.navis.argo.business.atoms.UnitCategoryEnum;

import org.apache.log4j.*;
import org.apache.log4j.xml.*;

public class EdiPostInterceptorPreventVGMChange extends AbstractEdiPostInterceptor {
	private DatabaseHelper _dbHelper;
	private final String CANNOT_CHANGE_VGM_EXPORT_YARD_UNITS = "No es posible modificar el peso para las unidades con peso verificado";

	public void afterEdiPost(org.apache.xmlbeans.XmlObject inXmlTransactionDocument,
			HibernatingEntity inHibernatingEntity, Map inParams) {

		Unit unit = (Unit) inHibernatingEntity;
		UnitFacilityVisit ufv;
		
		if (unit != null) 
		{
			ufv = unit.getActiveUfvNowActiveInAnyUnit();
		}

		if (ufv != null) {

			if ((unit.getUnitCategory() == UnitCategoryEnum.EXPORT)
					&& (ufv.getUfvTransitState() == UfvTransitStateEnum.S40_YARD)
					&& (unit.getUnitFreightKind() == FreightKindEnum.FCL)) {

				Double newVGMWy = unit.getUnitGoodsAndCtrWtKgVerfiedGross();
				//Double currentVGMWy = Double.parseDouble(getPesoUnidad(unit.unitId));
				
				Double currentVGMWy = Double.parseDouble(getPesoUnidad(unit.getUnitId()));

				if ((newVGMWy != currentVGMWy) && ((unit.getUnitVgmEntity() != "CE-HAINA TERMINAL")
						&& (unit.getUnitVgmEntity() != "RE-HAINA TERMINAL"))) {

					unit.setFieldValue(InventoryField.UNIT_GOODS_AND_CTR_WT_KG_VERFIED_GROSS, currentVGMWy);
					//log("VGM => Intento de cambio en el peso de la unidad =>" + unit.unitId);

					log("VGM => Intento de cambio en el peso de la unidad =>" + unit.getUnitId());
					
					// Esta funcion permite lanzar un menjae de error durante el
					// posteo del EDI
					// La deje aqui por si mas adelante cambian las reglas y se
					// quiere bloquear el posteo del EDI
					// registerError(CANNOT_CHANGE_VGM_EXPORT_YARD_UNITS+ );

				}

			}

		}

	}

	//
	private String getPesoUnidad(String unidad) {
		_dbHelper = (DatabaseHelper) Roastery.getBean(DatabaseHelper.BEAN_ID);
		String query = "select goods_ctr_wt_kg_vgm from inv_unit where visit_state = '1ACTIVE' and id = '" + unidad
				+ "'";

		List oLista = _dbHelper.queryForList(query);

		if (oLista == null) {
			return "0";
		}

		return oLista.toString().replace("[", "").replace("]", "").replace("goods_ctr_wt_kg_vgm:", "");

	}

}