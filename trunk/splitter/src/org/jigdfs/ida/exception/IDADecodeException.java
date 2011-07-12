package org.jigdfs.ida.exception;

public abstract class IDADecodeException extends IDAException {

	private static final long serialVersionUID = 2166667129388317474L;

	public IDADecodeException(String reason) {
		super(reason);
	}

	public IDADecodeException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
