package fuse.zipfs;

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

}
