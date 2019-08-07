package org.webq.struxslt.view;

/**
 * If for a view action another forward should be used, throw an Exception of this type
 * 
 * creation_date: 09.01.2008
 * @author albert
 */
public class OtherForwardException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String forward;

	public String getForward()
	{
		return forward;
	}

	/**
	 * @param forward the actionforward 
	 */
	public OtherForwardException(String forward)
	{
		super(forward);
		this.forward = forward;
	}
}
