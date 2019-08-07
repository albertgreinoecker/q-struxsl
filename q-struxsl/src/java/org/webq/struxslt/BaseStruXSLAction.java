package org.webq.struxslt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;
import org.webq.struxslt.mapper.xslt.XmlLang;
import org.webq.struxslt.mapper.xslt.XsltMap;
import org.webq.utils.CollectionUtils;
import org.webq.utils.ErrorHandlingUtils;
import org.webq.utils.HttpUtils;
import org.webq.utils.StringUtils;
import org.webq.utils.struts.FormFileUtils;
import org.xml.sax.SAXException;

/**
 * The base class of the StruXSL framework. A General Action class with some handy functions and access to the session management 
 * 
 * creation_date: Aug 24,  2006
 * @author albert
 */
public abstract class BaseStruXSLAction extends Action
{
	protected Logger logger = Logger.getLogger("BaseStruXSLAction");

	
	private static final String KEY_LANG = "lang";
	private static final String XML_LANG_DIR = "/lang/"; // where are the language token files are stored

	// forwards for struts-xdoclet
	protected static final String FW_SUCCESS = "success";
	protected static final String FW_ERROR = "error";

	BaseStruXSLSessionManager sess; // here accessing and writing StruXSLT-session data is implemented

	protected String getCssPath(String cssFile)
	{
		return getWebappPath() + "css/" + cssFile;
	}
	
	/**
	 * 1. Check if the parameter is set within the request
	 * 2. if parameter could be found, save as session variable
	 * 3. If not, look up the session
	 * 4. if session value is null, set the default value to session and return the default value
	 * 
	 * @param request The actual Servlet request
	 * @param parName the name of the parameter to be read 
	 * @return the value of the parameter <i>parName</i>
	 */
	protected String parFromRequestOrSession(HttpServletRequest request, String parName, Object def)
	{
		String val = request.getParameter(parName);
		if (StringUtils.isSet(val))
		{
			 sess(request).setSessionVar(parName, val);
			return val;
		} else
		{
			Object o = sess(request).getSessionVar(parName);
			if (o == null)
			{
				sess(request).setSessionVar(parName, def);
				return  (def == null ? null : (String)def);
			} else
			{
				return (o == null ? null : (String)o);
			}						
		}
	}
	
	/**
	 * @see #parFromRequestOrSession(HttpServletRequest, String, Object)
	 * null is set as default value
	 */
	protected String parFromRequestOrSession(HttpServletRequest request, String parName)
	{
		return parFromRequestOrSession(request, parName, null);
		
	}
	
	/**
	 * @see #parFromRequestOrSession(HttpServletRequest, String, Object)
	 * But the return and default types are int
	 */
	protected int parFromRequestOrSessionInt(HttpServletRequest request, String parName, int def)
	{
		String parStr = parFromRequestOrSession(request, parName, new Integer(def).toString());
		return Integer.parseInt(parStr);
	}
	
	/**
	 * dump all request parameters	
	 */
	protected void dumpPars(HttpServletRequest request)
	{
		Hashtable<String, String> pars = HttpUtils.getRequestParameters(request);
		log(CollectionUtils.toString(pars));
	}

	/**
	 * Clear all codes which have been set e.g. within the process action. These codes are only valid for one request cycle
	 * Codes are error, warning and ajax-codes
	 */
	protected void clearAllCodes(HttpServletRequest request)
	{
		clearErrorCodes(request); // only error codes for one cycle should be stored
		clearMessageCodes(request); // only message codes for one cycle should be stored
		clearAjaxCodes(request);
		sess(request).setAjaxRequest(false);
	}

	/**
	 * @param request the request object holding the session information
	 * @return a session manager to easily access session information
	 */
	protected BaseStruXSLSessionManager sess(HttpServletRequest request)
	{
		return new BaseStruXSLSessionManager(request);
	}

	/**
	 * Override this method for checking preconditions for a concrete action
	 * 
	 * @param request the HTTP Servlet Request
	 * @param form the concrete action form of a certain action
	 * @throws Exception if the precondition fails, throw a concrete exception. An error code should be set to inform the clien about the error
	 */
	protected abstract void preCondition(HttpServletRequest request, ActionForm form) throws Exception;

	/**
	 * mask the IP address depending on the setting of the concrete application
	 * @param IP the IP-address to be masked
	 * @return the anonymized IP address. If no anonymization has to be done, the IP-address is returned unmodified
	 */
	protected abstract String anonymizeIP(HttpServletRequest request, String IP) throws Exception;

	/**
	 * check if the session variable SESS_KEY_ADMINLOGGEDIN is set
	 * @param request
	 * @return true, if the system administrator is correctly logged in
	 */
	protected boolean checkAdminLogin(HttpServletRequest request) 
	{		
		return sess(request).isAdminLoggedIn();
	}

	/**
	 * Stores a file uploaded by the user under a certain local path
	 * 
	 * @param file the file which was uploaded
	 * @param path the relative path where the file should be stored
	 * @return the path after modification (if any, e.g. adding extension)
	 */
	protected String storeFile(FormFile file, String path) throws FileNotFoundException, IOException
	{
		return FormFileUtils.write2File(file, path);
	}

