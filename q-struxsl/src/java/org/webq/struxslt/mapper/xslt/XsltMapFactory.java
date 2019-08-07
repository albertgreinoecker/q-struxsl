package org.webq.struxslt.mapper.xslt;

import org.w3c.dom.Element;

/**
 * Load a concrete XSLT-Mapper
 * creation_date: 04.08.2006
 * 
 * @author albert
 */
public class XsltMapFactory
{	
	//the currently supported map-types
	public static final String TYPE_ATT = "type";
	
	public static final String MAP_SIMPLE = "simple";
	public static final String MAP_RANDOM = "random";
	
	/**
	 * Choose which  map-entry to be returned according to the attribute value of type	
	 * 
	 * @param el the map-entry as XML-Element
	 * @return a concrete subclass of XsltMapEntry
	 */
	public static XsltMapEntry getEntry(Element el)
	{
		String type = el.getAttribute(TYPE_ATT);
		if (type.equals(MAP_SIMPLE))
		{
			return new XsltMapEntrySimple(el);
		} else if (type.equals(MAP_RANDOM))
		{
			return new XsltMapEntryRandom(el);
		}
		return null;
	}
}
