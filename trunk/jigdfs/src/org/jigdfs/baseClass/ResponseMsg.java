package org.jigdfs.baseClass;

public abstract class ResponseMsg {
    final String resultMessage;
    final Object result;
    final Object source;
    
    public ResponseMsg(String resultMessage, Object result, Object source){
	this.resultMessage = resultMessage;
	this.result = result;	
	this.source = source;
    }
    
    public ResponseMsg(String resultMessage, Object result) {
	this.resultMessage = resultMessage;
	this.result = result;
	this.source = null;
    }
    
    public String getResultMessage(){
	return this.resultMessage;
    }
    
    public Object getResult(){
	return this.result;
    }
    
    public Object getSource(){
	return this.source;
    }
}
