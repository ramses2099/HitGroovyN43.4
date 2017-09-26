import com.navis.external.framework.AbstractExtensionCallback;
import com.navis.external.framework.EExtensionCallback;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.request.AbstractSimpleRequest;
import com.navis.external.framework.request.ESimpleReadRequest;
import com.navis.external.framework.ui.AbstractTableViewCommand;
import com.navis.framework.portal.KeyValuePair;
import com.navis.framework.portal.QueryUtils;
import com.navis.framework.portal.UserContext;
import com.navis.framework.portal.context.UserContextUtils;
import com.navis.framework.portal.query.DomainQuery;
import com.navis.framework.portal.query.PredicateFactory;
import com.navis.framework.presentation.FrameworkPresentationUtils
import com.navis.framework.ulc.server.application.view.ViewHelper;
import com.navis.www.services.argoservice.ArgoServiceLocator;
import com.navis.www.services.argoservice.ArgoServicePort;
import com.navis.external.framework.ui.EUIExtensionHelper;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.framework.business.Roastery;
import com.navis.framework.metafields.MetafieldId;
import com.navis.framework.metafields.entity.EntityId;
import com.navis.framework.persistence.Entity;
import com.navis.framework.util.message.MessageLevel;
import com.navis.services.business.event.GroovyEvent;
import com.navis.external.framework.ui.command.AbstractUiCommandProvider;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navis.billing.business.model.Invoice;
import com.navis.billing.BillingField;
import com.navis.framework.presentation.FrameworkPresentationUtils;
//import com.navis.argo.presentation.reports.DownloadManager;
import com.navis.argo.webservice.types.v1_0.GenericInvokeResponseWsType;
import com.navis.argo.webservice.types.v1_0.ResponseType;
import com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType;
import javax.xml.rpc.Stub;

/*
 * Mod by Rob Rojas, to generate report in Servlet environment from stored report definitions
 * */

class GetReportWebserviceClient extends AbstractTableViewCommand{


			private static final String ARGO_SERVICE_URL ="http://172.16.0.144:11080/billing/services/argoservice";
			private static String ERRORS = "3";

