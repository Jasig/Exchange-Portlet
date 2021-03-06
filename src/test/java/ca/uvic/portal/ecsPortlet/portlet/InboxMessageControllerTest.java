package ca.uvic.portal.ecsPortlet.portlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.portlet.PortletRequest;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.mock.web.portlet.MockRenderRequest;
import org.springframework.mock.web.portlet.MockRenderResponse;
import org.springframework.web.portlet.ModelAndView;

import ca.uvic.portal.ecsPortlet.domain.InboxMessage;

/**
 * Test class to test the InboxMessageControllerTest.
 * @author Charles Frank
 * @version svn:$Id$
 *
 */
public class InboxMessageControllerTest extends TestCase {

    /**
     * private The application properties file.
     */
    private static final String TESTPROPFILE = "/ecs.test.properties";
    /**
     * private The exchange user to test with.
     */
    private String exchangeUser;
    /**
     * private The exchange user password to test with.
     */
    private String exchangePassword;
    /**
     * private The exchange user mowaEntitlementAttributeName to test with.
     */
    private String mowaEntitlementAttributeName;
    /**
     * private The exchange user mowaEntitlementAttributeValue to test with.
     */
    private String mowaEntitlementAttributeValue;
    /**
     * private The PortletRequest USER_INFO login id to test with.
     */
    private String portletRequestUserInfoLoginId;
    /**
     * private The PortletRequest USER_INFO password to test with.
     */
    private String portletRequestUserInfoPassword;
    /**
     * private The PortletRequest USER_INFO multiValue setting to test with.
     */
    private String portletRequestUserInfoMultiValue;

    /**
     * private The application context.
     */
    private static ApplicationContext appContext;
    /**
     * private The portlet context.
     */
    private static ApplicationContext ecsPortletContext;
    /**
     * private The portlet mock render request object.
     */
    private MockRenderRequest request;
    /**
     * private The portlet userInfo map.
     */
    private Map <String, Object> userInfo;
    /**
     * private Entitlements hash map of entitlement name and values.
     */
    private Map<String, List<Object>> entitlements;
    /**
     * private Specific entitlement list to be put into entitlements HashMap.
     *
     * @see entitlements
     */
     private List <Object> entitlementList;

    /**
     * private The commons logger.
     */
    private final Log logger = LogFactory.getLog(getClass());



    /**
     * @param name The name of the test to run.
     */
    public InboxMessageControllerTest(final String name) {
        super(name);
    }

