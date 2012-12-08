
package org.jigdfs.ida.exception;

public class IDAInvalidSliceFormatException extends IDADecodeException
{
   private static final long serialVersionUID = -8047476317165271412L;

   
   public IDAInvalidSliceFormatException(String reason)
   {
      super(reason);
   }

   public IDAInvalidSliceFormatException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}
