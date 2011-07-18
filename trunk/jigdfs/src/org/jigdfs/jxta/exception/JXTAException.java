package org.jigdfs.jxta.exception;

import org.jigdfs.exception.BaseException;

public abstract class JXTAException extends BaseException {

	/**
	 * generated serialVersionUID;
	 */
	private static final long serialVersionUID = 3024954603701313929L;

	public JXTAException(String reason) {
		super(reason);
	}

	public JXTAException(String reason, Throwable cause) {
		super(reason, cause);
	}
}