    /**
     * Setup for testing.
     * @throws Exception Standard JUnit exception.
     */
    @Override
    protected final void setUp() throws Exception {
        super.setUp();
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(TESTPROPFILE));
        } catch (IOException e) {
           logger.debug("Failed to load testing properties.");
           e.printStackTrace();
        }
        try {
            appContext = new FileSystemXmlApplicationContext(
              new String[]{prop.getProperty("ecs.appContext.url")});
            ecsPortletContext = new FileSystemXmlApplicationContext(
              new String[]{prop.getProperty("ecs.portletContext.url")},
                  appContext);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
        exchangeUser                  = prop.getProperty("ecs.user");
        exchangePassword              = prop.getProperty("ecs.pass");
        mowaEntitlementAttributeName  =
            prop.getProperty("ecs.mowaEntitlementAttributeName");
        mowaEntitlementAttributeValue =
            prop.getProperty("ecs.mowaEntitlementAttributeValue");
        //The ecs.test.properties values for this must match what is
        //is actually set in ecs.properties for this test to pass because
        //this test uses the actual applicationContext.xml and portlet.xml
        //files.
        portletRequestUserInfoLoginId =
            prop.getProperty("ecs.portletRequest.userInfo.loginId");
        portletRequestUserInfoPassword =
            prop.getProperty("ecs.portletRequest.userInfo.password");
        portletRequestUserInfoMultiValue =
            prop.getProperty("ecs.portletRequest.userInfo.userInfoMultiValue");

        userInfo = new HashMap < String, Object>();

        request = new MockRenderRequest();

        //Simulate the org.jasig.portal.portlet.container.services.
        //RequestAttributeServiceImpl#getAttribute()
        //method that sets org.jasig.portlet.USER_INFO_MULTIVALUED
        entitlements = new HashMap <String, List<Object>>();

        //The specific entitlementList to be put into the entitlements HashMap
        entitlementList = new ArrayList <Object>();
    }

    /**
     * Teardown for testing.
     * @throws Exception Standard JUnit exception.
     */
    @Override
    protected final void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link ca.uvic.portal.ecsPortlet.portlet.
     * InboxMessageController#handleRenderRequest()}.
     * @throws Exception controller exception.
     */
    public final void testGetInboxMessages() throws Exception {
        InboxMessageController controller = (InboxMessageController)
            ecsPortletContext.getBean("inboxMessageController");
        MockRenderResponse response = new MockRenderResponse();
        //Map < String, Object> userInfo = new HashMap < String, Object>();
        userInfo.put(portletRequestUserInfoLoginId, exchangeUser);
        userInfo.put(portletRequestUserInfoPassword, exchangePassword);
        //userInfo.put(portletRequestUserInfoMultiValue, entitlements);
        request.setAttribute(PortletRequest.USER_INFO, userInfo);

        //Simulate the org.jasig.portal.portlet.container.services.
        //RequestAttributeServiceImpl#getAttribute()
        //method that sets org.jasig.portlet.USER_INFO_MULTIVALUED
        entitlementList.add(mowaEntitlementAttributeValue);
        entitlements.put(mowaEntitlementAttributeName, entitlementList);
        request.setAttribute(portletRequestUserInfoMultiValue, entitlements);

        ModelAndView mav = controller.handleRenderRequest(request, response);

        assertNotNull("Get model and view from controller", mav);
        //get the model
        Map < ? , ? > model = mav.getModel();
        assertTrue("model should contain messages",
                model.containsKey("messages"));
        ConcurrentLinkedQueue < InboxMessage > inboxMessages =
            (ConcurrentLinkedQueue) model.get("messages");
        Iterator < InboxMessage > msgIter = inboxMessages.iterator();
        InboxMessage firstMessage = msgIter.next();
        logger.debug("checking returned model message OwaId: "
                + firstMessage.getOwaId());
        assertNotNull("pull an message OwaId", firstMessage.getOwaId());
        assertNotNull("pull a message subject ", firstMessage.getSubject());

    }

    /**
     * Test method for {@link ca.uvic.portal.ecsPortlet.portlet.
     * CalendarItemController#handleRenderRequest()}, specifically the condition
     * for an appropriate view when the mowa_user ldap attribute is not present.
     * @throws Exception controller exception.
     */
    public final void testNoMowa() throws Exception {
        InboxMessageController controller = (InboxMessageController)
            ecsPortletContext.getBean("inboxMessageController");
        //Run this test if the checkMowaUser property is true in ecs-portlet.xml
        //portlet application context file, inboxMessageController bean.
        if (controller.checkMowaUser) {
            MockRenderResponse response = new MockRenderResponse();
            userInfo.put(portletRequestUserInfoLoginId, "admin");
            userInfo.put(portletRequestUserInfoPassword, "blah");
            //userInfo.put(portletRequestUserInfoMultiValue, entitlements);
            request.setAttribute(PortletRequest.USER_INFO, userInfo);

            //Simulate the org.jasig.portal.portlet.container.services.
            //RequestAttributeServiceImpl#getAttribute()
            //method that sets org.jasig.portlet.USER_INFO_MULTIVALUED
            //Don't give the user a mowa attribute value
            entitlementList.add("");
            entitlements.put(mowaEntitlementAttributeName, entitlementList);
            request.setAttribute(
                    portletRequestUserInfoMultiValue, entitlements);

            ModelAndView mav =
                controller.handleRenderRequest(request, response);
            Map < ? , ? > model = mav.getModel();
            assertNotNull("Get model and view from controller", mav);
            assertEquals("model view should be ecsNoMowa",
                    mav.getViewName(), "ecsNoMowa");
        }
    }
}
