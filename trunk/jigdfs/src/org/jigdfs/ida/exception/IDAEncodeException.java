/*
 * IDAEncodeException
 */

package org.jigdfs.ida.exception;

public abstract class IDAEncodeException extends IDAException {

	private static final long serialVersionUID = 8142128197279943406L;

	public IDAEncodeException(String reason) {
		super(reason);
	}

	public IDAEncodeException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
