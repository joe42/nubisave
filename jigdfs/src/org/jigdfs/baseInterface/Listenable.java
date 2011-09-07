package org.jigdfs.baseInterface;

import java.util.EventObject;

public interface Listenable {
   void notifyListeners(EventObject event);
}
