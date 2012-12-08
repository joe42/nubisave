package org.jigdfs.serivce;

import java.util.EventObject;

public class ServiceEvent extends EventObject {

    private ServiceResponseMsg serviceResponseMsg = null;
    /**
     * 
     */
    private static final long serialVersionUID = -7254048369983067435L;

    public ServiceEvent(Object source, ServiceResponseMsg serviceResponseMsg) {
	super(source);
	this.serviceResponseMsg = serviceResponseMsg;
    }

    /**
     * @param serviceResponseMsg the serviceResponseMsg to set
     */
    public void setServiceResponseMsg(ServiceResponseMsg serviceResponseMsg) {
	this.serviceResponseMsg = serviceResponseMsg;
    }

    /**
     * @return the serviceResponseMsg
     */
    public ServiceResponseMsg getServiceResponseMsg() {
	return serviceResponseMsg;
    }

}
