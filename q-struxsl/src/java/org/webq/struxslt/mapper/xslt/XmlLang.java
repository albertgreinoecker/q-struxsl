package org.webq.struxslt.mapper.xslt;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.webq.struxslt.StruXSLConf;
import org.webq.utils.CollectionUtils;
import org.webq.utils.FileUtils;
import org.webq.utils.XMLUtils;
import org.xml.sax.SAXException;

/**
 * Here processing of Language Files is implemented
 * 
 * creation_date: Sep 20, 2006 
 * 
 * TODO: store all loaded language files for each requested resource to speed up processing
 * 
 * @author albert
 */
public class XmlLang
{
	private static final String LANG_EL = "lang";

	private String[] LANGS;

	private XsltMap map;
	/**
	 * @param langBaseDir the base directory where all language files are located (also the right language is already set)
	 */
	public XmlLang(String baseDir, XsltMap map)
	{
		this.baseDir = baseDir;
		this.map = map;
		LANGS = map.GetSupportedLangs();
	}
	
	/**
	 * the absolute directory where the language files are stored (also the language subdirectory is included)
	 */
	private String baseDir;

	/**
	 * holds all language tokens which are used in most of the views because this document is never modyfied, just load it once
	 */
	private Document globalLang;

	/**
	 * All message/error holding documents. Used for mapping of message/error codes to the langauge dependent description
	 */
	private static Hashtable<String, Document> messageDocs;
	

	/**
	 * @return all languages which are registered to be used within the system
	 */
	public String[] getLangs()
	{
		return LANGS;
	}

	/**
	 * @param lang the language (two character code) which should be checked (if lowercase, transformed to uppercase)
	 * @return true if language is not null, not empty and one of the supported languages
	 */
	public boolean isLanguageSupported(String lang)
	{
		if ((lang == null) || lang.equals(""))
			return false;

		lang = lang.toUpperCase();
		return CollectionUtils.contains(lang, LANGS);
	}

	/**
	 * Return an array of String containing all languages possible within the system except <i>lang</i>
	 * 
	 * @param lang the lang which should not be listed within the resulting string array
	 * @return all languages as string except <i>lang</i>
	 */
	public String[] getOtherLangs(String lang)
	{
		if (LANGS.length < 2)
		{
			return new String[0];
		}
		if (lang != null)
		{
			return CollectionUtils.minus(LANGS, lang);
		}
		return LANGS;
	}

	/**
	 * 
	 * @return The first supported lang as set in xslt-map
	 */
	public String getDefaultLang()
	{
		return LANGS[0];
	}
	
	/**
	 * @return a document which contains all globally needed language tokens
	 * If no files with language tokens are set, an empty document is returned
	 */
	private Document getGlobalLang() throws ParserConfigurationException, SAXException, IOException,
			TransformerException
	{
		Vector<String> globalLangPaths = map.getGlobalLangs();
		
		if (globalLangPaths.isEmpty())
		{
			return XMLUtils.createEmptyDoc();
		}
		
		String currLangDocPath = FileUtils.buildPath(baseDir, globalLangPaths.elementAt(0));
		globalLang = getDocumentFromFile(currLangDocPath);
		Element baseLangEl = globalLang.getDocumentElement();

		for (int i = 1; i < globalLangPaths.size(); i++)
		{
			String globalLangPath = globalLangPaths.elementAt(i);
			NodeList currLangNds = getLangNodesFromDoc(globalLangPath);
			XMLUtils.importNodes(baseLangEl, currLangNds);
		}
		return (Document) globalLang.cloneNode(true);
	}

	/**
	 * @param lang one of the supported languages
	 * @return the message/error code document according to <i>lang</i>
	 */
	private Document getMessageDoc(String lang) throws ParserConfigurationException, SAXException, IOException
	{
		lang = lang.toUpperCase();
		if (messageDocs == null)
		{
			messageDocs = new Hashtable<String, Document>();
		}

		if (messageDocs.containsKey(lang))
		{
			return messageDocs.get(lang);
		}

		String msgCodeFile = map.getMessageCodesFile();
		String msgCodePath = FileUtils.buildPath(baseDir, msgCodeFile);
		Document doc = getDocumentFromFile(msgCodePath);
		messageDocs.put(lang, doc);
		return doc;
	}

	/**
	 * Read all message ids from <i>msgs</i> and find the right description string according to the language set
	 * 
	 * @param msgs a list of message/error ids
	 * @return all message descriptions in the desired language
	 */
	public Vector<String> getMessageDescrStrings(String lang, Vector<String> msgs) throws ParserConfigurationException,
			SAXException, IOException, TransformerException
	{
		Vector<String> result = new Vector<String>();
		if (msgs != null)
		{
			Document doc = getMessageDoc(lang);
			for (String msg : msgs)
			{
				String msgText = XMLUtils.getNodeText(doc, "//msg[@id='" + msg + "']");
				result.add(msgText);
			}
		}
		return result;
	}

	/**
	 * Also the global language files are added here
	 * 
	 * @param baseDir the base directory where all language files are stored
	 * @param langFiles all language files which have to be merged
	 * @return one single language document which contains all language tokens of all <i>langFiles</i>
	 */
	public Document mergeLangFiles(String[] langFiles) throws ParserConfigurationException, SAXException, IOException,
			TransformerException
	{
		Document baseLangDoc = getGlobalLang();
		Element baseLangEl = baseLangDoc.getDocumentElement();			
		
		if (baseLangEl == null)
		{
			baseLangEl = XMLUtils.createDocumentRoot("langs");
			baseLangDoc = baseLangEl.getOwnerDocument();
		} 

		for (String langFile : langFiles)
		{
			NodeList currLangNds = getLangNodesFromDoc(langFile);
			XMLUtils.importNodes(baseLangEl, currLangNds);
		}
		return baseLangDoc;
	}

	/**
	 * Read a certain XML document stuffed with language tokens from file
	 * 
	 * @param langFile the relative path o
	 * @return
	 */
	private Document getLangDoc(String langFile) throws ParserConfigurationException, SAXException, IOException
	{
		String currLangDocPath = FileUtils.buildPath(baseDir, langFile);
		return getDocumentFromFile(currLangDocPath);
	}

	/**
	 * @param langFile the relative path to a language file
	 * @return all language token elements of the language file
	 */

	private NodeList getLangNodesFromDoc(String langFile) throws TransformerException, ParserConfigurationException,
			SAXException, IOException
	{
		Document currLangDoc = getLangDoc(langFile);
		return XPathAPI.selectNodeList(currLangDoc, "/" + LANG_EL + "/*");
	}

	/**
	 * Reads a language file and returns all language tokens within a Hashtable
	 * 
	 * @param langFile the language file from which the tokens should be read
	 * @return a Hashtable stuffed with: + key: the identifier of the language token + value: the language dependent token
	 */
	public Hashtable<String, String> getLangsFromDoc(String langFile) throws TransformerException,
			ParserConfigurationException, SAXException, IOException
	{
		NodeList langNl = getLangNodesFromDoc(langFile);
		return XMLUtils.getElementsAsKeyValuePairs(langNl);
	}
	
	private Document getDocumentFromFile(String langFile) throws ParserConfigurationException, SAXException, IOException
	{
		return XMLUtils.getDocumentFromFile(langFile, StruXSLConf.getEncoding());
	}
}
