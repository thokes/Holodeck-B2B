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
package org.holodeckb2b.ebms3.handlers.inflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.holodeckb2b.common.messagemodel.UserMessage;
import org.holodeckb2b.common.mmd.xml.MessageMetaData;
import org.holodeckb2b.common.testhelpers.NullDeliveryMethod;
import org.holodeckb2b.core.testhelpers.TestUtils;
import org.holodeckb2b.ebms3.constants.MessageContextProperties;
import org.holodeckb2b.ebms3.packaging.Messaging;
import org.holodeckb2b.ebms3.packaging.SOAPEnv;
import org.holodeckb2b.ebms3.packaging.UserMessageElement;
import org.holodeckb2b.interfaces.core.HolodeckB2BCoreInterface;
import org.holodeckb2b.interfaces.persistency.entities.IUserMessageEntity;
import org.holodeckb2b.interfaces.processingmodel.ProcessingState;
import org.holodeckb2b.module.HolodeckB2BCore;
import org.holodeckb2b.module.HolodeckB2BTestCore;
import org.holodeckb2b.persistency.dao.StorageManager;
import org.holodeckb2b.pmode.helpers.DeliverySpecification;
import org.holodeckb2b.pmode.helpers.Leg;
import org.holodeckb2b.pmode.helpers.PMode;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created at 12:05 15.03.17
 *
 * Checked for cases coverage (24.04.2017)
 *
 * @author Timur Shakuov (t.shakuov at gmail.com)
 */
public class DeliverUserMessageTest {

    private static String baseDir;

    private static HolodeckB2BTestCore core;

    private DeliverUserMessage handler;

    @BeforeClass
    public static void setUpClass() throws Exception {
        baseDir = DeliverUserMessageTest.class.getClassLoader()
                .getResource("handlers").getPath();
        core = new HolodeckB2BTestCore(baseDir);
        HolodeckB2BCoreInterface.setImplementation(core);
    }

    @Before
    public void setUp() throws Exception {
        // Executed after org.holodeckb2b.as4.receptionawareness.DetectDuplicateUserMessages handler
        handler = new DeliverUserMessage();
    }

    @After
    public void tearDown() throws Exception {
        core.getPModeSet().removeAll();
    }

    @Test
    public void testDoProcessing() throws Exception {
        MessageMetaData mmd = TestUtils.getMMD("handlers/full_mmd.xml", this);
        // Creating SOAP envelope
        SOAPEnvelope env = SOAPEnv.createEnvelope(SOAPEnv.SOAPVersion.SOAP_12);
        // Adding header
        SOAPHeaderBlock headerBlock = Messaging.createElement(env);
        // Adding UserMessage from mmd
        OMElement umElement = UserMessageElement.createElement(headerBlock, mmd);

        MessageContext mc = new MessageContext();

        UserMessage userMessage
                = UserMessageElement.readElement(umElement);
        String pmodeId =
                userMessage.getCollaborationInfo().getAgreement().getPModeId();
        userMessage.setPModeId(pmodeId);

        PMode pmode = new PMode();
        pmode.setId(pmodeId);

        Leg leg = new Leg();
        DeliverySpecification deliverySpec = new DeliverySpecification();
        deliverySpec.setFactory(NullDeliveryMethod.class.getName());
        deliverySpec.setId("some_delivery_spec_01");
        leg.setDefaultDelivery(deliverySpec);

        pmode.addLeg(leg);

        core.getPModeSet().add(pmode);

        StorageManager updateManager = HolodeckB2BCore.getStorageManager();
        IUserMessageEntity userMessageEntity =
                updateManager.storeIncomingMessageUnit(userMessage);
        mc.setProperty(MessageContextProperties.IN_USER_MESSAGE,
                userMessageEntity);

        updateManager.setProcessingState(userMessageEntity, ProcessingState.READY_FOR_DELIVERY);

        assertEquals(ProcessingState.READY_FOR_DELIVERY,
                userMessageEntity.getCurrentProcessingState().getState());

        try {
            Handler.InvocationResponse invokeResp = handler.invoke(mc);
            assertEquals(Handler.InvocationResponse.CONTINUE, invokeResp);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(ProcessingState.DELIVERED,
                userMessageEntity.getCurrentProcessingState().getState());
    }
}