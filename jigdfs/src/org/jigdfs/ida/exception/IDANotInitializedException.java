package org.jigdfs.ida.exception;

public class IDANotInitializedException extends IDAException
{
   private static final long serialVersionUID = -3586042062577454929L;

   
   public IDANotInitializedException(String reason)
   {
      super(reason);
   }

   public IDANotInitializedException(String reason, Throwable cause)
   {
      super(reason, cause);
   }
}

