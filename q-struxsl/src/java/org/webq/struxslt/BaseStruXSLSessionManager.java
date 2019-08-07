package org.webq.struxslt;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.webq.struxslt.mapper.xslt.XsltMap;
import org.webq.utils.CollectionUtils;
import org.webq.utils.StringUtils;
import org.webq.utils.XMLUtils;

/**
 * Manage session information for the actions
 * creation_date: 25.01.2008
 * 
 * @author albert
 */
public class BaseStruXSLSessionManager
{
	//session parameter keys
	private static final String KEY_LANG = "lang";
	private static final String KEY_ADMINLOGGEDIN = "admin_logged_in";
	private static final String KEY_DEBUG = "debug";
	private static final String KEY_LOCALE = "locale";

	//view keys
	private static final String KEY_STYLE = "STYLE";
	private static final String KEY_STYLEFIRSTCHOICE = "STYLE_FIRSTCHOICE";
	private static final String KEY_CURR_XSLT_PATH = "CURR_XSLT_PATH";
	private static final String KEY_XSLTPATHS = "xsltpaths";
	private static final String KEY_XSLTMAP = "xsltmap";
	private static final String KEY_AJAX = "ajax";

	protected HttpServletRequest request;

	public BaseStruXSLSessionManager(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * Set if the current request an ajax-request	
	 * 
	 * @param ajax
	 */
	public void setAjaxRequest(boolean ajax)
	{
		setSessionVar(KEY_AJAX, ajax);
	}

	/**
	 * REMARK: this parameter is stored until deleted, so this setting can survive forwards
	 * 
	 * @return true if this is an ajax request 
	 */
	public boolean isAjaxRequest()
	{
		return getSessionVarBoolean(KEY_AJAX);
	}

	/**
	 * Set a flag that the admin is currently logged in, which could cause different behaviour on the view and within the actions 
	 * depending on the implementation of 
	 * 	
	 * @param loggedIn is admin should be set as logged in or not?
	 */
	public void setAdminLoggedIn(boolean loggedIn)
	{
		setSessionVar(KEY_ADMINLOGGEDIN, new Boolean(loggedIn).toString());
	}

	/**
	 * Retrieve objects stored within the current session
	 * 
	 * @param request the Servlet request
	 * @param key the key which identifies a object stored within the current session
	 * @return the object stored within the session
	 */
	public Object getSessionVar(String key)
	{
		HttpSession sess = request.getSession();
		return sess.getAttribute(key);
	}

	/**
	 * @see #getSessionVar(String)
	 * @return true, when the value can be successfully parsed to a boolean-true-value. 
	 * If the session var is not set, return false
	 */
	public boolean getSessionVarBoolean(String key)
	{
		Object o = getSessionVar(key);
		if (o instanceof Boolean)
		{
			return (Boolean) o;
		}
		String str = (String) o;
		if (!StringUtils.isSet(str))
		{
			return false;
		}
		return new Boolean(str).booleanValue();
	}

	public int getSessionVarInt(String key)
	{
		return getSessionVarInt(key, Integer.MIN_VALUE);
	}

	public int getSessionVarInt(String key, int def)
	{
		Object o = getSessionVar(key);
		if (o != null)
		{
			return (Integer) o;
		}
		return def;
	}

	/**
	 * Store objects within the current session
	 * 
	 * @param request the Servlet request
	 * @param key key the key which identifies a object stored within the current session
	 * @param val the object which should be stored within the session
	 */
	public void setSessionVar(String key, Object val)
	{
		HttpSession sess = request.getSession();
		sess.setAttribute(key, val);
	}

	/**
	 * Check if the session variable SESS_KEY_ADMINLOGGEDIN is set
	 * @return true, if the system administrator is correctly logged in
	 */
	public boolean isAdminLoggedIn()
	{
		return this.getSessionVarBoolean(KEY_ADMINLOGGEDIN);
	}

	/**
	 * Adds an message code to this request
	 * 
	 * @param request the Servlet request to store the messages within the session
	 * @param code the message code to be stored
	 */
	public void addMessageCode(String code)
	{
		Vector<String> msgKeys = getMessageCodes();
		if (msgKeys == null)
		{
			msgKeys = new Vector<String>();
		}
		if (!msgKeys.contains(code))
		{
			msgKeys.add(code);
		}
		this.setSessionVar(GlobalKeys.SESS_MESSAGECODES, msgKeys);
	}

	/**
	 * Adds an ajax code to this request
	 * 
	 * @param request the Servlet request to store the messages within the session
	 * @param code the message code to be stored
	 */
	public void addAjaxCode(String code)
	{
		Vector<String> msgKeys = getAjaxCodes();
		if (msgKeys == null)
		{
			msgKeys = new Vector<String>();
		}
		if (!msgKeys.contains(code))
		{
			msgKeys.add(code);
		}
		this.setSessionVar(GlobalKeys.SESS_AJAXCODES, msgKeys);
	}

	/**
	 * Returns the message codes which are stored within the session
	 * 
	 * @param request the Servlet request to store the messages within the session
	 * @return a list of message codes
	 */
	public Vector<String> getMessageCodes()
	{
		return (Vector<String>) this.getSessionVar(GlobalKeys.SESS_MESSAGECODES);
	}

	public Vector<String> getAjaxCodes()
	{
		return (Vector<String>) this.getSessionVar(GlobalKeys.SESS_AJAXCODES);
	}

	/**
	 * @return the current language as set within the session
	 */
	public String getCurrentLang()
	{
		String lang = (String) this.getSessionVar(KEY_LANG);
		if (lang == null)
			lang = request.getLocale().getLanguage();
		if (lang != null) lang = lang.toUpperCase();
		return lang;
	}

	/**
	 * Write the currently used language to session
	 * 
	 * @param lang the language to be set
	 */
	public void setCurrentLang(String lang)
	{
		this.setSessionVar(KEY_LANG, lang.toUpperCase());
		setCurrentLocale(new Locale(lang));
	}

	// TODO: getcurrentLang should be eliminated, and only locale should be supported!
	//       Locale contains more info than the language string.
	public Locale getCurrentLocale()
	{
		Object locale = this.getSessionVar(KEY_LOCALE);
		if (locale != null)
			return (Locale) locale;
		return new Locale(getCurrentLang());
	}

	public void setCurrentLocale(Locale locale)
	{
		this.setSessionVar(KEY_LOCALE, locale);
	}

	/**
	 * Empty the message codes container
	 * 
	 * @param request the Servlet request to store the messages within the session
	 */
	public void clearMessageCodes()
	{
		Vector<String> msgKeys = new Vector<String>();
		this.setSessionVar(GlobalKeys.SESS_MESSAGECODES, msgKeys);
	}

	/**
	 * Empty the ajax codes container
	 * 
	 * @param request the Servlet request to store the messages within the session
	 */
	public void clearAjaxCodes()
	{
		Vector<String> msgKeys = new Vector<String>();
		this.setSessionVar(GlobalKeys.SESS_AJAXCODES, msgKeys);
	}

	/**
	 * @return true if the debug flag is set
	 */
	public boolean isDebug()
	{
		Object o = getSessionVar(KEY_DEBUG);
		if (o == null)
		{
			return false;
		}
		return (Boolean) o;
	}

	/**
	 * Turn debug mode on or off
	 * 
	 * @param request
	 * @param debug
	 */
	public void setDebug(boolean debug)
	{
		setSessionVar(KEY_DEBUG, debug);
	}

	/**
	 * Adds an error code to this request
	 * 
	 * @param request the Servlet request to store the errors within the session
	 * @param code the error code to be stored
	 */
	public void AddErrorCode(String code)
	{
		Vector<String> errKeys = (Vector<String>) this.getSessionVar(GlobalKeys.SESS_ERRORCODES);
		if (errKeys == null)
		{
			errKeys = new Vector<String>();
		}
		if (!errKeys.contains(code))
		{
			errKeys.add(code);
		}
		this.setSessionVar(GlobalKeys.SESS_ERRORCODES, errKeys);
	}

	public void removeErrorCodes()
	{
		this.request.getSession().removeAttribute(GlobalKeys.SESS_ERRORCODES);
	}

	/**
	 * @param request the Servlet request to store the errors within the session
	 * @return the error codes which are stored within the session
	 */
	public Vector<String> getErrorCodes()
	{
		return (Vector<String>) this.getSessionVar(GlobalKeys.SESS_ERRORCODES);
	}

	/**
	 * @return true if any error codes are set
	 */
	public boolean hasErrorCodes()
	{
		Vector<String> codes = getErrorCodes();
		return !CollectionUtils.isEmpty(codes);
	}

	/**
	 * @return true if any ajax codes are set
	 */
	public boolean hasAjaxCodes()
	{
		Vector<String> codes = getAjaxCodes();
		return !CollectionUtils.isEmpty(codes);
	}

	/**
	 * Empty the error codes container
	 * 
	 * @param request the servlet request to store the errors within the session
	 */
	public void clearErrorCodes()
	{
		Vector<String> errKeys = new Vector<String>();
		this.setSessionVar(GlobalKeys.SESS_ERRORCODES, errKeys);
	}

	/**	
	 * @return the currently used style
	 */
	public String getStyle()
	{
		return (String) this.getSessionVar(KEY_STYLE);
	}

	/**
	 * Set the currently used style	
	 * @param style the style key to be set
	 */
	public void setStyle(String style)
	{
		this.setSessionVar(KEY_STYLE, style);
	}

	/**
	 * Style selection can be done following different strategies. Set to true, if the selected style was first choice selection
	 * 	
	 * @param fistChoice
	 */
	public void setStyleFirstChoice(boolean fistChoice)
	{
		this.setSessionVar(KEY_STYLEFIRSTCHOICE, fistChoice);
	}

	/**	
	 * @return true if the selection of the current style was a first choice selection
	 */
	public boolean getStyleFirstChoice()
	{
		return (Boolean) this.getSessionVar(KEY_STYLEFIRSTCHOICE);
	}

	/**
	 * Just use for debugging info
	 * 
	 */
	public void setCurrXsltPath(String xsltPath)
	{
		this.setSessionVar(KEY_CURR_XSLT_PATH, xsltPath);
	}

	/**
	 * Just use for debugging info
	 * 
	 * @param request
	 * @return
	 */
	public String getCurrXsltPath()
	{
		return (String) this.getSessionVar(KEY_CURR_XSLT_PATH);
	}

	public void setXsltPaths(Hashtable<XsltBaseKey, String> iEntries)
	{
		this.setSessionVar(KEY_XSLTPATHS, iEntries);
	}

	public Hashtable<XsltBaseKey, String> getOrCreateXsltPathTable()
	{
		Object o = this.getSessionVar(KEY_XSLTPATHS);
		if (o != null)
		{
			return (Hashtable<XsltBaseKey, String>) o;
		}
		Hashtable<XsltBaseKey, String> entries = new Hashtable<XsltBaseKey, String>();
		this.setSessionVar(KEY_XSLTPATHS, entries);
		return entries;
	}

	public XsltMap getXsltMap()
	{
		return (XsltMap) getSessionVar(KEY_XSLTMAP);
	}

	public void setXsltMap(XsltMap map)
	{
		setSessionVar(KEY_XSLTMAP, map);
	}

	public String getSessId()
	{
		return request.getSession().getId();
	}

	public Element toXML(Document doc)
	{
		HttpSession sess = request.getSession();
		Element rootEl = doc.createElement("session_vars");
		rootEl.setAttribute("sess_id", sess.getId());
		Enumeration<String> attNames = sess.getAttributeNames();
		while (attNames.hasMoreElements())
		{
			String key = attNames.nextElement();
			Element itemEl = doc.createElement("attribute");
			itemEl.setAttribute("key", key);
			XMLUtils.addWithTextNode(itemEl, "value", sess.getAttribute(key));
			rootEl.appendChild(itemEl);
		}
		return rootEl;
	}
}
