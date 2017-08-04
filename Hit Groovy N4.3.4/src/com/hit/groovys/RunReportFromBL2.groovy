package com.hit.groovys


import com.navis.argo.business.reports.DownloadManager;
import com.navis.argo.webservice.types.v1_0.GenericInvokeResponseWsType;
import com.navis.argo.webservice.types.v1_0.ResponseType;
import com.navis.argo.webservice.types.v1_0.ScopeCoordinateIdsWsType;

import com.navis.cargo.InventoryCargoField;
import com.navis.framework.ulc.server.application.view.ViewHelper;
import com.navis.www.services.argoservice.ArgoServiceLocator;
import com.navis.www.services.argoservice.ArgoServicePort;
import com.navis.external.framework.ui.AbstractTableViewCommand;
import com.navis.framework.metafields.MetafieldId;
import com.navis.framework.metafields.entity.EntityId;


import java.io.Serializable;
import java.net.URL;

import java.util.List;
import java.util.Map;

import javax.xml.rpc.Stub;



/*
 * Mod by Ing. Jose Encarnacion to generate report
 * */

class RunReportFromBL2 extends AbstractTableViewCommand {

	private static final String ARGO_SERVICE_URL = "http://172.16.0.123:9080/apex/services/argoservice";
	private static String ERRORS = "3";

	public void execute(EntityId inEntityId, List<Serializable> inGkeys, Map<String, Object> inParams) {

		try {

			
			String BL = getBLPorPK(inEntityId.getEntityName(), inGkeys.get(0));

			String xmlQuery = "<report-def>" +
					 "<get-report-url report-name=\"CREHIT\">" +
					 "<parameters>" +
					 "<parameter name=\"BL\"" +
					 " value=\"" + BL + "\"/>" +
					 "</parameters>" +
						"</get-report-url>" +
					 "</report-def>";

			// log("XML : " + xmlQuery);

			
			GenericInvokeResponseWsType response = callGenericWebservice(xmlQuery);
			ResponseType commonResponse = response.getCommonResponse();
			if (commonResponse.getStatus().equals(ERRORS)) {
				System.err.println("Web service returned error:\n" + commonResponse.getStatusDescription());
			}

			// getting report URL with webservice response
			String webserviceResponse = commonResponse.getQueryResults(0).getResult();
			String reportUrl = webserviceResponse.substring(webserviceResponse.indexOf('>') + 1,
					webserviceResponse.lastIndexOf('<'));
			String reportID = webserviceResponse.substring(webserviceResponse.indexOf('=') + 1,
					webserviceResponse.lastIndexOf('<'));

			log(" Report Url:" + reportUrl + " Report ID:" + reportID +"");
			

			DownloadManager downLoad = new DownloadManager();
			
			downLoad.
			
		
			
		} catch (Exception e) {

			log("Error : " + e.getStackTrace().toString());
		}

	}

	//
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
		stub._setProperty(Stub.PASSWORD_PROPERTY, "Navistest");
		response = port.genericInvoke(scope, inQueryXML);
		return response;
	}

	//
	private String getBLPorPK(String inEntityName, Serializable inPrimaryKey) {
		MetafieldId field = InventoryCargoField.BL_NBR;
		String BLPk = ViewHelper.getEntityFieldValue(inEntityName, inPrimaryKey, field).toString();
		return BLPk;
	}

}