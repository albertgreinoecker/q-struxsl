package org.webq.struxslt;

import java.util.Properties;

import org.webq.utils.Encodings;
import org.webq.utils.PropertyLoader;

/**
 * Reads settings from the struxsl-specific properties file
 * Currently, only the encoding to be used for the framework can be set
 * 
 * creation_date: 08.01.2009
 * @author albert
 */
public class StruXSLConf
{
	private static final String CONF_FILE = "struxsl";
	private static final String DEFAULT_ENCODING = Encodings.UTF_8;
	static String encoding;
	static
	{
		try
		{
			Properties props = PropertyLoader.loadProperties(CONF_FILE);
			encoding = (String) props.get("encoding");
		} catch (Exception e)
		{
			encoding = DEFAULT_ENCODING;
		}
	}

	public static String getEncoding()
	{
		return encoding;
	}
}