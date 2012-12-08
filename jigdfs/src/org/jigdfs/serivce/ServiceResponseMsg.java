package org.jigdfs.serivce;

import org.jigdfs.baseClass.ResponseMsg;

public class ServiceResponseMsg extends ResponseMsg {

    public ServiceResponseMsg(String resultMessage, Object result, Object source) {
	super(resultMessage, result, source);
    }
    
    public ServiceResponseMsg(String resultMessage, Object result) {
	super(resultMessage, result);	
    }    
}
