/* ******************************************************************** *
 *                                                                      *
 * Disclaimer                                                           *
 *                                                                      *
 *   Information provided in this document is provided "as is" without  *
 * warranty of any kind, either express or implied. Every effort has    *
 * been made to ensure accuracy and conformance to standards accepted   *
 * at the time of publication. The reader is advised to research other  *
 * sources of information on these topics.                              *
 *                                                                      *
 * The user assumes the entire risk as to the accuracy and the use of   *
 * this document. This document may be copied and distributed subject   *
 * to the following conditions:                                         *
 *  1. All text must be copied without modification and all pages must  *
 *         be included;                                                 *
 *  2. All copies must contain copyright notice and any other notices   *
 *         provided therein; and                                        *
 *  3. This document may not be distributed for profit. All trademarks  *
 *         acknowledged.                                                *
 *                                                                      *
 * ******************************************************************** */

/**
 * Digitally sign a message with an MD5withRSA signature.
 *
 * @author Jason Weiss <jasonweiss@yahoo.com> - Copyright (c) 2004 Elsevier
 * @author Andy Pippin <pippin@cs.ucsb.edu>
 * 
 *   This code was taken from pages 113-117 of Jason Weiss's book "Java
 * Cryptology Extensions". Any errors or ommissions are my own.
 */

package edu.ucsb.cs.jicos.admin.chameleon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class Signature
{
    //
    //-- Constants -------------------------------------------------------

    /** Type of key to use while signing. */
    public static final String KEY_ALGORITHM = "RSA";

    /** The type of encoding the contents of the file to be signed.. */
    public static final String TEXT_ENCODING = "UTF-8";

    /** Signature encoding type. */
    public static final String SIGNATURE_TYPE = "MD5withRSA";

    //
    //-- Methods ---------------------------------------------------------

    public final static byte[] getSignature( PrivateKey privateKey,
            byte[] contentToSign ) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException,
            InvalidKeyException, SignatureException
    {
        if (null == privateKey)
        {
            throw new InvalidKeyException( "Key cannot be null" );
        }

        // Create the signature.
        java.security.Signature signatureEngine = java.security.Signature
                .getInstance( SIGNATURE_TYPE );
        signatureEngine.initSign( privateKey );

        // Pass in the bytes and pull out the signature.
        //        String fileData = CryptoUtil.readPlainTextFile( new File (
        // filenameToSign ) );
        signatureEngine.update( contentToSign );
        byte[] signature = signatureEngine.sign();

        // Return the signature
        return (signature);
    }

    /**
     * Verify a signature.
     * 
     * @param publicKey
     * @param contentToVerify
     *            The bytes to verify
     * @param signature
     *            The signature to verify.
     * @return Verified (<CODE>true</CODE>), or not (<CODE>false</CODE>).
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static final boolean verifySignature( PublicKey publicKey,
            byte[] contentToVerify, byte[] signature )
            throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException
    {
        boolean isVerified = false;

        if (null == publicKey)
        {
            throw new InvalidKeyException( "Key cannot be null" );
        }
        if (null == signature)
        {
            throw new SignatureException( "Signature cannot be null" );
        }

        if (null != contentToVerify)
        {
            java.security.Signature signatureEngine = java.security.Signature
                    .getInstance( SIGNATURE_TYPE );
            signatureEngine.initVerify( publicKey );

            signatureEngine.update( contentToVerify );
            isVerified = signatureEngine.verify( signature );
        }

        return (isVerified);
    }

    /**
     * Create the in-memory version of the Private Key from a file.
     * 
     * @param publicKeyFilename
     *            the URI of the file containing the private key.
     * @return A private key.
     */
    public static final PrivateKey getPrivateKey() throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException
    {
        URI uri = null;
        try
        {
            uri = new URI( "" );
        }
        catch (URISyntaxException uriSyntaxException)
        {
            throw new IOException( "URISyntaxException: "
                    + uriSyntaxException.getMessage() );
        }
        return (getPrivateKey( uri ));
    }

    /**
     * Create the in-memory version of the Private Key from a file.
     * 
     * @param publicKeyFilename
     *            the URI of the file containing the private key.
     * @return A private key.
     */
    public static final PrivateKey getPrivateKey( URI privateKeyURI )
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        PrivateKey privateKey = null;

        // Open the private key file, and create an in-memory version of it.
        FileInputStream fileInputStream = new FileInputStream( new File(
                privateKeyURI ) );
        ByteArrayOutputStream privateKeyBoas = new ByteArrayOutputStream();
        int currByte = 0;
        while (-1 != (currByte = fileInputStream.read()))
        {
            privateKeyBoas.write( currByte );
        }
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( privateKeyBoas
                .toByteArray() );
        privateKeyBoas.close();
        //
        KeyFactory keyFactoryEngine = KeyFactory.getInstance( KEY_ALGORITHM );
        privateKey = keyFactoryEngine.generatePrivate( keySpec );

        return (privateKey);
    }

    public static final PublicKey getPublicKey() throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException
    {
        URI uri = null;
        try
        {
            uri = new URI( "" );
        }
        catch (URISyntaxException uriSyntaxException)
        {
            throw new IOException( "URISyntaxException: "
                    + uriSyntaxException.getMessage() );
        }
        return (getPublicKey( uri ));
    }

    /**
     * Create the in-memory version of the Public Key from a file.
     * 
     * @param publicKeyFilename
     *            the URI of the file containing the public key.
     * @return A public key.
     */
    public static final PublicKey getPublicKey( URI publicKeyURI )
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        PublicKey publicKey = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        FileInputStream fileInputStream = new FileInputStream( new File(
                publicKeyURI ) );

        int curByte = 0;
        while ((curByte = fileInputStream.read()) != -1)
        {
            baos.write( curByte );
        }

        byte[] fileData = baos.toByteArray();
        baos.close();

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec( fileData );
        KeyFactory keyFactoryEngine = KeyFactory.getInstance( KEY_ALGORITHM );
        publicKey = keyFactoryEngine.generatePublic( keySpec );

        return (publicKey);
    }

}