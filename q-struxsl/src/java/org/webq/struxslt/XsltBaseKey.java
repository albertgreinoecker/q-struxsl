package org.webq.struxslt;

/**
 * Extend this class if other conditions should be used for selecting a certain entry within the XSLT-map
 * currently, only the Serlvet path is used as key
 * 
 * @author albert
 *
 */
public class XsltBaseKey implements Comparable<XsltBaseKey>
{
	public String path; //the path to the requested view action

	public XsltBaseKey()
	{
		this("");
	}

	public XsltBaseKey(String path)
	{
		super();
		this.path = path;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	/**
	 * Compare only by path
	 */
	public int compareTo(XsltBaseKey key)
	{
		return path.compareTo(key.getPath());
	}

	@Override
	public String toString()
	{
		return path;
	}
}
