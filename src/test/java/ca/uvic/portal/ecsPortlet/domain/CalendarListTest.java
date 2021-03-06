package ca.uvic.portal.ecsPortlet.domain;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unit testing for CalendarList.
 */
public class CalendarListTest extends TestCase {

    /**
     * private Set the TESTPROPFILE constant for testing.
     */
    private static final String TESTPROPFILE = "/ecs.test.properties";
    /**
     * private Set the fakeResponse, simulating a success response from
     * Exchange.
     */
    private static final String FAKERESPONSESUCCESS =
        "/ecs-calendar-list-success.xml";
    /**
     * private Set the fakeResponse, simulating a response from Exchange.
     */
    private static final String FAKERESPONSEERROR =
        "/find-folder-error.xml";
    /**
     * private Set the rules file for re-use.
     */
    private static String rulesFile;
    /**
     * Create the test case.
     * @param testName name of the test case
     */
    public CalendarListTest(final String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CalendarListTest.class);
    }

    /**
     * private Apache Commons logger.
     */
    private final Log logger = LogFactory.getLog(getClass());

    /**
     * Test the getter methods for the domain class.
     * Not using mock objects here because we should always get a calendar
     * response back from exchange (the default 'calendar' should always be
     * available).
     */
    /**
     * Setup for testing.
     * @throws Exception Standard JUnit exception.
     */
    @Override
    protected final void setUp() throws Exception {
        Properties prop = new Properties();
        try {
            prop.load(getClass().getResourceAsStream(TESTPROPFILE));
        } catch (IOException e) {
           e.printStackTrace();
        }

        rulesFile = prop.getProperty("ecs.calendarListRulesFile");
    }

    /**
     * private method Return a ResponseMessage for testing.
     * @param response The mock response file.
     * @return ResponseMessage
     */
    private ResponseMessage getResponse(final String response) {
        //Use a EcsSoapMock object to simulate call to exchange.
        EcsSoapMock soap =
            new EcsSoapMock(response, rulesFile);
        try {
            soap.queryExchange();
        } catch (Exception e) {
            assertNull("Got error " + e, e);
        }
        //Get the first, and in this case, only ResponseMessage back.
        ConcurrentLinkedQueue < Object > respMessages =
            soap.getExchangeObjects();
        Iterator < Object > respIterator = respMessages.iterator();
        assertNotNull("received response messages back",
                respIterator.hasNext());
        ResponseMessage respMessage = (ResponseMessage) respIterator.next();
        return respMessage;
    }
    /**
     * Test the getter methods for the domain class, for success response.
     */
    public final void testCalendarListSuccess() {

        ResponseMessage respMessage = getResponse(FAKERESPONSESUCCESS);

        //Get the first CalendarList back
        ConcurrentLinkedQueue < Object > calendarListItems =
            respMessage.getExchangeObjects();
        if (logger.isDebugEnabled()) {
            logger.debug("We have this many CalendarList objects: "
                    + calendarListItems.size());
        }
        Iterator < Object > calListIter = calendarListItems.iterator();
        assertNotNull("received calendar item back", calListIter.hasNext());
        CalendarList calItem = (CalendarList) calListIter.next();

        //System.out.println(calItem.getSubject());
        assertEquals("Success", respMessage.getResponseIndicator());
        assertEquals("NoError", respMessage.getResponseCode());
        assertNull("error message text should be null",
                respMessage.getResponseText());
        assertNotNull("got id", calItem.getId());
        assertNotNull("got displayName", calItem.getDisplayName());
        assertNotNull("got childFolderCount", calItem.getChildFolderCount());
    }
    /**
     * Test the getter methods for the domain class, for error response.
     */
    public final void testCalendarListError() {
        if (logger.isDebugEnabled()) {
            logger.debug("Testing error condition.");
        }
        ResponseMessage respMessage = getResponse(FAKERESPONSEERROR);
        assertEquals("Error", respMessage.getResponseIndicator());
        assertEquals("MockErrorCode",
                respMessage.getResponseCode());
        assertEquals("Mock error text.",
                respMessage.getResponseText());
    }

}
