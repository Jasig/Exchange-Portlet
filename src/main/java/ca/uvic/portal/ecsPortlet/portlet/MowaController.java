package ca.uvic.portal.ecsPortlet.portlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.portlet.mvc.AbstractController;

import ca.uvic.portal.ecsPortlet.domain.DataAccessResourceFailureException;

/**
 * This is a simple Controller Super Class, it provides common controller
 * functionality to the Mowa Portlet Controller Classes.
 *
 * @author Charles Frank
 * @version svn:$Id$
 */
public class MowaController extends AbstractController {
    /**
     * protected Apache commons logger.
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * protected The login id portlet.xml property, depends on portal used. This
     * property will come from an application properties file, through the
     * applicationContext and portletContext.
     */
    protected String loginIdPortletParam;
    /**
     * protected The password portlet.xml property, depends on portal used. This
     * property will come from an application properties file, through the
     * applicationContext and portletContext.
     */
    protected String passwordPortletParam;
    /**
     * protected The single sign on URL.
     */
    protected String singleSignOnUrl;
    /**
     * protected The owa base URL.
     */
    protected String owaUrl;
    /**
     * protected The login id backup field to use if loginIdPortletParam is not
     * available. This property will come from an application properties file,
     * through the applicationContext and portletContext.
     */
    protected String loginIdPortletParamBackup;
    /**
     * protected The mowa entitlement attribute name (likely from LDAP).  This
     * attribute's value determines if a portal user is a mowa user.
     */
    protected String mowaEntitlementAttributeName;
    /**
     * protected The mowa entitlement attribute value (likely from LDAP).  This
     * value determines if a portal user is a mowa user.
     */
    protected String mowaEntitlementAttributeValue;
    /**
     * protected The mowa user.
     */
    protected String user;
    /**
     * protected The mowa user password.
     */
    protected String pass;
     /**
      * protected The RenderRequest userInfo hashmap that stores userInfo.
      */
    protected Map<String, String> userInfo;

    /**
     * public The boolean value that performs a switch to turn on/off
     * a check to see if the portal user has a mowa account (useful for
     * displaying a nice page for any user w/out a mowa account).
     */
    public boolean checkMowaUser;


    /**
     * This method, if checkMowaUser is wired as true, will check to see if
     * the user has an LDAP attribute corresponding to a mowa account.  If not
     * it will return the name of the view corresponding to a need for a mowa
     * attribute.  If everything is fine, it will return an empty string, and
     * allow the subclass controller to set the view name.  This method also
     * initializes the userInfo Map, and sets the user and pass class attributes
     * if they are available.
     * @param request The RenderRequest Object.
     * @return String The Mowa view to show if checkMowaUser is enabled.  Empty
     * string if there isn't an inital view to show.
     * @throws Exception Controller Exception.
     */
    public final String initialMowaView(final RenderRequest request)
            throws Exception {

        // Get the USER_INFO from portlet.xml, which gets it from personDirs.xml
        userInfo = (Map<String,String>)
            request.getAttribute(PortletRequest.USER_INFO);

        if (checkMowaUser) {
            //This is a uPortal specific way to get around using the USER_INFO
            //issue of a Map(<String,String>), which for an attribute from the
            //datasource that has multiple values, eduPersonAffiliation or
            //eduPersonEntitlement, gets only the very first value as a String.
            //Here we can pull out a List of possible values.  This method
            //also keeps compliance with JSR-168 spec.
            Map<String, List<Object>> userInfoMulti =
                (Map <String, List<Object>>) request.
                        getAttribute("org.jasig.portlet.USER_INFO_MULTIVALUED");
            List<Object> affiliations = userInfoMulti.get(mowaEntitlementAttributeName);

            //Check if the user is mowa enabled
            boolean mowaEntitlement = false;
            //Iterator <String> it = userInfoMulti.keySet().iterator();
            if(affiliations != null) {
                if(logger.isDebugEnabled()) {
                    logger.debug("We have affiliations: ");
                    for(Object affiliation : affiliations) {
                        logger.debug(" " + affiliation);
                    }
                }
                mowaEntitlement =
                    affiliations.contains(mowaEntitlementAttributeValue);
            }

            //Handle the situation where there is no mowa entitlement
            //mowaEntitlement = false; //uncomment line for debugging view
            if (!mowaEntitlement) {
                return "ecsNoMowa";
            }
        }

        /*
         * if (logger.isDebugEnabled()) { logger.debug("loginIdPortletParam: '"
         * + loginIdPortletParam + "'"); logger.debug("passwordPortletParam: '"
         * + passwordPortletParam + "'"); }
         */
        user = (String) userInfo.get(loginIdPortletParam);
        pass = (String) userInfo.get(passwordPortletParam);

        // Handle the case where the user has just added the portlet, but
        // portlet won't work until next portal login.
        if (user == null || user.length() == 0) {
        //if (user.toString().equals("cpfrank")) {  //Keep this for view debug
            if (logger.isWarnEnabled()) {
                logger.warn("We have no user, trying "
                        + loginIdPortletParamBackup);
            }
            // If the login id is not available try the backup field.
            user = (String) userInfo.get(loginIdPortletParamBackup);
            //user = null; //  Keep this for debugging the view.
            if (user == null || user.length() == 0) {
                if (logger.isWarnEnabled()) {
                    logger.warn("We have no user or uid.");
                }
                return "ecsFirstTime";
            }
        }
        /*
        if(logger.isDebugEnabled()) {
            logger.debug("User is: '" + user + "'");
        }
        */
        logger.warn("User is: " + user);
        //Throw an Exception that we can map to an error view nicely, need
        //a pass or application fails.
        //pass = ""; //use this for testing exception resolver
        if(pass == null || pass == "") {
            String passErrMsg = "User: " + user +
                      " does not have a cached password,"  +
                      "check clearpass integration, " +
                      "node replication on cas, and portal security context.";
            if(logger.isErrorEnabled()) {
                logger.error(passErrMsg);
            }
            //TODO make an exception class for lack of exchangePassword
            throw new DataAccessResourceFailureException(passErrMsg, null);
        }
        return "";
    }

