package com.hit.groovys;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.navis.external.framework.ui.AbstractTableViewCommand;
import com.navis.external.framework.ui.EUIExtensionHelper;
import com.navis.framework.metafields.entity.EntityId;
import com.navis.framework.portal.BizRequest;
import com.navis.framework.portal.CrudDelegate;
import com.navis.framework.portal.UserContext;
import com.navis.framework.portal.context.UserContextUtils;
import com.navis.framework.presentation.util.FrameworkUserActions;
import com.navis.framework.ulc.server.context.UlcRequestContextFactory;
import com.navis.framework.util.message.MessageLevel;


public class HITGvyExamples extends AbstractTableViewCommand{

	@Override
	public void execute(EntityId entityId, List<Serializable> gkeys, Map<String, Object> params) {
		super.execute(entityId, gkeys, params);
		
		UserContext user = UserContextUtils.getSystemUserContext();
		
		log("gkeys=$gkey");
		
		if(gkeys == null)
			return;
		
		BizRequest request = new BizRequest(user);
		
		log("request=$request");
		
	}

	
	
	
}
