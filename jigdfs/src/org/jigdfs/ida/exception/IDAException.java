//Base Exception for all IDA exceptions

package org.jigdfs.ida.exception;

import org.jigdfs.exception.BaseException;

public abstract class IDAException extends BaseException {

	private static final long serialVersionUID = -8395584411924972673L;

	public IDAException(String reason) {
		super(reason);
	}

	public IDAException(String reason, Throwable cause) {
		super(reason, cause);
	}
}
