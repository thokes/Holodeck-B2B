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
package org.holodeckb2b.events.security;

import java.util.Map;
import org.holodeckb2b.interfaces.messagemodel.IPayload;
import org.holodeckb2b.interfaces.messagemodel.ISignalMessage;
import org.holodeckb2b.interfaces.messagemodel.IUserMessage;
import org.holodeckb2b.interfaces.events.security.ISignatureVerifiedEvent;
import org.holodeckb2b.interfaces.security.ISignedPartMetadata;

/**
 * Is the implementation class of {@link ISignatureVerifiedEvent} to indicate that a signature for a received User
 * Message is successfully verified. The information about the digests can only be set when the event is created and not
 * be modified afterwards.
 *
 * @author Sander Fieten (sander at holodeck-b2b.org)
 * @since 4.0.0
 */
public class SignatureVerifiedEvent extends AbstractSignatureProcessedEvent implements ISignatureVerifiedEvent {

    /**
     * Creates a new <code>SignatureVerifiedEvent</code> for the given Signal Message Unit and digest of its ebMS header
     *
     * @param subject         The Signal Message Unit that was signed
     * @param headerDigest    The digest of the ebMS header
     */
    public SignatureVerifiedEvent(final ISignalMessage subject, final ISignedPartMetadata headerDigest) {
        super(subject, headerDigest);
    }

    /**
     * Creates a new <code>SignatureVerifiedEvent</code> for the given User Message and digests of ebMS header and
     * payloads.
     *
     * @param subject         The User Message that was signed
     * @param headerDigest    The digest of the ebMS header
     * @param payloadDigests  The information about the digests for the payloads that were part of the signature
     */
    public SignatureVerifiedEvent(final IUserMessage subject, final ISignedPartMetadata headerDigest,
                                  final Map<IPayload, ISignedPartMetadata> payloadDigests) {
        super(subject, headerDigest, payloadDigests);
    }
}
