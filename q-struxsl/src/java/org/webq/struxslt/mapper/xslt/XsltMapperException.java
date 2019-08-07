package org.webq.struxslt.mapper.xslt;

/**
 * This exception is thrown when something went wrong when trying to find a mapper entry within the XSLT-map
 * 
 * creation_date: 19.03.2007
 * @author albert
 */
public class XsltMapperException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XsltMapperException()
	{
	}

	/**
	 * @see Exception#Exception(String)
	 * @param message the message to be set
	 */
	public XsltMapperException(String message)
	{
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public XsltMapperException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public XsltMapperException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
