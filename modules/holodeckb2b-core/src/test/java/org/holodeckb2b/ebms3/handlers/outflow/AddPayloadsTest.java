/*
 * Copyright (C) 2016 The Holodeck B2B Team, Sander Fieten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.holodeckb2b.ebms3.handlers.outflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.holodeckb2b.common.messagemodel.Payload;
import org.holodeckb2b.common.messagemodel.UserMessage;
import org.holodeckb2b.common.mmd.xml.MessageMetaData;
import org.holodeckb2b.core.testhelpers.TestUtils;
import org.holodeckb2b.ebms3.constants.MessageContextProperties;
import org.holodeckb2b.ebms3.packaging.Messaging;
import org.holodeckb2b.ebms3.packaging.SOAPEnv;
import org.holodeckb2b.ebms3.packaging.UserMessageElement;
import org.holodeckb2b.interfaces.core.HolodeckB2BCoreInterface;
import org.holodeckb2b.interfaces.messagemodel.IPayload;
import org.holodeckb2b.interfaces.persistency.entities.IUserMessageEntity;
import org.holodeckb2b.module.HolodeckB2BCore;
import org.holodeckb2b.module.HolodeckB2BTestCore;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created at 23:39 29.01.17
 *
 * Checked for cases coverage (04.05.2017)
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 */
public class AddPayloadsTest {

    private static org.holodeckb2b.module.HolodeckB2BTestCore core;

    private static String baseDir;

    private AddPayloads handler;

    @BeforeClass
    public static void setUpClass() throws Exception {
        baseDir = AddPayloadsTest.class.getClassLoader()
                .getResource("handlers").getPath();
        core = new HolodeckB2BTestCore(baseDir);
        HolodeckB2BCoreInterface.setImplementation(core);
    }

    @Before
    public void setUp() throws Exception {
        handler = new AddPayloads();
    }

    @After
    public void tearDown() throws Exception {
        core.getPModeSet().removeAll();
    }

    @Test
    public void testDoProcessing() throws Exception {
        MessageMetaData mmd = TestUtils.getMMD("handlers/full_mmd_add_payloads.xml", this);
        // Creating SOAP envelope
        SOAPEnvelope env = SOAPEnv.createEnvelope(SOAPEnv.SOAPVersion.SOAP_12);
        // Adding header
        SOAPHeaderBlock headerBlock = Messaging.createElement(env);
        // Adding UserMessage from mmd
        OMElement umElement = UserMessageElement.createElement(headerBlock, mmd);

        MessageContext mc = new MessageContext();
        mc.setFLOW(MessageContext.OUT_FLOW);
        // Envelope is needed to add body payload
        try {
        	mc.setEnvelope(env);
        } catch (AxisFault axisFault) {
        	fail(axisFault.getMessage());
        }
        UserMessage userMessage = new UserMessage();        
        userMessage.setMessageId("payload-adder-01");
        
        // Programmatically added attachment payload
        Payload payload = new Payload();
        payload.setContainment(IPayload.Containment.ATTACHMENT);
        String payloadURI = "some_URI_01";
        payload.setPayloadURI(payloadURI);
        payload.setContentLocation(baseDir + "/flower.jpg");
        userMessage.addPayload(payload);

        // Programmatically added body payload
        payload = new Payload();
        payload.setContainment(IPayload.Containment.BODY);
        payload.setPayloadURI("some_URI_03");
        payload.setContentLocation(baseDir + "/document.xml");
        userMessage.addPayload(payload);

        // Setting input message property
        IUserMessageEntity userMessageEntity = 
        		HolodeckB2BCore.getStorageManager().storeIncomingMessageUnit(userMessage);
        mc.setProperty(MessageContextProperties.OUT_USER_MESSAGE, userMessageEntity);

        try {
            Handler.InvocationResponse invokeResp = handler.invoke(mc);
            assertNotNull(invokeResp);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(mc.getAttachmentMap());
        assertEquals(1, mc.getAttachmentMap().getAllContentIDs().length);
        
        assertNotNull(mc.getEnvelope().getBody().getFirstElement());        
    }
}
