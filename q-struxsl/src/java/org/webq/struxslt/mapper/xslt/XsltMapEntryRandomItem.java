package org.webq.struxslt.mapper.xslt;

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.webq.utils.CollectionUtils;
import org.webq.utils.XMLAble;
import org.webq.utils.XMLSerializException;
import org.webq.utils.XMLUtils;

/**
 * An Item for the get entry by random XlstMapEntry. Just holds necessary information and does XML-(de-)serializing
 * creation_date: Nov 24, 2006
 * @author albert
 */
public class XsltMapEntryRandomItem extends XMLAble
{
	private static final String STYLE_ENTRY_EL = "style_entry";
	private static final String PATH_ATT = "path";
	private static final String PROBABILITY_ATT = "prob";
	private static final String NAME_ATT = "name";
	
	private static final String CONDITIONS = "conditions";
	
	public static final String CONDITION_JAVA = "java";
	public static final String CONDITION_JAVASCRIPT = "javascript";
	public static final String CONDITION_FLASH = "flash";
	public static final String CONDITION_COOKIES = "cookies";
	
	private String xsltPath;
	private int prob;
	private String name; 
	String[] conditions; 
	
	
	public XsltMapEntryRandomItem()
	{
	}
	
	/**
	 * Decide if the current browser settings fulfill all conditions set for the item

	 * @param fullFilledCond all conditions offered by the browser
	 * @return true, if this style can be used due to the client's browser settings
	 */
	public boolean fullFillsConditions(Vector<String> fullFilledCond)
	{
		return CollectionUtils.containsAll(conditions, fullFilledCond);
	}
	
	public String[] getConditions() {
		return conditions;
	}

	public void setConditions(String[] conditions) {
		this.conditions = conditions;
	}

	/**
	 * Read all necessary items from the XML element
	 * 
	 * @param el the element from which data should be read
	 */
	public XsltMapEntryRandomItem(Element el)
	{
		xsltPath = el.getAttribute(PATH_ATT);
		prob = XMLUtils.getAttrAsInt(el, PROBABILITY_ATT);
		name = el.getAttribute(NAME_ATT);
		conditions = XMLUtils.getAttrAsStrArr(el, CONDITIONS);
	}

	/**
	 * @return the probability for being assigned
	 */
	public int getProb()
	{
		return prob;
	}

	/**
	 * @return the xsltPath.
	 */
	public String getXsltPath()
	{
		return xsltPath;
	}

	/**
	 * @return the name.
	 */
	public String getName()
	{
		return name;
	}

	/** Simply compare by name
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		XsltMapEntryRandomItem item = (XsltMapEntryRandomItem)obj;
		return name.equals(item.getName());
	}	
	
	/**
	 * check if <i>name</i> equals to the name of the this-item
	 * 
	 * @param name the name to be compared
	 * @return true, if name equals to the this-items name
	 */
	public boolean isMe(String name)
	{
		return this.name.equals(name);
	}

	/**
	 * TODO extend
	 * @see org.webq.utils.XMLAble#toXML(org.w3c.dom.Document)
	 */
	@Override
	public Element toXML(Document doc) throws XMLSerializException
	{
		Element el = doc.createElement(STYLE_ENTRY_EL);
		el.setAttribute(NAME_ATT, name);
		return el;
	}
}
