package org.webq.struxslt.view;

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.webq.struxslt.BaseStruXSLAction;

/**
 * TODO: for future use: problem: a class from which an ajax request is called, is a view action with a concrete path, 
 * so it's different to the concrete ajax-action-URL. 
 * Workaround: when ajax parameters are set within the concrete process action, XML is returned with all page information plus
 * the ajax code to instruct the    
 * creation_date: 06.10.2007
 * 
 * @author albert
 */
public abstract class BaseStruXSLAjaxAction extends BaseStruXSLAction
{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)
	{
		try
		{
			preCondition(request, form);
		} catch (Exception e)
		{
			logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			return mapping.findForward(FW_ERROR);
		}
		return null;
	}
}
