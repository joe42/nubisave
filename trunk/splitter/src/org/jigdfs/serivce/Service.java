package org.jigdfs.serivce;

import java.io.IOException;

import net.jxta.exception.PeerGroupException;

/**
 * Service base class
 * @author jbian
 *
 */
public abstract class Service { 
    
    public abstract Object getResult();
    
    /**
     * 
     */
    protected boolean isRunning = false;
        
    protected void setIsRunning(boolean isRunning){
	this.isRunning = isRunning;
    }
    
    public boolean isRunning() {
	return isRunning;
    }
    /**
     * run this service
     */
    public abstract void runService() throws Exception;
}