			public void execute(EntityId inEntityId, List<Serializable> inGkeys,Map<String, Object> inParams) {


				String invDraft = buscarInvoicePorPK(inEntityId.getEntityName(),inGkeys[0]);
				
				
				//log("Entity :" + inEntityId.getEntityName());

				/*					
				EUIExtensionHelper extHelper = getExtensionHelper();
				String dialogTitle = "Testing table view command extension";
				extHelper.showMessageDialog(MessageLevel.INFO, dialogTitle, "Hello! " + tp);
				*/
				

				//DownloadManager myDownMan;
				GenericInvokeResponseWsType response;
				ResponseType commonResponse;
				String webserviceResponse;
				String reportUrl;
				String reportID;
				String xmlQuery;
				
				// Imprimo reporte 1
				xmlQuery = "<report-def>" +
						 "<get-report-url report-name=\"ALMACENAJE_A4_PAGE\">" +
						 "<parameters>" +
						 "<parameter name=\"DRAFT\"" +
						 " value=\"" + invDraft + "\"/>" +
						 "</parameters>" +
					   	 "</get-report-url>" +
						 "</report-def>"; 
				 
			//myDownMan = new DownloadManager();
			response = callGenericWebservice(xmlQuery);
			commonResponse = response.getCommonResponse();
			if (commonResponse.getStatus().equals(ERRORS)) {
			System.err.println("Web service returned error:\n" + commonResponse.getStatusDescription());
			}

			// getting report URL with webservice response
			webserviceResponse = commonResponse.getQueryResults(0).getResult();
			reportUrl = webserviceResponse.substring(webserviceResponse.indexOf('>') + 1, webserviceResponse.lastIndexOf('<'));
			reportID = webserviceResponse.substring(webserviceResponse.indexOf('=') + 1, webserviceResponse.lastIndexOf('<'));

			//myDownMan.showDocument(reportID);
			FrameworkPresentationUtils.displayDocument(reportUrl);
			// FIN - Imprimo reporte 1
			

			// Imprimo reporte 2
			xmlQuery = "<report-def>" +
					 "<get-report-url report-name=\"ALMACENAJE_A4_PAGE_COPIA1\">" +
					 "<parameters>" +
					 "<parameter name=\"DRAFT\"" +
					 " value=\"" + invDraft + "\"/>" +
					 "</parameters>" +
				   	 "</get-report-url>" +
					 "</report-def>"; 
			 
		//myDownMan = new DownloadManager();
		response = callGenericWebservice(xmlQuery);
		commonResponse = response.getCommonResponse();
		if (commonResponse.getStatus().equals(ERRORS)) {
		System.err.println("Web service returned error:\n" + commonResponse.getStatusDescription());
		}

		// getting report URL with webservice response
		webserviceResponse = commonResponse.getQueryResults(0).getResult();
		reportUrl = webserviceResponse.substring(webserviceResponse.indexOf('>') + 1, webserviceResponse.lastIndexOf('<'));
		reportID = webserviceResponse.substring(webserviceResponse.indexOf('=') + 1, webserviceResponse.lastIndexOf('<'));

		//myDownMan.showDocument(reportID);
		FrameworkPresentationUtils.displayDocument(reportUrl);
		// FIN - Imprimo reporte 2
			

		// Imprimo reporte 3
		xmlQuery = "<report-def>" +
				 "<get-report-url report-name=\"ALMACENAJE_A4_PAGE_COPIA2\">" +
				 "<parameters>" +
				 "<parameter name=\"DRAFT\"" +
				 " value=\"" + invDraft + "\"/>" +
				 "</parameters>" +
			   	 "</get-report-url>" +
				 "</report-def>"; 
		 
	//myDownMan = new DownloadManager();
	response = callGenericWebservice(xmlQuery);
	commonResponse = response.getCommonResponse();
	if (commonResponse.getStatus().equals(ERRORS)) {
	System.err.println("Web service returned error:\n" + commonResponse.getStatusDescription());
	}

	// getting report URL with webservice response
	webserviceResponse = commonResponse.getQueryResults(0).getResult();
	reportUrl = webserviceResponse.substring(webserviceResponse.indexOf('>') + 1, webserviceResponse.lastIndexOf('<'));
	reportID = webserviceResponse.substring(webserviceResponse.indexOf('=') + 1, webserviceResponse.lastIndexOf('<'));

	//myDownMan.showDocument(reportID);
	// FIN - Imprimo reporte 3
	FrameworkPresentationUtils.displayDocument(reportUrl);
			
			
			}
			
/**
* Calls generic web service
* @param inQueryXML
* @return
* @throws Exception
*/
			
public static GenericInvokeResponseWsType callGenericWebservice(String inQueryXML) throws Exception {

	
	GenericInvokeResponseWsType response = null;
	ScopeCoordinateIdsWsType scope = new ScopeCoordinateIdsWsType();
	scope.setOperatorId("HIT");
	scope.setComplexId("SANTO_DOMINGO");
	scope.setFacilityId("HAINA_TERMINAL");
	scope.setYardId("HITYRD");
	// Identify the Web Services host
	ArgoServiceLocator service = new ArgoServiceLocator();
	ArgoServicePort port = service.getArgoServicePort(new URL(ARGO_SERVICE_URL));
	Stub stub = (Stub) port;
	// Specify the User ID and the Password
	stub._setProperty(Stub.USERNAME_PROPERTY, "admin");
	stub._setProperty(Stub.PASSWORD_PROPERTY, "Navis*2017*");
	response = port.genericInvoke(scope, inQueryXML);
	return response;
	}

private Serializable buscarInvoicePorPK(String inEntityName, Serializable inPrimaryKey) {

	  MetafieldId field = BillingField.INVOICE_DRAFT_NBR;
	  Serializable InvPk = (Serializable) ViewHelper.getEntityFieldValue(inEntityName, inPrimaryKey, field);
	  
    return InvPk;
  }


}