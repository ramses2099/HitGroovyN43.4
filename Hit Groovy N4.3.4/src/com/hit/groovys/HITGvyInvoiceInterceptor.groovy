import com.navis.framework.util.internationalization.PropertyKey;
import com.navis.framework.util.internationalization.PropertyKeyFactory;

import com.navis.framework.util.BizViolation
import java.text.SimpleDateFormat
import com.navis.billing.business.model.Invoice;
import com.navis.external.framework.ECallingContext;
import com.navis.external.framework.entity.AbstractEntityLifecycleInterceptor;
import com.navis.external.framework.entity.EEntityView;
import com.navis.external.framework.util.EFieldChanges;
import com.navis.external.framework.util.EFieldChangesView;
import com.navis.framework.portal.FieldChanges;
import com.navis.billing.BillingField;

import com.navis.billing.business.model.TariffRate
import com.navis.billing.business.model.TariffRateHbr;

import com.navis.billing.business.model.CurrencyExchangeRate
import com.navis.argo.business.extract.ChargeableUnitEvent
import com.navis.billing.business.model.InvoiceItem
import com.navis.billing.business.model.Contract
import com.navis.billing.business.model.Currency
import com.navis.billing.business.model.Tariff

import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import groovy.util.slurpersupport.NodeChildren;
import wslite.soap.*

import com.navis.external.framework.ui.EUIExtensionHelper;
import com.navis.external.framework.ui.DefaultEUIExtensionHelper
import com.navis.framework.util.message.MessageLevel

import com.navis.framework.portal.BizResponse

import com.navis.framework.util.BizViolation;
import com.navis.framework.util.internationalization.PropertyKey;
import com.navis.framework.util.internationalization.PropertyKeyFactory;
import com.navis.framework.util.UserMessageImp
import com.navis.framework.util.BizFailure
import com.navis.framework.util.message.MessageCollector;
import com.navis.framework.util.message.MessageLevel;
import com.navis.argo.ArgoExtractField;
import com.navis.billing.business.model.InvoiceParmValue;
import com.navis.billing.business.model.InvoiceType;
import com.navis.billing.business.model.InvoiceHbr;
import com.navis.framework.metafields.MetafieldIdFactory
import java.util.regex.Matcher;

import com.navis.framework.portal.query.DomainQuery;
import com.navis.framework.portal.query.PredicateFactory;
import com.navis.security.SecurityField;
import com.navis.framework.portal.QueryUtils;
import com.navis.argo.business.security.ArgoUser;
import com.navis.framework.portal.UserContext;
import com.navis.argo.ContextHelper;
import com.navis.argo.Interchange.Time;
import com.navis.framework.persistence.HibernateApi;
import com.navis.framework.business.Roastery;


public class InvoiceInterceptor extends AbstractEntityLifecycleInterceptor
{
	 private enum TipoValidacion { Bls_EN_BLANCO,FORMATO_BLs, BL_NO_EXISTE,BL_VISITA_DIFERENTE}
	 private PropertyKey mensajeValidacion;
	 private ChargeableUnitEvent chagbl;
	 private InvoiceItem item;
	 
