package org.webq.struxslt;

/**
 * USe this exception for any error handling within the XstruXSL framework. 
 * To assure language independence, only an error code and not the message itself has to be set as error message
 * creation_date: Nov 20, 2006
 * 
 * @author albert
 */
public class StruXSLActionException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public StruXSLActionException(String errId)
	{
		super(errId);
	}
}
