/*
 * Copyright 2011-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.appng.application.manager.business;

import org.appng.api.ActionProvider;
import org.appng.api.BusinessException;
import org.appng.api.DataContainer;
import org.appng.api.DataProvider;
import org.appng.api.Environment;
import org.appng.api.FieldProcessor;
import org.appng.api.Options;
import org.appng.api.Request;
import org.appng.api.model.Application;
import org.appng.api.model.Site;
import org.appng.application.manager.MessageConstants;
import org.appng.application.manager.form.RoleForm;
import org.appng.application.manager.service.Service;
import org.appng.application.manager.service.ServiceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Provides CRUD-operations for a {@link org.appng.core.domain.RoleImpl}.
 * 
 * @author Matthias Müller
 * 
 */

@Lazy
@Component
@Scope("request")
public class Roles extends ServiceAware implements DataProvider, ActionProvider<RoleForm> {
	private static final Logger log = LoggerFactory.getLogger(Roles.class);

	public void perform(Site site, Application application, Environment environment, Options options, Request request,
			RoleForm roleForm, FieldProcessor fp) {
		String action = getAction(options);

		String okMessage = null;
		Service service = getService();
		Integer applicationroleId = request.convert(options.getOptionValue(ID, ID), Integer.class);

		try {
			if (ACTION_CREATE.equals(action)) {
				Integer applicationId = request.convert(options.getOptionValue(APPLICATION, ID), Integer.class);
				service.createRole(roleForm, applicationId, fp);
				okMessage = MessageConstants.ROLE_CREATED;
			} else if (ACTION_UPDATE.equals(action)) {
				Integer roleId = request.convert(options.getOptionValue(ID, ID), Integer.class);
				roleForm.getRole().setId(roleId);
				service.updateRole(roleForm, fp);
				okMessage = MessageConstants.ROLE_UPDATED;
			} else if (ACTION_DELETE.equals(action)) {
				service.deleteRole(applicationroleId);
				okMessage = MessageConstants.ROLE_DELETED;
			}
			String message = request.getMessage(okMessage, applicationroleId);
			fp.addOkMessage(message);
		} catch (BusinessException e) {
			log.error("error while performing action", e);
			request.addErrorMessage(fp, e);
		}
	}

	public DataContainer getData(Site site, Application application, Environment environment, Options options,
			Request request, FieldProcessor fp) {
		Service service = getService();
		Integer applicationRoleId = request.convert(options.getOptionValue(ID, ID), Integer.class);
		Integer applicationId = request.convert(options.getOptionValue(APPLICATION, ID), Integer.class);
		DataContainer data = null;
		if (null == applicationRoleId && ACTION_CREATE.equals(getAction(options))) {
			data = service.getNewRole(fp, applicationId);
		} else {
			try {
				data = service.searchRole(fp, applicationRoleId, applicationId);
			} catch (BusinessException ex) {
				String message = request.getMessage(ex.getMessageKey(), ex.getMessageArgs());
				log.error(message, ex);
				fp.addErrorMessage(message);
			}
		}
		return data;
	}
}
