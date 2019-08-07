package org.webq.struxslt.mapper.xslt;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.webq.utils.CollectionUtils;
import org.webq.utils.XMLAble;
import org.webq.utils.XMLUtils;

/**
 * The base class for all XSLT-map-entries
 * creation_date: 04.08.2006
 * 
 * @author albert
 */
public abstract class XsltMapEntry extends XMLAble
{
	private static final String MIME_TYPE = "mime_type";
	private static final String LANG_FILE = "lang_file";
	private static final String LANG_DEFAULT = "lang_default";

	private static final String DEFAULT_LANGUAGE = "EN";

	private static final String KEY_PATH = "path";

	private static final String DEFAULT_STYLE = "default";

	private static Logger logger = Logger.getLogger("org.qsys.quest.action.view.mapper.xslt.XsltMapEntry");

	String mimeType;
	String lang;
	String[] langFiles;
	protected boolean firstChoice; //is the assignment of a (sub-) stylesheet first choice or not
	protected String path;

	/**
	 * Read entry from element here
	 * @param el the element holding the XSLT-map information
	 */
	public XsltMapEntry(Element el)
	{
		firstChoice = true;
		path = el.getAttribute(KEY_PATH);

		mimeType = "text/html"; //default mimetype is HTML
		if (el.hasAttribute(MIME_TYPE))
		{
			mimeType = el.getAttribute(MIME_TYPE);
		}

		lang = DEFAULT_LANGUAGE;
		if (el.hasAttribute(LANG_DEFAULT))
		{
			lang = el.getAttribute(LANG_DEFAULT);
		}

		langFiles = XMLUtils.getAttrAsStrArr(el, LANG_FILE);
	}

	/**
	 * The algorithm how the XSLT-path should be chosen is implemented in the subclasses	
	 * @return the path to the concrete stylesheet
	 */
	public abstract String getXsltPath();

	public abstract String getXsltPath(Vector<String> conds);

	/**
	 * If a certain stylesheet path should be selected directly. For MapEntry, where na name can be set for subitems, the parameterless version is used (which is default)
	 * @param name the name of the path which should be looked up directly
	 * @see #getXsltPath()
	 * @throws XsltMapperException when the XSLT-entry-name which should be looked up does not exist
	 */
	public String getXsltPath(String name) throws XsltMapperException
	{
		logger.log(Level.INFO, "name " + name + " not used for for xslt-path retrieving. Default value is returned");
		return getXsltPath();
	}

	/**
	 * Per default, only the default style is returned (e.g. if no names are available for certain styles)
	 * @return all styles which are available for a certain XsltMap-Entry
	 */
	public List<String> getAvailableStyles()
	{
		return CollectionUtils.asList(DEFAULT_STYLE);
	}

	/**
	 * Used to browse through all XsltMapEntryies and each entry type should decide if it is suitable for the request
	 * 	
	 * @param path the path to the action (with / in front)
	 * @param group the name of the group
	 * @param questionnaire the questionnaire
	 * @return true, if this entry is suitable
	 */
	public abstract boolean isMe(String path, String group, String questionnaire);

	public String getMimeType()
	{
		return mimeType;
	}

	public String getLang()
	{
		return lang;
	}

	public String[] getLangFiles()
	{
		return langFiles;
	}

	public String getName()
	{
		return path;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return mimeType + "| " + lang + "| " + CollectionUtils.toString(langFiles) + "| " + path;
	}

	public boolean isFirstChoice()
	{
		return firstChoice;
	}
}
