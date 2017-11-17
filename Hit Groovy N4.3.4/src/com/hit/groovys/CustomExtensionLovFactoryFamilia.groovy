import com.navis.framework.presentation.lovs.Lov
import com.navis.external.framework.ui.lov.AbstractExtensionLovFactory
import com.navis.framework.presentation.lovs.Style
import com.navis.external.framework.ui.lov.ELovKey
import com.navis.framework.persistence.DatabaseHelper;
import com.navis.framework.business.Roastery;
import java.util.List;
import java.util.Map;
import com.navis.framework.util.ValueObject;
import java.util.LinkedHashMap;
import com.navis.framework.presentation.lovs.list.DynamicLov;

class CustomExtensionLovFactoryFamilia extends AbstractExtensionLovFactory 
{
	private DatabaseHelper _dbHelper;

	public Lov getLov(ELovKey inKey)
	 {	

			_dbHelper = (DatabaseHelper) Roastery.getBean(DatabaseHelper.BEAN_ID);
	    	String query= "select customfamili_IdFamilia,customfamili_Descripcion from CUSTOM_FAMILIA_MERCANCIA";
    	
	    	ValueObject valor; 
	    	
	    	int i = 0;
	    	List lista = _dbHelper.queryForList(query);
	    	ValueObject[] arregloValores = new ValueObject[lista.size()];
	    	
	    	LinkedHashMap mapa = new LinkedHashMap();

		for (Object pk : lista)
		{
			if (pk.customfamili_Descripcion != null)
			{
				valor = new ValueObject(pk.customfamili_Descripcion);
				arregloValores[i] = valor;
				mapa.put(pk.customfamili_Descripcion,String.valueOf(pk.customfamili_IdFamilia));
				i++;
				
			}
		}
		
		DynamicLov listaValor = DynamicLov.create(mapa);
		listaValor.setStyle(Style.STYLE_DEFAULT);
		return listaValor;
		
	}
}
