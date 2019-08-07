package org.webq.struxslt.mapper.xslt.test;

import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.webq.struxslt.mapper.xslt.XsltMap;
import org.webq.struxslt.mapper.xslt.XsltMapEntry;
import org.webq.struxslt.mapper.xslt.XsltMapEntryRandomItem;
import org.webq.utils.ItemCounter;

/**
 * @author albert
 * Test if random selection of styles works properly
 */
public class XsltMapEntryRandomTest extends TestCase
{
	XsltMap xsltMap;

	/**
	 * get the xsltMap
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		String mapFilePath = "./data/view/xslt-map.xml";
		xsltMap = new XsltMap(mapFilePath);
	}

	public void testStyleAssignment()
	{
		String groupId = "PsychologieSalzburg_1177694328092";
		String questId = "Alternativtourismus_1177694385688";
		String path = "/doqv";

		XsltMapEntry xsltEntry = xsltMap.getEntry(path, groupId, questId);
		ItemCounter counter = new ItemCounter();

		for (int i = 0; i < 1000; i++)
		{
			//Vector<String> cond = new Vector<String>();
			//String xsltPath =  xsltEntry.getXsltPath(cond);
			String style = xsltEntry.getName();
			counter.add(style);
		}

		System.out.println();
		System.out.println("NOONE");
		counter.dump();
		Assert.assertTrue(counter.size() == 2);

		counter = new ItemCounter();
		for (int i = 0; i < 1000; i++)
		{
			Vector<String> cond = new Vector<String>();
			cond.add(XsltMapEntryRandomItem.CONDITION_JAVA);
			cond.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT);

			//String xsltPath =  xsltEntry.getXsltPath(cond);
			String style = xsltEntry.getName();
			counter.add(style);
		}

		System.out.println();
		System.out.println("ALL");
		counter.dump();
		Assert.assertTrue(counter.size() == 6);

		counter = new ItemCounter();
		for (int i = 0; i < 1000; i++)
		{
			Vector<String> cond = new Vector<String>();
			cond.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT);

			//String xsltPath =  xsltEntry.getXsltPath(cond);
			String style = xsltEntry.getName();
			counter.add(style);
		}

		System.out.println();
		System.out.println("JS");
		counter.dump();
		Assert.assertTrue(counter.size() == 5);
	}

	public void fullFillsConditionsTest()
	{
		XsltMapEntryRandomItem item = new XsltMapEntryRandomItem();
		String[] conds = new String[0];
		item.setConditions(conds);

		Vector<String> fullfilledConds = new Vector<String>();
		boolean fullfills = item.fullFillsConditions(fullfilledConds);
		Assert.assertTrue(fullfills);

		conds = new String[1];
		conds[0] = XsltMapEntryRandomItem.CONDITION_JAVASCRIPT;
		item.setConditions(conds);

		fullfilledConds = new Vector<String>();
		fullfilledConds.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT);
		fullfills = item.fullFillsConditions(fullfilledConds);
		Assert.assertTrue(fullfills);

		conds = new String[2];
		conds[0] = XsltMapEntryRandomItem.CONDITION_JAVA;
		conds[1] = XsltMapEntryRandomItem.CONDITION_JAVASCRIPT;
		item.setConditions(conds);

		fullfilledConds = new Vector<String>();
		fullfilledConds.add(XsltMapEntryRandomItem.CONDITION_JAVASCRIPT);
		fullfills = item.fullFillsConditions(fullfilledConds);
		Assert.assertTrue(fullfills);

	}

	/*
		public void testRnd()
		{
			for (int i = 0; i < 100; i++)
			{
				int rnd = RandomGenerator.getRandomNumber(0, 100);
				System.out.println(rnd);
			}
			 
		}
	*/
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

}
