package org.webq.struxslt.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.webq.struxslt.BaseStruXSLAction;
import org.webq.struxslt.StruXSLConf;
import org.webq.struxslt.XsltBaseKey;
import org.webq.struxslt.mapper.xslt.XmlLang;
import org.webq.struxslt.mapper.xslt.XsltMap;
import org.webq.struxslt.mapper.xslt.XsltMapEntry;
import org.webq.struxslt.mapper.xslt.XsltMapEntryRandom;
import org.webq.struxslt.mapper.xslt.XsltMapEntryRandomItem;
import org.webq.utils.CollectionUtils;
import org.webq.utils.HttpUtils;
import org.webq.utils.MIME;
import org.webq.utils.StringUtils;
import org.webq.utils.XMLUtils;
import org.webq.utils.XSLUtils;
import org.xml.sax.SAXException;

/**
 * This is the base class for all actions which should just generate data for
 * displaying. XML data is generated and transformed via XSLT <br/>
 * creation_date: 02.08.2006
 * 
 * @author albert
 */
public abstract class BaseStruXSLViewAction extends BaseStruXSLAction
{
	private static final String ADDMENUE_EL = "addMenue";
	private static final String XSL_DIR = "/WEB-INF/xsl/";

	private static final String STYLE_PAR = "style";

	private static final String STATUS_EL = "status";

	private static final String OUT_PAR = "out";
	private static final String OUT_PAR_XML = "xml";
	private static final String OUT_PAR_XSLT = "xslt";
	
	private static final String REASSIGN_PAR = "reassign";

