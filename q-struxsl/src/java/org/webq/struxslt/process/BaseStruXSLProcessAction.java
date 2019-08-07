package org.webq.struxslt.process;

import java.io.Writer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.webq.struxslt.BaseMessageKeys;
import org.webq.struxslt.BaseStruXSLAction;
import org.webq.struxslt.StruXSLConf;
import org.webq.utils.CollectionUtils;
import org.webq.utils.HttpUtils;
import org.webq.utils.StringUtils;

/**
 * This is the base class for all actions which should process any data (which in general means writing information to the database or to the session) <br/>
 * creation_date: 02.08.2006
 * 
 * @author albert
 */
public abstract class BaseStruXSLProcessAction extends BaseStruXSLAction
{
	// forward definitions (are used by xdoclet definitions for all actions)
	protected static final String FW_FINISHED = "finished";
	protected static final String FW_TOQUESTION = "toquestion";
	protected static final String FW_INTERVIEWEE_EXISTS = "interviewee_exists";

	protected final String AJAX_PAR = "ajax";
	private final String TEXT_FEEDBACK_PAR = "tfb";

	private final String TEXT_FEEDBACK_OK = "OK";

	/**
	 * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
	 *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public final ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response)
	{
		String lang = findCurrentLang(request);
		setCurrentLang(request, lang);
		
		response.setCharacterEncoding(StruXSLConf.getEncoding());
		boolean ajax = HttpUtils.getParameterBool(request, AJAX_PAR);
		boolean textFeedBack = HttpUtils.getParameterBool(request, TEXT_FEEDBACK_PAR);

		if (ajax)
		{
			sess(request).setAjaxRequest(true);
		}

		try
		{
			preCondition(request, form);
			String forward = doProcessData(form, request);
			if (StringUtils.isSet(forward) && !ajax && !textFeedBack)
			{
				ActionForward fw = mapping.findForward(forward);
				return fw;
			}
		} catch (Exception e)
		{
			if (e instanceof OtherForwardException)
			{
				return ((OtherForwardException) e).getFw();
			}

			e.printStackTrace();
			if (!this.hasErrorCodes(request))
			{
				this.AddErrorCode(request, BaseMessageKeys.MSG_UNDEFINED_ERR);
			}
			if (!ajax && !textFeedBack) //AJAX requests are delivered to the normal forward-success-page but with the error message delivered
			{
				return mapping.findForward(FW_ERROR);
			}
		}

		if (textFeedBack)
		{
			writeErrorOkToResponse(request, response);
			clearAllCodes(request);
			return null;
		}
		return mapping.findForward(FW_SUCCESS);
	}

	/**
	 * Write either OK or error codes to the response stream (set text as mime type)	
	 */
	private void writeErrorOkToResponse(HttpServletRequest request, HttpServletResponse response)
	{
		Vector<String> errors = sess(request).getErrorCodes();
		try
		{
			Writer writer = response.getWriter();
			if (errors == null || errors.size() == 0)
			{
				writer.write(TEXT_FEEDBACK_OK);
			}
			else
			{
				writer.write(CollectionUtils.toString(errors));
			}
			writer.flush();
		} catch (Exception e)
		{
			err(e);
		}
	}

	/**
	 * The concrete processing of data is implemented here within the subclasses
	 * 
	 * @param form the concrete actionform
	 * @return if null or an empty string is returned, the success-forward is used by default otherwise, the name of the mapped forward is returned
	 * @throws Exception throwing any exception causes an error-forward
	 */
	public abstract String doProcessData(ActionForm form, HttpServletRequest request) throws Exception;
}