    /**
     * Set the loginId portlet param. This is closely tied with parameters made
     * accessible via the portlet.xml context file. This parameter is also
     * specific to the portal that the portlet will be deployed to. For example
     * it might be user.login.id for uPortal, but urn:sungardhe:dir:loginId for
     * Luminis Portal. This parameter is used with the JSR-168 portlet
     * specification USER_INFO information hash.
     *
     * @param loginIdParam
     *            The login id portlet param.
     * @see portlet.xml, applicationContext.xml for more information on this
     *      deployment specific property.
     */
    @Required
    public final void setLoginIdPortletParam(final String loginIdParam) {
        this.loginIdPortletParam = loginIdParam;
    }

    /**
     * Set the loginId portlet param backup field, it will be used if the
     * loginIdPortletParam is not available. This field should something similar
     * to "uid" that is sure to be populated in personDirectoryContext.xml. This
     * field is tied with parameters made accessible via the portlet.xml context
     * file. This parameter is used with the JSR-168 portlet specification
     * USER_INFO information hash.
     *
     * @param loginIdParamBackup
     *            The login id portlet param backup field.
     * @see portlet.xml, applicationContext.xml for more information on this
     *      deployment specific property.
     */
    @Required
    public final void setLoginIdPortletParamBackup(
            final String loginIdParamBackup) {
        this.loginIdPortletParamBackup = loginIdParamBackup;
    }

    /**
     * Set the password portlet param. This is closely tied with parameters made
     * accessible via the portlet.xml context file. This parameter is also
     * specific to the portal that the portlet will be deployed to. This
     * parameter is used with the JSR-168 portlet specification USER_INFO
     * information hash.
     *
     * @param passwordParam
     *            The password portlet param.
     * @see portlet.xml, applicationContext.xml for more information on this
     *      deployment specific property.
     */
    @Required
    public final void setPasswordPortletParam(final String passwordParam) {
        this.passwordPortletParam = passwordParam;
    }

    /**
     * Set the mowa entitlement attribute name (likely from LDAP).  This param
     * corresponds to a setting in portlet.xml context file
     * (example, eduPersonEntitlement).
     *
     * @param attributeName
     *          The mowa entitlement attribute value.
     */
    public final void setMowaEntitlementAttributeName(
            final String attributeName) {
        this.mowaEntitlementAttributeName = attributeName;
    }

    /**
     * Set the mowa entitlement attribute value (likely from LDAP).  This param
     * corresponds to a setting in portlet.xml context file
     * (example, urn:mace:uvic.ca:university:usource:mowa_user).
     *
     * @param attributeValue
     *          The mowa attribute value.
     *
     * @see mowaEntitlementAttributeName
     */
    public final void setMowaEntitlementAttributeValue(
            final String attributeValue) {
        this.mowaEntitlementAttributeValue = attributeValue;
    }

    /**
     * Set the checkMowaUser boolean value.  If true this will check to see if
     * the portal user has a mowa account, and if not, display a nice view
     * informing the user that they need a mowa account.
     *
     * @param checkAccount
     *          The boolean flag to enable a check on a users mowa account.
     */
    @Required
    public final void setCheckMowaUser(final boolean checkAccount) {
        this.checkMowaUser = checkAccount;
    }

    /**
     * Set the single sign on resource url.
     *
     * @param ssoUrl
     *            The single sign on url portlet param.
     * @see portlet.xml, applicationContext.xml for more information on this
     *      deployment specific property.
     */
    @Required
    public final void setSingleSignOnUrl(final String ssoUrl) {
        this.singleSignOnUrl = ssoUrl;
    }

    /**
     * Set the owa resource url.
     *
     * @param oUrl
     *            The base url for the owa web service. 
     * @see portlet.xml, applicationContext.xml for more information on this
     *      deployment specific property.
     */
    @Required
    public final void setOwaUrl(final String oUrl) {
        this.owaUrl = oUrl;
    }

}
