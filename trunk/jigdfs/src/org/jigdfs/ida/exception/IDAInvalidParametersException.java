
package org.jigdfs.ida.exception;

public class IDAInvalidParametersException extends IDAException
{
   private static final long serialVersionUID = 1837113005051941698L;

   
   public IDAInvalidParametersException(String reason)
   {
      super(reason);
   }

   public IDAInvalidParametersException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}
