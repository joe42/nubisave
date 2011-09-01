package com.github.joe42.splitter.util;

import java.nio.*;
import java.nio.charset.*;

public class StringUtil {
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

}