	private Vector<String> getFullfilledConditions(HttpServletRequest request)
	{
		Vector<String> conds = new Vector<String>();
		//		TODO Enable this functionality again and check proper functioning		
		//		ConstantTrackerModelBean trB = getTrackerData(request);
		//		if (trB == null)
		//		{
		//			return conds;
		//		}
		//
		//		  if (trB.isJavaEnabled()) {
		//		  conds.add(XsltMapEntryRandomItem.CONDITION_JAVA); } if
		//		  (trB.isJavascriptEnabled()) {
		//		 conds.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT); }

		// TODO: DIRTY HACK!!! do not check
		conds.add(XsltMapEntryRandomItem.CONDITION_JAVA);
		conds.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT);
		return conds;
	}

	protected String getStyle(HttpServletRequest request)
	{
		return sess(request).getStyle();
	}

	protected void setStyle(HttpServletRequest request, String style)
	{
		sess(request).setStyle(style);
	}

	protected void setStyleFirstChoice(HttpServletRequest request, boolean fistChoice)
	{
		sess(request).setStyleFirstChoice(fistChoice);
	}

	protected boolean getStyleFirstChoice(HttpServletRequest request)
	{
		return sess(request).getStyleFirstChoice();
	}

	/**
	 * Here XSLT-transformation is done. Which stylesheet to choose is set
	 * 
	 * within a configuration file called WEB-INF/xslt-map Also appending of a
	 * language-file is associated, which contains language tokens necessary
	 * for language independence. The style can also be set manually via
	 * parameter <i>style</i> (only used for testing and development purposes)
	 */
	protected void transform(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping,
			ActionForm form) throws Exception
	{
		response.setCharacterEncoding(StruXSLConf.getEncoding());
		String path = mapping.getPath();

		XsltMap xsltMap = getXsltMap(request);
		this.setDebug(request, xsltMap.isDebug());
		String[] xsltIds = getXsltIds(request);
		String xsltId0 = CollectionUtils.get(xsltIds, 0, ""); 
		String xsltId1 = CollectionUtils.get(xsltIds, 1, "");
		XsltMapEntry xsltEntry = xsltMap.getEntry(path, xsltId0, xsltId1); // TODO extend for more than two parameters

		String xsltPath = this.getXsltPathFromSession(request, path);
		
		String styleNamePar = request.getParameter(STYLE_PAR);
		boolean reassignStyle = HttpUtils.getParameterBool(request, REASSIGN_PAR);
		
		if (StringUtils.isSet(styleNamePar))
		{
			xsltPath = this.getWebappPath() + XSL_DIR + xsltEntry.getXsltPath(styleNamePar);
			this.setStyle(request, styleNamePar);
			this.setStyleFirstChoice(request, false);
		}
		else
		{
			if (!StringUtils.isSet(xsltPath) || reassignStyle)
			{
				Vector<String> cond = getFullfilledConditions(request);
				//log("fullfilledCond:" + CollectionUtils.toString(cond));
				xsltPath = this.getWebappPath() + XSL_DIR + xsltEntry.getXsltPath(cond);
				this.setXsltPathToSession(request, path, xsltPath);

				// TODO: back hack :-/ assign style to user and path
				String style = this.getStyle(request);
				if (reassignStyle)
				{
					style = "";
				}

				if (xsltEntry instanceof XsltMapEntryRandom && !StringUtils.isSet(style))
				{
					this.setStyle(request, xsltEntry.getName());
					this.setStyleFirstChoice(request, xsltEntry.isFirstChoice());
					//log("USED_STYLE:" + xsltEntry.getName() + ":" + xsltEntry.isFirstChoice());
				}
			}
		}

		String lang = findCurrentLang(request);
		setCurrentLang(request, lang);
		XmlLang xmlLang = getXmlLang(lang, xsltMap);
		Document langDoc = getLangXml(xmlLang, xsltEntry);
		Document statusXml = generateStatusXml(request, xmlLang, lang);

		Document doc = null;

		boolean ajax = sess(request).isAjaxRequest();
		if (ajax)
		{
			doc = statusXml;
		}
		else
		{
			doc = generateXML(mapping, form, request, response);
			doc = XMLUtils.addDoc2Doc(doc, langDoc);
			doc = XMLUtils.addDoc2Doc(doc, statusXml);
		}

		//here, XML creation is finished, further steps define how content should be delivered		
		if (hasAjaxCodes(request) || ajax)
		{
			setXmlResponse(response, doc);
			return;
		}

		String outPar = request.getParameter(OUT_PAR);
		if (StringUtils.isSet(outPar) && (checkAdminLogin(request) || isDebug(request)))
		{
			if (outPar.equals(OUT_PAR_XML))
			{
				setXmlResponse(response, doc);
			}
			else if (outPar.equals(OUT_PAR_XSLT))
			{
				Document xsltDoc = XMLUtils.getDocumentFromFile(xsltPath, StruXSLConf.getEncoding());
				setXmlResponse(response, xsltDoc);
			}
		}
		else
		{
			response.setLocale(java.util.Locale.GERMANY); // TODO: Configurable or language dependent?
			String mime = xsltEntry.getMimeType();
			response.setContentType(mime);
			//XMLUtils.dump(doc, Encodings.UTF_8);

			// necessary to find the imported XSL files (xsl:import)
			String rootPath = this.getWebappPath() + "WEB-INF/xsl";
			setCurrXsltPath(request, xsltPath);
			XSLUtils.transform(doc, response.getOutputStream(), xsltPath, mime, rootPath, StruXSLConf.getEncoding());
		}
		sess(request).setAjaxRequest(false);
	}

	/**
	 * Do not perform a transformation but return the direct XML-content
	 * 	
	 * @param response is changed within this method (call-by-reference)
	 * @param doc the XML-document to be 
	 */
	private void setXmlResponse(HttpServletResponse response, Document doc) throws IOException
	{
		response.setContentType(MIME.XML);
		Writer writer = response.getWriter();
		writer.write(XMLUtils.documentToString(doc));
		writer.flush();
	}

	/**
	 * Just used for debugging info
	 * 
	 * @param xsltPath the relative path to the currently used XSLT file
	 */
	private void setCurrXsltPath(HttpServletRequest request, String xsltPath)
	{
		sess(request).setCurrXsltPath(xsltPath);
	}

	/**
	 * Just used for debugging info
	 * 
	 * @return the currently used XSLT path
	 */
	private String getCurrXsltPath(HttpServletRequest request)
	{
		return sess(request).getCurrXsltPath();
	}

	/**
	 * Get the already assigned XSLT path for an interviewee
	 * 
	 * @param group the group of for which the style should be retrieved
	 * @param quest the questionnaire id for which the style should be retrieved
	 * @param interviewee the interviewee id for which the style should be looked up
	 * @return null if value is not set, otherwise the full path to the XSLT file
	 */
	private String getXsltPathFromSession(HttpServletRequest request, String path)
	{
		Hashtable<XsltBaseKey, String> iEntries = getOrCreateXsltPathTable(request);
		XsltBaseKey xsltKey = getXsltKey(request, path);
		
		return xsltKey != null && iEntries.containsKey(xsltKey) ? iEntries.get(xsltKey) : null;
	}

	private void setXsltPathToSession(HttpServletRequest request, String path, String xsltPath)
	{
		Hashtable<XsltBaseKey, String> iEntries = getOrCreateXsltPathTable(request);
		XsltBaseKey xsltKey = getXsltKey(request, path);
		if (xsltKey != null && !iEntries.containsKey(xsltKey))
		{
			iEntries.put(xsltKey, path);
		}
		sess(request).setXsltPaths(iEntries);
	}

	/**
	 * The key for getting an xsltPath from the session
	 * 
	 * @param path this variable has to be part of the XsltKey
	 */
	protected abstract XsltBaseKey getXsltKey(HttpServletRequest request, String path);

	/**
	 * @return a list of additional keys to identify the right XSLT-path from the XSLT-mapper
	 */
	protected abstract String[] getXsltIds(HttpServletRequest request);

	private Hashtable<XsltBaseKey, String> getOrCreateXsltPathTable(HttpServletRequest request)
	{
		return sess(request).getOrCreateXsltPathTable();
	}

	/**
	 * The main entry point for request processing Here, XML-data is generated
	 * and transformed with a stylesheet (as returned from getXslTPath) this
	 * method always returns null because response is written directly within the
	 * transformation process
	 * 
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
	 *      org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)
	{
		try
		{
			preCondition(request, form);
			transform(request, response, mapping, form);
		} catch (Exception e)
		{
			if (e instanceof OtherForwardException)
			{
				String fwStr = ((OtherForwardException) e).getForward();
				ActionForward fw = mapping.findForward(fwStr);
				if (fw != null)
				{
					return fw;
				}
				logger.log(Level.SEVERE, "forward " + fwStr + " was not found");
			}
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return mapping.findForward(FW_ERROR);
		}
		clearAllCodes(request);
		return null;
	}



	/**
	 * Gets the language XML-File used for displaying the webpage
	 * 
	 * @param request the HTTP-request. Used for extraction of the (optional)
	 *        language parameter
	 *        
	 * @return an XML containing all language tokens necessary for displaying if
	 *         language does not change ==> Language document from session
	 *         otherwise: load the appropriate language document from file
	 *         according to the mapping made in lang-map.xml
	 */
	protected Document getLangXml(XmlLang xmlLang, XsltMapEntry xsltEntry) throws ParserConfigurationException,
			SAXException, IOException, TransformerException
	{
		String[] langFiles = xsltEntry.getLangFiles();
		return xmlLang.mergeLangFiles(langFiles);
	}

	protected abstract Dictionary<String, String> additionalStatusXmlEntries(HttpServletRequest request);
	protected abstract void appendStatusXmlEntries(HttpServletRequest request, Document doc, Element root);

	/**
	 * Generate common information about the currently logged in
	 * 
	 * @param request the HTTP-request for loading session information
	 * @return an XML document stuffed with general status information
	 */
	private Document generateStatusXml(HttpServletRequest request, XmlLang xmlLang, String lang)
			throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		Document doc = XMLUtils.createEmptyDoc();
		Element root = doc.createElement(STATUS_EL);
		doc.appendChild(root);

		Dictionary<String, String> addEntries = additionalStatusXmlEntries(request);
		if (addEntries != null)
		{
			Enumeration<String> addEnum = addEntries.keys();
			while (addEnum.hasMoreElements())
			{
				String key = addEnum.nextElement();
				String val = addEntries.get(key);
				XMLUtils.addWithTextNode(root, key, val);
			}
		}
		
		appendStatusXmlEntries(request, doc, root);

		XMLUtils.addWithTextNode(root, "servletpath", request.getServletPath());
		XMLUtils.addWithTextNode(root, "jsessionid", request.getSession().getId());
		XMLUtils.addWithTextNode(root, "style", this.getStyle(request));
		XMLUtils.addWithTextNode(root, "remote_ip", request.getRemoteAddr());
		XMLUtils.addWithTextNode(root, "debug", isDebugStr(request));
		XMLUtils.addWithTextNode(root, "admin", checkAdminLogin(request));
		XMLUtils.addWithTextNode(root, "baseurl", HttpUtils.getBaseApplicationURL(request));
		XMLUtils.addWithTextNode(root, "xsltpath", getCurrXsltPath(request));
		XMLUtils.addWithTextNode(root, "lang", getCurrentLang(request));
		addOtherLangStrings(root, request);

		// generate the whole request path with all parameters except "lang"
		String path = HttpUtils.getRequestPath(true, request, "lang");
		XMLUtils.addWithTextNode(root, "path", path);

		// also add the errors to the status element
		Vector<String> errors = xmlLang.getMessageDescrStrings(lang, this.getErrorCodes(request));
		Element errorsEl = doc.createElement("errors");
		root.appendChild(errorsEl);
		for (String message : errors)
		{
			XMLUtils.addWithTextNode(errorsEl, "error", message);
		}

		// also add the errors to the status element
		Vector<String> messages = xmlLang.getMessageDescrStrings(lang, this.getMessageCodes(request));
		Element messageEl = doc.createElement("messages");
		root.appendChild(messageEl);
		for (String message : messages)
		{
			XMLUtils.addWithTextNode(messageEl, "message", message);
		}

		Vector<String> ajaxMsgs = getAjaxCodes(request);
		if (!CollectionUtils.isEmpty(ajaxMsgs))
		{
			Element ajaxMsgsEl = doc.createElement("ajax_messages");
			root.appendChild(ajaxMsgsEl);
			for (String ajaxMsg : ajaxMsgs)
			{
				XMLUtils.addWithTextNode(messageEl, "ajax_message", ajaxMsg);
			}
		}
		return doc;
	}

	/**
	 * Adds an XML part to the result XML document containing
	 * 
	 * @param root the element to which the other languages should be added
	 */
	private void addOtherLangStrings(Element root, HttpServletRequest request)
	{
		String currLang = getCurrentLang(request);
		String[] otherLangs = getXmlLang(request).getOtherLangs(currLang);

		Document doc = root.getOwnerDocument();
		Element otherLangsEl = doc.createElement("otherLangs");
		for (String otherLang : otherLangs)
		{
			Element langEl = doc.createElement("lang");
			otherLangsEl.appendChild(langEl);
			langEl.setAttribute("value", otherLang);
		}

		List<String> addMenueEntries = addAdditionalMenueEntries();
		if (addMenueEntries != null && addMenueEntries.size() > 0)
		{
			Element menueEntryEl = doc.createElement(ADDMENUE_EL);
			for (String menueEntry : addMenueEntries)
			{
				Element el = doc.createElement(menueEntry);
				menueEntryEl.appendChild(el);
			}
			root.appendChild(menueEntryEl);
		}
		root.appendChild(otherLangsEl);
	}

	/**
	 * Override this method to add additional menu points
	 * 
	 * @return null
	 */
	protected List<String> addAdditionalMenueEntries()
	{
		return null;
	}

	/**
	 * Override this method and generate XML-Data necessary for response data
	 * 
	 * @return the XML document which will be rendered by XSLT
	 */
	protected abstract Document generateXML(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception;
}
