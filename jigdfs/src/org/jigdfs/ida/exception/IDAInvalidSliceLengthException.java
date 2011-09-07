
package org.jigdfs.ida.exception;

public class IDAInvalidSliceLengthException extends IDADecodeException
{
   private static final long serialVersionUID = -6630696463481056053L;

   
   public IDAInvalidSliceLengthException(String reason)
   {
      super(reason);
   }

   public IDAInvalidSliceLengthException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}
