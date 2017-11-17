package com.hit.groovys

import com.navis.argo.ContextHelper;
import com.navis.framework.email.EmailMessage;
import com.navis.framework.email.EmailManager;
import com.navis.framework.business.Roastery;
import com.navis.framework.email.EmailUtils;

class HITGvyEnviaEmail {

	
	public String execute() {
		
		def emailAccount ="jose.encarnacion@hit.com.do";
		
		EmailMessage msg = new EmailMessage(ContextHelper.getThreadUserContext());
		msg.setTo("jose.encarnacion@hit.com.do");
		//msg.setCc(copia);
		msg.setFrom("jose.encarnacion@hit.com.do");
		
		msg.setSubject("prueba envio correo");
		msg.setText("funciona");
		
		EmailManager manager = (EmailManager) Roastery.getBean("emailManager");
		manager.sendEmail(msg);
		
		
		return "Hello World!"
	}
	
	
}
