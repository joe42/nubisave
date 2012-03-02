package com.github.joe42.splitter.util;

import java.io.IOException;
import java.nio.*;
import java.nio.charset.*;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;
import javax.crypto.spec.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class StringUtil {
    private static final byte[] SALT = {
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };
	private static long string_uid = 0;
	public static String filterNonAscii(String inString) {
		// Create the encoder and decoder for the character encoding
		Charset charset = Charset.forName("US-ASCII");
		CharsetDecoder decoder = charset.newDecoder();
		CharsetEncoder encoder = charset.newEncoder();
		// This line is the key to removing "unmappable" characters.
		encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
		String result = inString;

		try {
			// Convert a string to bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(inString));

			// Convert bytes in a ByteBuffer to a character ByteBuffer and then to a string.
			CharBuffer cbuf = decoder.decode(bbuf);
			result = cbuf.toString();
		} catch (CharacterCodingException cce) {
			String errorMessage = "Exception during character encoding/decoding: " + cce.getMessage();
			//log.error(errorMessage, cce);
		}

		return result;	
	}
	public static String getUniqueAsciiString(String inString) {
		String res;
		res = filterNonAscii(inString)+string_uid++;
		return res;	
	}

	public static String getUTF8FromByteBuffer(ByteBuffer bb) {
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		try {
			return decoder.decode(bb).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ByteBuffer getByteBufferFromUTF8(String str) {
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		try {
			return encoder.encode(CharBuffer.wrap(str));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeUTF8StringToByteBuffer(String str, ByteBuffer bb) {
		Charset charset = Charset.forName("UTF-8");
		CharsetEncoder encoder = charset.newEncoder();
		CharBuffer in = CharBuffer.wrap(str);
		try {
			encoder.encode(in, bb, true);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		bb = null;
		return;
	}

    private static String encrypt(String text, String pwd) {
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey key = keyFactory.generateSecret(new PBEKeySpec(pwd.toCharArray()));
            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
            return base64Encode(pbeCipher.doFinal(text.getBytes()));
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private static String base64Encode(byte[] bytes) {
        // NB: This class is internal, and you probably should use another impl
        return new BASE64Encoder().encode(bytes);
    }

    private static String decrypt(String text, String pwd) {
        try {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(pwd.toCharArray()));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(text)));
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    private static byte[] base64Decode(String property) {
        try {
            // NB: This class is internal, and you probably should use another impl
            return new BASE64Decoder().decodeBuffer(property);
        } catch (IOException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new byte[0];
    }

}
