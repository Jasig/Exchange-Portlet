package ca.uvic.portal.ecsPortlet.portlet;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

import ca.uvic.portal.ecsPortlet.service.InboxMessageService;

/**
 * This is a simple Controller which delegates to the
 * {@link InboxMessageService InboxMessageService} and then populates the model
 * with all returned inbox messages.  Implements the controller interface.
 * @author Charles Frank
 * @see InboxMessageService
 */
public class InboxMessageController extends AbstractController {

    /**
     * private Apache commons logger.
     */
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * private The inbox message service.
     */
    private InboxMessageService inboxMessageService;
    /**
     * private The login id portlet.xml property, depends on portal used.  This
     * property will come from an application properties file, through the
     * applicationContext and portletContext.
     */
    private String loginIdPortletParam;
    /**
     * private The password portlet.xml property, depends on portal used.  This
     * property will come from an application properties file, through the
     * applicationContext and portletContext.
     */
    private String passwordPortletParam;

    /**
     * Method to list exchange inbox messages.
     * @param request The request object.
     * @param response The response object.
     * @return ModelAndView return the model and view.
     */
    @Override
    public final ModelAndView handleRenderRequestInternal(
            final RenderRequest request, final RenderResponse response) {
        //Get the USER_INFO from portlet.xml, which gets it from personDirs.xml
        Map userInfo =
            (Map) request.getAttribute(PortletRequest.USER_INFO);
        if (logger.isDebugEnabled()) {
           logger.debug("loginIdPortletParam: '" + loginIdPortletParam + "'");
           logger.debug("passwordPortletParam: '" + passwordPortletParam + "'");
        }
        String user  = (String) userInfo.get(loginIdPortletParam);
        String pass  = (String) userInfo.get(passwordPortletParam);
        /*
        if(logger.isDebugEnabled()) {
           logger.debug("USER: '" + user + "'");
           logger.debug("PASSWORD: '" + pass + "'");
        }
        */

        //logical view name        => inboxMessages
        //variable holding objects => messages
        return new ModelAndView("inboxMessages", "messages",
                inboxMessageService.getInboxMessages(user, pass));
    }

    /**
     * Set the InboxMessageService.
     * @param inboxMsgService The InboxMessageService to set.
     */
    @Required
    public final void setInboxMessageService(
            final InboxMessageService inboxMsgService) {
        this.inboxMessageService = inboxMsgService;
    }


    /**
     * Set the loginId portlet param.  This is closely tied with parameters
     * made accessible via the portlet.xml context file.  This parameter is
     * also specific to the portal that the portlet will be deployed to.  For
     * example it might be user.login.id for uPortal, but
     * urn:sungardhe:dir:loginId for Luminis Portal.  This parameter is used
     * with the JSR-168 portlet specification USER_INFO information hash.
     * @param loginIdParam The login id portlet param.
     * @see portlet.xml, applicationContext.xml for more information on this
     * deployment specific property.
     */
    @Required
    public final void setLoginIdPortletParam(final String loginIdParam) {
        this.loginIdPortletParam = loginIdParam;
    }

    /**
     * Set the password portlet param.  This is closely tied with parameters
     * made accessible via the portlet.xml context file.  This parameter is
     * also specific to the portal that the portlet will be deployed to.
     * This parameter is used with the JSR-168 portlet specification USER_INFO
     * information hash.
     * @param passwordParam The password portlet param.
     * @see portlet.xml, applicationContext.xml for more information on this
     * deployment specific property.
     */
    @Required
    public final void setPasswordPortletParam(final String passwordParam) {
        this.passwordPortletParam = passwordParam;
    }
}