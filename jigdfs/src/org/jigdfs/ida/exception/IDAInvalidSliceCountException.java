
package org.jigdfs.ida.exception;

public class IDAInvalidSliceCountException extends IDADecodeException
{
   private static final long serialVersionUID = -1530995737282100555L;

   
   public IDAInvalidSliceCountException(String reason)
   {
      super(reason);
   }

   public IDAInvalidSliceCountException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}
