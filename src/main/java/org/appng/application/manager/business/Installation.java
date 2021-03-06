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

import java.util.ArrayList;

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
import org.appng.application.manager.service.Service;
import org.appng.application.manager.service.ServiceAware;
import org.appng.core.model.InstallablePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Matthias Herlitzius
 * 
 */

@Lazy
@Component
@Scope("request")
public class Installation extends ServiceAware implements DataProvider, ActionProvider<Void> {

	private static final Logger log = LoggerFactory.getLogger(Installation.class);

	public static final String REPOSITORY = "repository";
	private static final String PACKAGE_OPTION = "package";
	private static final String PACKAGE_NAME = "packageName";
	private static final String PACKAGE_VERSION = "packageVersion";
	private static final String PACKAGE_TIMESTAMP = "packageTimestamp";

	public void perform(Site site, Application application, Environment environment, Options options, Request request,
			Void valueHolder, FieldProcessor fp) {
		String action = getAction(options);
		String errorMessage = null;
		String okMessage = null;
		Service service = getService();
		Integer repositoryId = request.convert(options.getOptionValue(REPOSITORY, ID), Integer.class);
		String packageName = request.convert(options.getOptionValue(PACKAGE_OPTION, PACKAGE_NAME), String.class);
		String packageVersion = request.convert(options.getOptionValue(PACKAGE_OPTION, PACKAGE_VERSION), String.class);
		String packageTimestamp = request.convert(options.getOptionValue(PACKAGE_OPTION, PACKAGE_TIMESTAMP),
				String.class);

		try {
			if (ACTION_INSTALL.equals(action)) {
				errorMessage = MessageConstants.PACKAGE_INSTALL_ERROR;
				service.installPackage(repositoryId, packageName, packageVersion, packageTimestamp, fp);
				okMessage = MessageConstants.PACKAGE_INSTALLED;
				String reloadMessage = request.getMessage(MessageConstants.RELOAD_PLATFORM);
				fp.addNoticeMessage(reloadMessage);
			} else if (ACTION_DELETE_PACKAGE.equals(action)) {
				errorMessage = MessageConstants.PACKAGE_DELETE_ERROR;
				service.deletePackageVersion(repositoryId, packageName, packageVersion, packageTimestamp, fp);
				okMessage = MessageConstants.PACKAGE_DELETED;
			}
			String message = request.getMessage(okMessage, repositoryId, packageName, packageVersion);
			fp.addOkMessage(message);
		} catch (BusinessException ex) {
			String message = request.getMessage(errorMessage, repositoryId, packageName, packageVersion);
			log.error(message, ex);
			fp.addErrorMessage(message);
		}
	}

	public DataContainer getData(Site site, Application application, Environment environment, Options options,
			Request request, FieldProcessor fp) {
		Service service = getService();
		Integer repositoryId = request.convert(options.getOptionValue(REPOSITORY, ID), Integer.class);
		String applicationName = request.convert(options.getOptionValue(PACKAGE_OPTION, PACKAGE_NAME), String.class);
		DataContainer data = new DataContainer(fp);
		try {
			if (null != repositoryId && null == applicationName) {
				data = service.searchInstallablePackages(fp, repositoryId);
			} else if (null != repositoryId && null != applicationName) {
				data = service.searchPackageVersions(fp, repositoryId, applicationName);
			}
		} catch (BusinessException ex) {
			data.setPage(new ArrayList<InstallablePackage>(), fp.getPageable());
		}
		return data;
	}
}
