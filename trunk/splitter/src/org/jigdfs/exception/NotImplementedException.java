package org.jigdfs.exception;

import org.apache.log4j.Logger;

/**
 * Any feature that is not implemented should throw this exception or its
 * derived exception.
 * 
 */
public class NotImplementedException extends RuntimeException
{

   private static final long serialVersionUID = 2260199096341127562L;

   private static Logger _logger = Logger.getLogger(NotImplementedException.class);
   
   public NotImplementedException()
   {
      super("This feature is not implemented.");
      _logger.error("Exception: Unimplemented feature", this);
   }

   public NotImplementedException(String reason)
   {
      super(reason);
      _logger.error("Exception: " + reason, this);
   }
}