	/**
	 * Adds an message code to this request
	 * @param request the Servlet request to store the messages within the session
	 * @param code the message code to be stored
	 */
	protected void addMessageCode(HttpServletRequest request, String code)
	{
		sess(request).addMessageCode(code);
	}

	/**
	 * Add certain codes to be delivered back to the view when an ajax-call has been performed
	 * 
	 * @param request the Servlet request to store the ajax code within the session
	 * @param code the ajax code to be stored
	 */
	protected void addAjaxCode(HttpServletRequest request, String code)
	{
		sess(request).addAjaxCode(code);
	}

	/**
	 * Returns the message codes which are stored within the session
	 * 
	 * @param request the Servlet request to store the messages within the session
	 * @return a list of message codes
	 */
	protected Vector<String> getMessageCodes(HttpServletRequest request)
	{
		return sess(request).getMessageCodes();
	}

	/**
	 * Returns the ajax codes which are stored within the session
	 * 
	 * @param request the Servlet request to store the ajax codes within the session
	 * @return a list of ajax codes
	 */
	protected Vector<String> getAjaxCodes(HttpServletRequest request)
	{
		return sess(request).getAjaxCodes();
	}

	/**
	 * Is a certain message code set?
	 * 
	 * @param request the Servlet request to store the messages within the session
	 * @param code the code to be looked up within the stored message codes
	 * @return true if a certain message code is set within the message code list
	 */
	protected boolean hasMessageCode(HttpServletRequest request, String code)
	{
		Vector<String> codes = getMessageCodes(request);
		return codes.contains(code);
	}

	/**
	 * Empty the message codes container
	 * 
	 * @param request the Servlet request to store the messages within the session
	 */
	protected void clearMessageCodes(HttpServletRequest request)
	{
		sess(request).clearMessageCodes();
	}

	/**
	 * Empty the ajax codes container
	 * 
	 * @param request the Servlet request to store the messages within the session
	 */
	protected void clearAjaxCodes(HttpServletRequest request)
	{
		sess(request).clearAjaxCodes();

	}

	/**
	 * Application specific rules can be applied when overriding this method
	 */
	protected boolean isDebug(HttpServletRequest request)
	{
		return sess(request).isDebug();
	}

	/**
	 * return as string to avoid conversion
	 * 
	 * @see #isDebug()
	 */
	protected String isDebugStr(HttpServletRequest request)
	{
		return new Boolean(isDebug(request)).toString();
	}

	/**
	 * Turn debug mode on or off. This setting is also delivered to the view
	 */
	protected void setDebug(HttpServletRequest request, boolean debug)
	{
		sess(request).setDebug(debug);
	}

	/**
	 * Write the Exception message and stacktrace as severe message
	 * 
	 * @param ex the exception which should be written to the error log
	 */
	protected void err(Exception e)
	{
		String exStr = ErrorHandlingUtils.stackTraceToString(e);
		this.logger.log(Level.SEVERE, this.getClass().toString() + " - " + e.getMessage());
		this.logger.log(Level.SEVERE, this.getClass().toString() + " - " + exStr);
	}

	/**
	 * Write <i>msg</i> with current class named in a predefined form into the logfile
	 * 
	 * @param msg the message to be logged
	 */
	protected void log(String msg)
	{
		this.logger.log(Level.INFO, this.getClass().toString() + " - " + msg);
	}

	/**
	 * @see log(String msg)
	 * @param Object.toString() is written to the logfile
	 */
	protected void log(Object o)
	{
		String msg = "null";
		if (o != null)
		{
			msg = o.toString();
		}
		this.logger.log(Level.INFO, this.getClass().toString() + " - " + msg);
		//System.out.println(msg);
	}

	/**
	 * @return the full path of the whole web application, e.g. /usr/share/tomcat/webapps/qsys
	 */
	protected String getWebappPath()
	{
		return this.getServlet().getServletContext().getRealPath("/");
		//ServletA
	}

	/**
	 * @return the id of the current session
	 */
	protected String getSessId(HttpServletRequest request)
	{
		return request.getSession().getId();
	}

	/**
	 * The error logging method
	 * 
	 * @param msg a message describing the error
	 * @param e the exception which raised the error
	 */
	protected void severe(String msg, Exception e)
	{
		this.logger.log(Level.SEVERE, msg, e);
	}

	/**
	 * Adds an error code to this request
	 * 
	 * @param request the Servlet request to store the errors within the session
	 * @param code the error code to be stored
	 */
	protected void AddErrorCode(HttpServletRequest request, String code)
	{
		sess(request).AddErrorCode(code);
	}

	/**
	 * @param request the Servlet request to store the errors within the session
	 * @return true if an error code is set
	 */
	protected boolean hasErrorCodes(HttpServletRequest request)
	{
		Vector<String> errCodes = this.getErrorCodes(request);
		return ((errCodes != null) && (errCodes.size() > 0));
	}

