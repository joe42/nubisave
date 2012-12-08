package org.jigdfs.jxta.exception;


public class JXTANotInitializedException  extends JXTAException {
   

    
    /**
     * 
     */
    private static final long serialVersionUID = -6061177153055954046L;

    public JXTANotInitializedException(String reason)
    {
       super(reason);
    }

    public JXTANotInitializedException(String reason, Throwable cause)
    {
       super(reason, cause);
    }
}
