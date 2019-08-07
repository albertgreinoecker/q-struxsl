package org.webq.struxslt.process;

import org.apache.struts.action.ActionForward;

/**
 * This exception is thrown when there is another forward to be used as the standard forwards
 * Mostly, forwards with parameters are then used in this case
 * 
 * creation_date: 22.12.2007
 * @author albert
 */
public class OtherForwardException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 230033606767464354L;
	ActionForward fw;
	
	/**
	 * @param fw the forward to be used
	 */
	public OtherForwardException(ActionForward fw)
	{
		this.fw = fw;
	}
	
	public ActionForward getFw()
	{
		return fw;
	}
	
}
