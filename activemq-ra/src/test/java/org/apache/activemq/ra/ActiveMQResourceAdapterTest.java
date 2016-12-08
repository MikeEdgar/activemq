package org.apache.activemq.ra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class ActiveMQResourceAdapterTest {

    private static final String DEFAULT_HOST = "vm://localhost?broker.persistent=false";

    private Mockery context;
    private MessageActivationSpec activationSpec;
    private ActiveMQResourceAdapter adapterUnderTest;
    private Expectations expect;

    @Before
    public void setUp() {
        context = new Mockery();
        activationSpec = context.mock(MessageActivationSpec.class);

        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory();
        ActiveMQConnectionRequestInfo info = new ActiveMQConnectionRequestInfo();
        info.setServerUrl(DEFAULT_HOST);
        info.configure(cf, null);
        adapterUnderTest = new ActiveMQResourceAdapter();
        adapterUnderTest.setConnectionFactory(cf);
        adapterUnderTest.setInfo(info);

        expect = new Expectations() {
            {
                oneOf(activationSpec).getUserName();
                oneOf(activationSpec).getPassword();
                oneOf(activationSpec).redeliveryPolicy();
            }
        };
    }

    @Test
    public void testMakeConnectionMessageActivationSpecWithDefaultClientId() throws JMSException {
        final String expected = "connectionClientId";

        expect.oneOf(activationSpec).isDefaultClientId();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        expect.never(activationSpec).getClientId();
        expect.oneOf(activationSpec).isDurableSubscription();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        context.checking(expect);

        adapterUnderTest.getInfo().setClientid(expected);
        ActiveMQConnection connection = adapterUnderTest.makeConnection(activationSpec);

        context.assertIsSatisfied();
        assertEquals(expected, connection.getClientID());
    }

    @Test
    public void testMakeConnectionMessageActivationSpecWithConfiguredClientId() throws JMSException {
        final String expected = "configuredClientId";

        expect.oneOf(activationSpec).isDefaultClientId();
        expect.will(Expectations.returnValue(Boolean.FALSE));
        expect.oneOf(activationSpec).getClientId();
        expect.will(Expectations.returnValue(expected));
        expect.never(activationSpec).isDurableSubscription();
        context.checking(expect);

        adapterUnderTest.getInfo().setClientid("connectionClientId");
        ActiveMQConnection connection = adapterUnderTest.makeConnection(activationSpec);

        context.assertIsSatisfied();
        assertEquals(expected, connection.getClientID());
    }

    @Test
    public void testMakeConnectionMessageActivationSpecWithBlankDefaultClientId() throws JMSException {
        final String expected = "";

        expect.oneOf(activationSpec).isDefaultClientId();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        expect.never(activationSpec).getClientId();
        expect.oneOf(activationSpec).isDurableSubscription();
        context.checking(expect);

        adapterUnderTest.getInfo().setClientid(expected);
        ActiveMQConnection connection = adapterUnderTest.makeConnection(activationSpec);
        context.assertIsSatisfied();
        assertNull(connection.getClientID());
    }

    @Test
    public void testMakeConnectionMessageActivationSpecWithNoClientIds() throws JMSException {
        expect.oneOf(activationSpec).isDefaultClientId();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        expect.never(activationSpec).getClientId();
        expect.oneOf(activationSpec).isDurableSubscription();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        context.checking(expect);

        ActiveMQConnection connection = adapterUnderTest.makeConnection(activationSpec);
        context.assertIsSatisfied();
        assertNull(connection.getClientID());
    }

    @Test
    public void testMakeConnectionMessageActivationSpecWithNullClientId() throws JMSException {
        expect.oneOf(activationSpec).isDefaultClientId();
        expect.will(Expectations.returnValue(Boolean.FALSE));
        expect.oneOf(activationSpec).getClientId();
        expect.will(Expectations.returnValue(null));
        expect.oneOf(activationSpec).isDurableSubscription();
        expect.will(Expectations.returnValue(Boolean.TRUE));
        context.checking(expect);

        adapterUnderTest.getInfo().setClientid("connectionClientId");
        ActiveMQConnection connection = adapterUnderTest.makeConnection(activationSpec);

        context.assertIsSatisfied();
        assertNull(connection.getClientID());
    }
}
