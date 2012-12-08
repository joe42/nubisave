package org.jigdfs.jxta.exception;

public class JXTAAlreadyInitializedException extends JXTAException {
    /**
     * 
     */
    private static final long serialVersionUID = -4717103229640784439L;

    public JXTAAlreadyInitializedException(String reason)
    {
       super(reason);
    }

    public JXTAAlreadyInitializedException(String reason, Throwable cause)
    {
       super(reason, cause);
    }
}
