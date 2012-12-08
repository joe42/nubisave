package org.jigdfs.jxta.wrapper;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.jigdfs.baseInterface.Listenable;
import org.jigdfs.baseInterface.Listener;
import org.jigdfs.jxta.exception.JXTANotInitializedException;
import org.jigdfs.serivce.Service;

/**
 * The ServiceThreadWrapper wraps an service and make it runnable in a thread It
 * must be thread-safe
 * 
 * @author jbian
 * 
 */

public class ServiceThreadedWrapper implements Runnable, Listenable {

    private Object result = null;

    private boolean isRunning = false;

    private Service service = null;

    public ServiceThreadedWrapper(Service service) {
	this.service = service;
    }
    
    private List<Listener> listenerList = new ArrayList<Listener>();

    @Override
    public void run() {
	if (this.service == null) {
	    try {
		throw new JXTANotInitializedException(
			"the service has not been initialized!");
	    } catch (JXTANotInitializedException e) {
		e.printStackTrace();
		return;
	    }
	}

	/*
	 * synchronized on the class, there is only one NetPeerGroup can be used for search 
	 * */
	synchronized (ServiceThreadedWrapper.class) {

	    this.isRunning = true;

	    while (this.service.isRunning()) {
		try {
		    this.service.runService();
		} catch (InterruptedException iEx) {

		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	    this.result = this.service.getResult();

	    this.isRunning = false;
	}
    }

    /**
     * @param isRunning
     *            the isRunning to set
     */
    public void setRunning(boolean isRunning) {
	this.isRunning = isRunning;
    }

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
	return isRunning;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(Object result) {
	this.result = result;
    }

    /**
     * @return the result
     */
    public Object getResult() {
	return result;
    }


    @Override
    public void notifyListeners(EventObject event) {
	// TODO Auto-generated method stub
	
    }

}
