/*
 * Copyright 2011-2023 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.appng.api.BusinessException;
import org.appng.api.Environment;
import org.appng.api.InvalidConfigurationException;
import org.appng.api.messaging.Event;
import org.appng.api.model.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;


public class LogConfigChangedEvent extends Event implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogConfigChangedEvent.class);

	private static final long serialVersionUID = 1L;
	private final String content;
	private final String configFilePath;

	public LogConfigChangedEvent(String siteName, String content, String configFilePath) {
		super(siteName);
		this.content = content;
		this.configFilePath = configFilePath;
	}

	public void perform(Environment environment, Site site) throws InvalidConfigurationException, BusinessException {
		try {
			File configFile = new File(configFilePath).getAbsoluteFile();
			FileUtils.write(configFile, content, StandardCharsets.UTF_8);
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			loggerContext.reset();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			configurator.doConfigure(configFile);
			LOGGER.info("Updated {}", configFile.getPath());
		} catch (IOException | JoranException e) {
			throw new BusinessException("error while executing LogConfigChangedEvent", e);
		}
	}

}
