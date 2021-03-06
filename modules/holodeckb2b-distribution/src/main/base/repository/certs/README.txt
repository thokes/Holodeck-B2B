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


1. Contents of this directory
=============================

This directory is used by the default security provider to store the Java
keystore files that hold the certificates that are used for processing signed
and/or encrypted messages.

It contains three keystores:

1) "encryptionkeys.jks" holding the public keys of trading partners. As the name
   suggests these are used to encrypt messages sent to the partner.

2) "privatekeys.jks" holding the private keys. These are used to sign
   sent messages and decrypt received messages.

3) "trustedcerts.jks" holding trusted certificates of both trading partners and
   certificate authorities. These are used to the validate the signature of
   received messages.

The distribution package by default includes empty keystores, with simple
passwords: "secret" for the private one, "nosecrets" for the public
one and "trusted" for the one with CA certificates. It is HIGHLY RECOMMENDED to
change these passwords to safer ones, see below how to configure Holodeck B2B
for the new passwords.

NOTE: As the renaming of the first keystore already indicates its usage has
changed compared to Holodeck B2B version 3 as the certificates stored in the
"encryptionkeys.jks" keystore are now only used for encryption and not used for
signature validation. This means that a certificate of a partner that is used
for both encryption and signature validation must be loaded in both the
"encryptionkeys.jks" and "trustedcerts.jks".

2. Configuring Holodeck B2B
===========================

To give Holodeck B2B access to the keystores you need to configure the
keystore passwords in the Holodeck B2B configuration file which is found
in «Holodeck B2B base dir»/conf/holodeckb2b.xml
Set "PrivateKeyStorePassword", "PublicKeyStorePassword" and "TrustKeyStorePassword"
parameters to the passwords of the respective keystores.

NOTE: If you want the change the passwords for the default keystores you must
also change the password on the keystore files by executing the following
command:
    keytool -storepasswd -keystore «path to keystore»

Also the location where Holodeck B2B should look for the keystores can be
specified by setting the "PrivateKeyStorePath", "PublicKeyStorePath" and
"TrustKeyStorePath" parameters.

3. Adding certificates and private keys
=======================================

Each certificate of a trading partner or private key that is added to a keystore
is assigned an identifier, called the alias. This alias is used in the P-Mode to
identify the certificate / key that must be used for processing the message. It
is RECOMMENDED to use descriptive aliases for easy identification.
Although the aliases of trusted certificate authorities' certificates (in
"trustedcerts.jks") are not used in the P-Modes it is still RECOMMENDED to use
meaningful aliases for these too.

To add a X.509v3 certificate holding the public key of a trading
partner or trusted CA to the public or trusted keystore use the following
command:

keytool -importcert \
        -keystore «location of the keystore» \
        -storepass «your keystore password» \
        -alias «alias for the cert in keystore» \
        -file «path to certificate file»

To add a PKCS#12 formatted certificate holding the private of a trading partner
to the private keystore use the following command:

keytool -importkeystore -srcstoretype PKCS12 \
        -srckeystore «path to certificate file» \
        -srcalias «the name of the certificate in the PKCS#12 file» \
        -srcstorepass «the password to access the PKCS#12 file» \
        -destkeystore «Holodeck B2B base dir»/repository/certs/privatekeys.jks \
        -deststorepass «your keystore password» \
        -destalias «alias for cert in keystore» \
        -destkeypass «the password to set on the new entry in the keystore»

NOTE: Use the following command to list the certificates in the PKCS#12
file and show their names / aliases:
keytool -list -v -storetype pkcs12 -keystore «path to certificate file»

4. Examples
===========

The examples/certs directory contains two sample keystores which contain
the certificates that are used in the example P-Modes (contained in
examples/pmodes). Their passwords are the same as the default keystores.
You can therefore just overwrite the default keystores with the example
keystores.

When using the private key in a P-Mode the password is
"Example" + 'A' | 'B' | 'C' | 'D'
