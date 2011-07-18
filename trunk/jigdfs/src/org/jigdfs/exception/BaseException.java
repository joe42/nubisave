package org.jigdfs.exception;

import org.apache.log4j.Logger;

//BaesException class borrowed from cleversafe.org Exception...
public class BaseException extends Exception {
	
	private static final long serialVersionUID = 8831273457330260940L;
	private static Logger logger = Logger.getLogger(BaseException.class.getName());
	
	public BaseException()
	   {
	      super();
	      if (logger.isTraceEnabled())
	    	  logger.trace("Exception", this);
	   }

	   public BaseException(String reason, Throwable cause)
	   {
	      super(reason == null ? "" : reason, cause);
	      if (logger.isTraceEnabled())
	    	  logger.trace("Exception: " + reason, this);
	   }

	   public BaseException(String reason)
	   {
	      super(reason);
	      if (logger.isTraceEnabled())
	    	  logger.trace("Exception: " + reason, this);
	   }

	   public BaseException(Throwable cause)
	   {
	      super(cause);
	      if (logger.isTraceEnabled())
	    	  logger.trace("Exception", this);
	   }

	   public String getMessage()
	   {
	      return super.getMessage();
	   }

	   public String toString()
	   {
	      StringBuffer fullMessage = new StringBuffer();

	      if (getMessage() != null)
	      {
	         fullMessage.append(getMessage());
	      }
	      else
	      {
	         fullMessage.append("(No message provided)");
	      }

	      if (getCause() != null)
	      {
	         fullMessage.append(", Caused by: ");
	         if (getCause().getMessage() != null)
	         {
	            fullMessage.append(getCause().getMessage());
	         }
	         else // better then nothing
	         {
	            fullMessage.append(getCause().getClass().getName());
	         }
	      }
	      return fullMessage.toString();
	   }
}