	/**
	 * Get the error codes which are stored within the session
	 * 
	 * @param request the Servlet request to store the errors within the session
	 * @return a list of error codes
	 */
	protected Vector<String> getErrorCodes(HttpServletRequest request)
	{
		return sess(request).getErrorCodes();
	}

	/**
	 * @param request the Servlet request to store the errors within the session
	 * 
	 * @param code the code to be looked up within the stored error codes
	 * @return true if a certain error code is set within the error code list
	 */
	protected boolean hasErrorCode(HttpServletRequest request, String code)
	{
		Vector<String> codes = getErrorCodes(request);
		return codes.contains(code);
	}

	/**
	 * Empty the error codes container
	 * 
	 * @param request the Servlet request to store the errors within the session
	 */
	protected void clearErrorCodes(HttpServletRequest request)
	{
		sess(request).clearErrorCodes();
	}

	/**
	 * Are any AJAX codes set?
	 */
	protected boolean hasAjaxCodes(HttpServletRequest request)
	{
		return sess(request).hasAjaxCodes();
	}

	/**
	 * Check which language is currently set. Look for a language set in the following order:
	 * 	1. set as request parameter?
	 * 	2. already set within the session
	 * 	3. try to get from the locale settings as delivered from the browser used by the user
	 * 	4. set English as default language, if no language could be set within steps 1-3
	 * @return the language to be used for processing the request
	 */
	protected String findCurrentLang(HttpServletRequest request)
	{
		String lang = request.getParameter(KEY_LANG);
		if (!StringUtils.isSet(lang))
		{
			lang = getCurrentLang(request); // get from session
			if (!StringUtils.isSet(lang))
			{
				// try to get language from locale settings
				Locale l = request.getLocale();
				lang = l.getLanguage().toUpperCase();

				// if the language as set within the browser is not supported by
				// the system, set the default language
				if (!this.getXmlLang(request).isLanguageSupported(lang))
				{
					lang = "EN";
				}
			}
		}
		return lang;
	}

	/**
	 * @return the current language as set within the session
	 */
	protected String getCurrentLang(HttpServletRequest request)
	{
		return sess(request).getCurrentLang();
	}

	/**
	 * Set the currently used language to session
	 * If the language is not supported, the first language as set for supported languages 
	 * within xslt-map
	 * 
	 * @param lang the language to be set
	 */
	protected void setCurrentLang(HttpServletRequest request, String lang)
	{
		if (!getXmlLang(request).isLanguageSupported(lang))
		{
			lang = getXmlLang(request).getDefaultLang();
		}
		sess(request).setCurrentLang(lang.toUpperCase());
	}

	/**
	 * Get language tokens from a certain language XML-file 
	 * REMARK: do not move to struxsl-baseVIEW, also some process-actions need language tokens (e.g. for generating e-Mails)
	 * 
	 * @param langFile the language file from which the tokens should be taken
	 * @return all these tokens within a Hashtable
	 */
	protected Hashtable<String, String> getLang(HttpServletRequest request, String langFile)
			throws TransformerException, ParserConfigurationException, SAXException, IOException
	{
		XmlLang xmlLang = getXmlLang(request);
		return xmlLang.getLangsFromDoc(langFile);
	}

	/**
	 * Generate an XML-Lang object initialized with language <i>lang</i> 
	 * 
	 * REMARK: do hot move to struxsl-baseVIEW, also some process actions need language tokens
	 * (e.g. for generating e-Mails) 
	 * @param lang the language to be used when receiving language specific tokens
	 * @return an XmlLang object for further language receiving tasks
	 */
	protected XmlLang getXmlLang(String lang, XsltMap map)
	{
		String fullPath2LangFiles = this.getWebappPath() + "/WEB-INF" + XML_LANG_DIR + lang.toUpperCase();
		return new XmlLang(fullPath2LangFiles, map);
	}

	/**
	 * Parses the XSLT-transformation document (only the first time, otherwise from session)
	 * The map is loaded once and stored within the session 
	 * (except in debug-mode: the map is loaded for each request)
	 * 
	 * @param request the HTTP-request
	 * @return a stuffed XsltMap object containing all mapping information
	 */
	protected XsltMap getXsltMap(HttpServletRequest request)
	{
		Object xsltMapO = sess(request).getXsltMap();
		if (xsltMapO == null || isDebug(request))
		{
			String mapFilePath = this.getWebappPath() + "/WEB-INF/xslt-map.xml";
			XsltMap xsltMap = new XsltMap(mapFilePath);
			sess(request).setXsltMap(xsltMap);
			return xsltMap;
		}
		return (XsltMap) xsltMapO;
	}
	
	/**
	 * Use the language currently stored within the session 
	 * REMARK: do hot move to struxsl-baseVIEW, also some process actions need language tokens (e.g. for generating e-Mails)
	 * 
	 * @see #getXmlLang(String)
	 * @param request the current HTTP Servlet request to access session variables
	 */
	protected XmlLang getXmlLang(HttpServletRequest request)
	{
		String lang = this.getCurrentLang(request);		
		return getXmlLang(lang, getXsltMap(request) );
	}
}
