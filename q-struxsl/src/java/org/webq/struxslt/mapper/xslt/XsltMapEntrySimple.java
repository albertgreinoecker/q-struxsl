package org.webq.struxslt.mapper.xslt;

import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.webq.utils.XMLSerializException;
import org.webq.utils.XMLUtils;

/**
 * Just return the mapping according to the (deterministic setting) within the map
 * 
 * creation_date: 04.08.2006
 * @author albert
 */
public class XsltMapEntrySimple extends XsltMapEntry
{
	private static final String STYLES_EL = "styles";
	private static final String MAP_PATH = "path";
	private static final String XSLT_TAG = "xslt";
	
	String xsltPath;
	
	public XsltMapEntrySimple(Element el)
	{
		super(el);
		xsltPath = 	((Element)el.getElementsByTagName(XSLT_TAG).item(0)).getAttribute(MAP_PATH);	
	}
	
	/** 
	 * @see org.qsys.quest.action.view.xsltmap.XsltMapEntry#getXsltPath()
	 * @return the only registered path
	 */
	@Override
	public String getXsltPath()
	{
		return xsltPath;
	}

	/** 
	 * just compare if the path is equal
	 * 
	 * @see org.qsys.quest.action.view.mapper.xslt.XsltMapEntry#isMe(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isMe(String path, String group, String questionnaire)
	{
		return this.path.equals(path);
	}

	@Override
	public String getXsltPath(Vector<String> conds) 
	{
		//TODO: implement with conds
		return xsltPath;
	}
	
	/**
	 * TODO implement
	 * @see org.webq.utils.XMLAble#toXML(org.w3c.dom.Document)
	 */
	@Override
	public Element toXML(Document doc) throws XMLSerializException
	{
		try
		{
			return XMLUtils.createDocumentRoot(STYLES_EL);
		} catch (ParserConfigurationException e)
		{
			throw new XMLSerializException();
		}
	}

}
