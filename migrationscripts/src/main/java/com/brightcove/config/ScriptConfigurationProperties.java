package com.brightcove.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class ScriptConfigurationProperties {

	static final Logger logger = Logger.getLogger(ScriptConfigurationProperties.class);

	public ScriptConfigurationProperties() {
		/*
		 * Empty constructor
		 */
	}

	public Properties getPropValues() throws IOException {
		Properties prop = new Properties();

		String propertyFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertyFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propertyFileName + "' not found in the classpath");
		}

		return prop;
	}

}
