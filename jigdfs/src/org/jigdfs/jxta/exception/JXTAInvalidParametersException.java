package org.jigdfs.jxta.exception;

public class JXTAInvalidParametersException extends JXTAException
{
      
   /**
	 * 
	 */
	private static final long serialVersionUID = -4413016304505371550L;

public JXTAInvalidParametersException(String reason)
   {
      super(reason);
   }

   public JXTAInvalidParametersException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}
