package org.webq.struxslt.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.w3c.dom.Document;
import org.webq.utils.XMLUtils;

/**
 * The base class for all static pages
 * creation_date: Nov 8, 2006
 * 
 * @author albert
 */
public abstract class BaseStaticViewAction extends BaseStruXSLViewAction
{
	/**
	 * @see org.qsys.quest.action.view.BaseStruXSLViewAction#generateXML(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected Document generateXML(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		return XMLUtils.createEmptyDoc();
	}
}
