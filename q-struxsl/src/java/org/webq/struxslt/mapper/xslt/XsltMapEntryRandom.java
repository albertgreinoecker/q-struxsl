package org.webq.struxslt.mapper.xslt;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.webq.utils.NumberUtils;
import org.webq.utils.RandomGenerator;
import org.webq.utils.XMLSerializException;
import org.webq.utils.XMLUtils;

/**
 * Stylesheets are randomly assigned to a certain action
 * creation_date: 04.08.2006
 * 
 * @author albert
 */
public class XsltMapEntryRandom extends XsltMapEntry
{
	private static final String DEFAULT = "default";
	private static final String STYLES_EL = "styles";
	private static final String GROUP_ATTR = "group";
	private static final String QUESTIONNAIRE_ATTR = "questionnaire";

	String group;
	String questionnaire;
	Vector<XsltMapEntryRandomItem> xsltItems;
	XsltMapEntryRandomItem selectedItem;

	/**
	 * A stylesheet is assigned by chance
	 * 
	 * @param el the element from which the entries should be read
	 */
	public XsltMapEntryRandom(Element el)
	{
		this(el, new Vector<String>());
	}

	/**
	 * A stylesheet is assigned by chance, but the fullfilled conditions are
	 * taken into consideration
	 * 
	 * @param el the element from which the entries should be read
	 * @param conFullfilled which fullfilled conditions does the client browser offer
	 */
	public XsltMapEntryRandom(Element el, Vector<String> conFullfilled)
	{
		super(el);
		group = el.getAttribute(GROUP_ATTR);
		questionnaire = el.getAttribute(QUESTIONNAIRE_ATTR);
		xsltItems = new Vector<XsltMapEntryRandomItem>();
		Vector<Element> xsltEls = XMLUtils.getChildElements(el);
		for (Element xsltEl : xsltEls)
		{
			xsltItems.add(new XsltMapEntryRandomItem(xsltEl));
		}
	}

	@Override
	public String getXsltPath()
	{
		selectedItem = selectItem();
		return selectedItem.getXsltPath();
	}

	@Override
	public String getXsltPath(Vector<String> conFullfilled)
	{
		selectedItem = selectItem(conFullfilled);
		return selectedItem.getXsltPath();
	}

	/**
	 * @see org.qsys.quest.action.view.mapper.xslt.XsltMapEntry#getName()
	 */
	@Override
	public String getName()
	{
		return selectedItem.getName();
	}

	/**
	 * Try to find the name of the style entry-item directly and return the
	 * XSLT-path. remark: selectedItem is set here, when the name could be
	 * looked up within the available styles
	 * 
	 * @see org.qsys.quest.action.view.mapper.xslt.XsltMapEntry#getXsltPath(java.lang.String)
	 */
	@Override
	public String getXsltPath(String name) throws XsltMapperException
	{
		for (XsltMapEntryRandomItem xsltItem : xsltItems)
		{
			if (xsltItem.isMe(name))
			{
				selectedItem = xsltItem;
				return xsltItem.getXsltPath();
			}
		}
		throw new XsltMapperException("name could not be found");
	}

	/**
	 * Select an item by chance, but where all conditions are fullfilled by the client's browser
	 * @param conFullfilled the conditions fullfilled by the browser
	 * @return the selected item
	 */
	private XsltMapEntryRandomItem selectItem(Vector<String> conFullfilled)
	{
		XsltMapEntryRandomItem tmpItem = selectItem();

		if (!tmpItem.fullFillsConditions(conFullfilled))
		{
			firstChoice = false;
			return selectItem(conFullfilled);
		}

		return tmpItem;
	}

	/**
	 * The class variable selectedItem is set here (only once)
	 */
	private XsltMapEntryRandomItem selectItem()
	{
		int rndNr = RandomGenerator.getRandomNumber(0, 100);
		int start = 0;
		for (XsltMapEntryRandomItem xsltItem : xsltItems)
		{

			int end = start + xsltItem.getProb();
			if (NumberUtils.isInRange(rndNr, start, end))
			{
				return xsltItem;
			}
			start = end;
		}
		return null;
	}

	/**
	 * compare path, group and questionnaire (also check if is default for all
	 * groups and/or all questionnaires)
	 * 
	 * @see org.qsys.quest.action.view.mapper.xslt.XsltMapEntry#isMe(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public boolean isMe(String path, String group, String questionnaire)
	{
		return (this.path.equals(path) && (this.group.equals(group) || isDefaultGroup()))
				&& (this.questionnaire.equals(questionnaire) || isDefaultQuestionnaire());
	}

	private boolean isDefaultGroup()
	{
		return this.group.equals(DEFAULT);
	}

	private boolean isDefaultQuestionnaire()
	{
		return this.questionnaire.equals(DEFAULT);
	}

	/**
	 * @see org.qsys.quest.action.view.mapper.xslt.XsltMapEntry#getAvailableStyles()
	 */
	@Override
	public List<String> getAvailableStyles()
	{
		List<String> styles = new Vector<String>();
		for (XsltMapEntryRandomItem xsltItem : xsltItems)
		{
			styles.add(xsltItem.getName());
		}
		return styles;
	}

	@Override
	public Element toXML(Document doc) throws XMLSerializException
	{
		Element root = doc.createElement(STYLES_EL);
		for (XsltMapEntryRandomItem xsltItem : xsltItems)
		{
			root.appendChild(xsltItem.toXML(doc));
		}
		return root;
	}
}
