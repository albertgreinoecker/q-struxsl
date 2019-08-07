package org.webq.struxslt.mapper.xslt;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.webq.struxslt.StruXSLConf;
import org.webq.utils.StringUtils;
import org.webq.utils.SystemUtils;
import org.webq.utils.XMLNodeListIterator;
import org.webq.utils.XMLUtils;

/**
 * Mapping from action to a XSLT-Document is implemented here Also language XML-documents are assigned to an Action-path and global language filepathes are
 * loaded/stored here
 * creation_date: 04.08.2006
 * TODO: currently, only reading of the XML document is implemented, so also implement writing
 * 
 * @author albert
 */
public class XsltMap
{
	private static Logger logger = Logger.getLogger("org.webq.struxslt.mapper.xslt.XsltMap");
	
	private static final String DEBUG_ATTR = "debug";
	private static final String MAP_ENTRY_EL = "entry";
	private static final String GLOBAL_LANGS_ROOT_EL = "global_langs";
	private static final String LANGFILE_ENTRY_ATT = "lang_file";
	private static final String SUPPORTEDLANGS = "supportedLangs";
	private static final String MULTIPLE_LANGS_ATTR = "langs";
	private static final String MESSAGE_CODES_EL = "messageCodes";

	private Vector<XsltMapEntry> xsltEntries;
	private Vector<String> globalLangs = null;
	private String[] supportedLangs;
	private String messageCodesFile;
	private boolean debug;

	/**
	 * For each language, a folder named as the language to be supported, has to be located within WEB-INF/lang with all language XMLs in it
	 * 
	 * @return a List of languages which are actually supported by the system
	 */
	public String[] GetSupportedLangs()
	{
		return supportedLangs;
	}

	/**
	 * @return the document which holds all language dependent translations from error/message code to the error/message description
	 */
	public String getMessageCodesFile()
	{
		return messageCodesFile;
	}

	/**
	 * Read the XSLT <==> action map from a configuration file
	 * 
	 * @param confFile the path to the (xml) configuration file
	 */
	public XsltMap(String confFile)
	{
		xsltEntries = new Vector<XsltMapEntry>();
		try
		{
			Document doc = XMLUtils.getDocumentFromFile(confFile, StruXSLConf.getEncoding());
			Element root = doc.getDocumentElement();
			debug = XMLUtils.getAttrAsBoolean(root, DEBUG_ATTR);

			// read the supported languages
			Element supportedLangsEl = (Element) root.getElementsByTagName(SUPPORTEDLANGS).item(0);
			supportedLangs = StringUtils.trim(supportedLangsEl.getAttribute(MULTIPLE_LANGS_ATTR).split(","));

			// load message/error codes xml file
			try
			{
				Element msgCodesEl = (Element) root.getElementsByTagName(MESSAGE_CODES_EL).item(0);
				messageCodesFile = msgCodesEl.getAttribute(LANGFILE_ENTRY_ATT);
			} catch (Exception e) 
			{
				 logger.log(Priority.WARN, "Cannot Load Message Codes File");
			}
			NodeList subEntries = root.getElementsByTagName(MAP_ENTRY_EL);

			for (int i = 0; i < subEntries.getLength(); i++)
			{
				Element subEntry = (Element) subEntries.item(i);
				XsltMapEntry entry = XsltMapFactory.getEntry(subEntry);
				xsltEntries.add(entry);
			}
			loadGlobalLangs(doc);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Loads the global language files (if not already loaded)
	 * 
	 * @param confFile the path to the (xml) configuration file
	 */
	private void loadGlobalLangs(Document doc)
	{
		if (globalLangs == null)
		{
			globalLangs = new Vector<String>();
			Element globLangRootEl = XMLUtils.getSingleElement(doc, GLOBAL_LANGS_ROOT_EL);
			NodeList globLangNl = globLangRootEl.getElementsByTagName("*");
			XMLNodeListIterator nlIt = new XMLNodeListIterator(globLangNl);
			while (nlIt.hasNext())
			{
				Element currEl = nlIt.next();
				String path = currEl.getAttribute(LANGFILE_ENTRY_ATT);
				globalLangs.add(path);
			}
		}
	}

	/**
	 * @return the relative paths of the global language files
	 */
	public Vector<String> getGlobalLangs()
	{
		return globalLangs;
	}

	/**
	 * Get the right entry for this parameters list
	 * 
	 * @param path A Struts-Action path
	 * @param group the group for which the entry should be found
	 * @param quest the questionnaire for which the entry should be found
	 * @return the entry best suitable for the parameters. Null if no suitable parameter could be found
	 */
	public XsltMapEntry getEntry(String path, String key1, String key2)
	{
		for (XsltMapEntry entry : xsltEntries)
		{
			if (entry.isMe(path, key1, key2))
			{
				return entry;
			}
		}
		return null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (xsltEntries == null)
			return "NULL";
		String res = "";
		for (XsltMapEntry entry : xsltEntries)
		{
			res = entry.toString() + SystemUtils.newLine();
		}
		return res;
	}

	public boolean isDebug()
	{
		return debug;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

}