	public void onUpdate(EEntityView inEntity, EFieldChangesView inOriginalFieldChanges, EFieldChanges inMoreFieldChanges)  throws BizViolation
	{

		try
		{

		Invoice factura  = inEntity._entity;
		InvoiceType tipoFact = factura.getInvoiceInvoiceType();
		
                    ///////// Busca Usuario //////
		
					UserContext userContext = ContextHelper.getThreadUserContext();
					DomainQuery dq = QueryUtils.createDomainQuery("ArgoUser").addDqPredicate(PredicateFactory.eq(SecurityField.BUSER_UID, userContext.getUserId()));
					ArgoUser usuarioActual = (ArgoUser)Roastery.getHibernateApi().getUniqueEntityByDomainQuery(dq);
					
					///////// Fin Busca Usuario //////
					if (usuarioActual != null)
					{
						if(usuarioActual.getArgouserCompanyBizUnit() != null)
						{
							factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFTerminal"), usuarioActual.getArgouserCompanyBizUnit().getBzuName());
																
						}
						
					}
		
		if ((tipoFact.getInvtypeId() == "ALMACENAJE_GUBERNAMENTAL" && factura.getInvoiceInvoiceItems() == null) ||  (tipoFact.getInvtypeId() == "ALMACENAJE_ESPECIAL" && factura.getInvoiceInvoiceItems() == null) ||  (tipoFact.getInvtypeId() == "ALMACENAJE_FINAL" && factura.getInvoiceInvoiceItems() == null) || (tipoFact.getInvtypeId() == "ALMACENAJE_FISCAL" && factura.getInvoiceInvoiceItems() == null) )		
		//if (tipoFact.getInvtypeId() == "ALMACENAJE_GUBERNAMENTAL" ||  tipoFact.getInvtypeId() == "ALMACENAJE_ESPECIAL" ||  tipoFact.getInvtypeId() == "ALMACENAJE_FINAL" || tipoFact.getInvtypeId() == "ALMACENAJE_FISCAL" && factura.getInvoiceInvoiceItems() == null ) 
		//if (tipoFact.getInvtypeId() == "ALMACENAJE_FISCAL" && factura.getInvoiceInvoiceItems() == null ) 	
{
					int i = 0;
					Date inFecha = new SimpleDateFormat("yyyy-MMM-dd").parse("2016-MAR-02");
					
					TariffRate tarifaPrecio;
					Tariff tarifa ; 

					CurrencyExchangeRate moneda = CurrencyExchangeRate.findOrCreateExchangeRate(Currency.findCurrency("PESOS"),Currency.findCurrency("USD"), inFecha,0);
					chagbl = new ChargeableUnitEvent();
					
					Double inAmount = 0;
					Double inQuantity = 0;
					//InvoiceItem item;
					
					String BLs = factura.getField(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFBLS")).toString();
					String cliente = factura.getInvoicePayeeCustomer().getCustTaxId();

					String unidades = "";
					/*
					DefaultEUIExtensionHelper extHelper = new DefaultEUIExtensionHelper();
					String dialogTitle = "Testing table view command extension";
					extHelper.showMessageDialog(MessageLevel.INFO, dialogTitle, cliente);
					return
					*/
					
					///////// Bloque de Validacion del formato de BLs especificado por el usuario /////////
					mensajeValidacion = Validador(TipoValidacion.FORMATO_BLs,BLs);
					if (mensajeValidacion != null)
					{
						throw new BizViolation(mensajeValidacion,null,null,null,null);
					}
					///////// Fin del bloque de validacion /////////
					
					///////// Bloque de Validacion BLs en blanco /////////
					mensajeValidacion = Validador(TipoValidacion.Bls_EN_BLANCO,BLs);
					if (mensajeValidacion != null)
					{
						throw new BizViolation(mensajeValidacion,null,null,null,null);
					}
					
					///////// Fin Bloque de Validacion BLs en blanco /////////

					///////// Busca Usuario //////
					
					//UserContext userContext = ContextHelper.getThreadUserContext();
					//DomainQuery dq = QueryUtils.createDomainQuery("ArgoUser").addDqPredicate(PredicateFactory.eq(SecurityField.BUSER_UID, userContext.getUserId()));
					//ArgoUser usuarioActual = (ArgoUser)Roastery.getHibernateApi().getUniqueEntityByDomainQuery(dq);
					
					///////// Fin Busca Usuario //////
					
					String[] listaBLs = BLs.split(",");
					HashSet items ;
					String primeraVisita = "";
					
					Date fechaReliq=null;
					Date hoy = new Date();
					
					if (factura.getField(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFFechaReliq")) != null)
					{
						fechaReliq = (Date)factura.getField(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFFechaReliq"));
						//fechaReliq.setTime(hoy.getTime());
					}
							
					
					/*
					DefaultEUIExtensionHelper extHelper = new DefaultEUIExtensionHelper();
					String dialogTitle = "Testing table view command extension";
					extHelper.showMessageDialog(MessageLevel.INFO, dialogTitle, fechaReliq.toString());
					return;
					*/
					
					
					for(int CantidadBL = 0;CantidadBL <= listaBLs.length-1;CantidadBL++)
					{

						NodeChildren valores = devuelveValorServicio(listaBLs[CantidadBL],cliente,fechaReliq);
						if (valores.size() > 0)
						{
							if (primeraVisita == "")
							{
								primeraVisita = valores[0].visita.toString();
							}
							
						}
						
						unidades+= valores[CantidadBL].Unidades.toString();
						
						for(i = 0;i<= valores.size() -1;i++)
						{
							mensajeValidacion = Validador(TipoValidacion.BL_NO_EXISTE,valores[i].Codigo.toString(),valores[i].descripcion.toString() + "-"+listaBLs[CantidadBL]);
							if (mensajeValidacion != null)
							{
								throw new BizViolation(mensajeValidacion,null,null,null,null);
							}

							// Valida que todos los BLs pertenezcan a la misma visita
							mensajeValidacion = Validador(TipoValidacion.BL_VISITA_DIFERENTE,primeraVisita,valores[i].visita.toString());
							if (mensajeValidacion != null)
							{
								throw new BizViolation(mensajeValidacion,null,null,null,null);
							}
							
							
							///////// Captura los valores desde el WS y crea los valores para insertar el Invoice Item /////////

							factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFVisita"), valores[i].visita.toString());
							
									
							factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFBuque"), valores[i].buque.toString());
							factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFLinea"), valores[i].linea.toString());
							factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFSemana"), Long.parseLong(valores[i].semana.toString()));
							if (usuarioActual != null)
							{
								if(usuarioActual.getArgouserCompanyBizUnit() != null)
								{
									factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFTerminal"), usuarioActual.getArgouserCompanyBizUnit().getBzuName());
																		
								}
								
							}
							
							
							if (valores[i].Unidades.toString() == null || valores[i].Unidades.toString()=="")
							{
								factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFU"), "No hay unidades");
								factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFAnexoS_N"), "NO");
							}
							else
							{
								factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFU"),unidades);
								if (unidades.length() > 367)
									factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFAnexoS_N"), "SI");
								else
									factura.setFieldValue(MetafieldIdFactory.valueOf("customFlexFields.invoiceCustomDFFAnexoS_N"), "NO");

							}
							
							
							inQuantity = Double.parseDouble(valores[i].cantidad.toString());
							
							tarifa  = Tariff.findTariff("STORAGE");
							tarifa.setFieldValue(BillingField.TARIFF_DESCRIPTION, valores[i].descripcion.toString());
							tarifaPrecio = TariffRate.findTariffRate(Contract.findContract("BASE"), tarifa, inFecha);
							tarifaPrecio.setFieldValue(BillingField.RATE_AMOUNT, Double.parseDouble(valores[i].tarifa.toString()));
							chagbl.setBexuQuantity(inQuantity);
							chagbl.setBexuCargoQuantity(inQuantity);
							chagbl.setBexuEventType(valores[i].Codigo.toString());
							chagbl.setBexuEqId(listaBLs[CantidadBL]);
							chagbl.setBexuEventStartTime(new SimpleDateFormat("yyyy-MM-dd").parse(valores[i].FechaLlegada.toString().substring(0,10)));
							inAmount = inQuantity * Double.parseDouble(valores[i].tarifa.toString());
							
							///////// FIN - Captura los valores desde el WS y crea los valores para insertar el Invoice Item /////////
							
							//mensajeValidacion = "Valor de la tarifa" + tarifa.toString(); 
							//throw new BizViolation(mensajeValidacion,null,null,null,null);
							
							item = InvoiceItem.createInvoiceItem(factura, tarifaPrecio, chagbl, moneda, inAmount, new SimpleDateFormat("yyyy-MM-dd").parse(valores[i].ValidoHasta.toString().substring(0,10)), inFecha);

							
						if ( items  == null)
						{
							items = new HashSet();
						}
						items.add(item);
						
						}
					}

					factura.setInvoiceInvoiceItems (items);

				}
				
					
			}
			catch(BizViolation inBizViolation)
				{
				
				 	MessageCollector messageCollector = ContextHelper.getThreadMessageCollector();
					 if (messageCollector != null) {
					        //messageCollector.appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf(inBizViolation.getMessage()), null, inBizViolation.getParms());
						messageCollector.appendMessage(MessageLevel.SEVERE, PropertyKeyFactory.valueOf(inBizViolation.getMessage()), null, null);
					      }	  
				}
	}
  
	
	private PropertyKey Validador(TipoValidacion valida,String valorValidar,String valorValidar2="Valor parametro 2")
	{
		
		switch(valida)
		{
			case valida.Bls_EN_BLANCO:
				if (valorValidar.isEmpty() || valorValidar == null)
				{
					PropertyKey CUSTOM_ERROR_BLANCO = PropertyKeyFactory.valueOf("Debe especificar un valor en el campop BLs.");
					return CUSTOM_ERROR_BLANCO;
					
				}
				break;
		
			case valida.FORMATO_BLs:
				def patron = /^([0-9A-Za-z -]+)(,[0-9A-Za-z -]+)*$/;
				Matcher checkPatt =  (valorValidar =~ patron);
				if (!checkPatt)
				{
					PropertyKey CUSTOM_ERROR_PATRON = PropertyKeyFactory.valueOf("El formato especificado de BLs es incorrecto. Verifique");
					return CUSTOM_ERROR_PATRON;
				}
				break;
				
			case valida.BL_NO_EXISTE:
				if (valorValidar == "ERROR")
				{
					PropertyKey CUSTOM_ERROR_BL = PropertyKeyFactory.valueOf(valorValidar2);
					throw new BizViolation(CUSTOM_ERROR_BL,null,null,null,null);
				
				}
				break;

			case valida.BL_VISITA_DIFERENTE:
				if (valorValidar != valorValidar2)
				{
					PropertyKey CUSTOM_ERROR_VISITA_DIFERENTE = PropertyKeyFactory.valueOf("La visita -> " + valorValidar + " no es la misma para los BLs indicados");
					throw new BizViolation(CUSTOM_ERROR_VISITA_DIFERENTE,null,null,null,null);
					
				}
				break;
				
		}
		
	}
	

	private NodeChildren devuelveValorServicio(String BLNo,String cliente,def fechaReliquidacion)
	{
		
			def fecha = new Date();
			String fechaFormateada = fecha.format("yyyy-MM-dd'T'HH:mm:ss.SSS") ;
			String fechaReqFormateada;
			
			if (fechaReliquidacion != null)
			{
				fechaReqFormateada = fechaReliquidacion.format("yyyy-MM-dd'T'HH:mm:ss.SSS") ;
			}
			else
			{
				fechaReqFormateada="";
			}
			
			/*
			DefaultEUIExtensionHelper extHelper = new DefaultEUIExtensionHelper();
			String dialogTitle = "Testing table view command extension";
			extHelper.showMessageDialog(MessageLevel.INFO, dialogTitle, fechaReqFormateada);
			return;
			*/
				
				
				def client = new SOAPClient("http://hit-app02:52750/wsextrahit.asmx")
				def response = client.send( """<?xml version="1.0" encoding="utf-8"?>
										<soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
											  <soap:Body>
												    <GetCalculoALM   xmlns="http://tempuri.org/">
													      <Bl>$BLNo</Bl>
													      <TipoMoneda>1</TipoMoneda>
													      <Rnc>$cliente</Rnc>
													      <Fecha>$fechaFormateada</Fecha>
													      <fecha_reliquidacion>$fechaReqFormateada</fecha_reliquidacion>
												    </GetCalculoALM >
											  </soap:Body>
										</soap:Envelope>""")
				
				
				def result = response.GetCalculoALMResponse.GetCalculoALMResult._getcalculoalm;
	
				return result;
	
		  }

 }

