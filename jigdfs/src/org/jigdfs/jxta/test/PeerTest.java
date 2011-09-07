package org.jigdfs.jxta.test;

import java.io.IOException;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.core.JigDFSPeerNode;
import org.jigdfs.jxta.exception.JXTAInvalidParametersException;
import org.jigdfs.jxta.service.PeerGroupSearchService;
import org.jigdfs.serivce.ServiceEvent;
import org.jigdfs.serivce.ServiceListener;

public class PeerTest {
    private final static Logger logger = Logger.getLogger(PeerTest.class
	    .getName());
    
    public static void main(String[] args) throws JXTAInvalidParametersException, PeerGroupException, IOException{
	final JigDFSPeerNode jigDFSPeerNode = new JigDFSPeerNode("test-0", "i am test-0", "test-0", "iloveusm" );
	
	logger.info("create searchListener");
	
	final PeerGroupSearchService peerGroupSearchService = new PeerGroupSearchService("jigdfs-jxta-group", jigDFSPeerNode.getNetPeerGroup());
	
	ServiceListener searchListener = new ServiceListener(){
	    @Override
	    public void serviceFinishedEvent(ServiceEvent event) {
		logger.info("get an event " + event.getSource().getClass().getName());
		Object result = event.getServiceResponseMsg().getResult();
		
		if(result != null && result instanceof PeerGroup) {
		    PeerGroup jigDFSPeerGroup = (PeerGroup) result;		    
		   
		    logger.info("i found gorup " + jigDFSPeerGroup.getPeerGroupName());
		    jigDFSPeerNode.setJigDFSPeerGroup(jigDFSPeerGroup);
		    jigDFSPeerNode.joinJigDFSPeerGroup();
		    jigDFSPeerNode.startPublishSelf();
			
		    peerGroupSearchService.removeListener(this);
		}
		
		
	    }
	    
	};
	
	
	peerGroupSearchService.addListener(searchListener);
	
	
	
	new Thread(new Runnable(){
	    @Override
	    public void run() {
		try {
		    peerGroupSearchService.runService();
		} catch (PeerGroupException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}		
	    }
	    
	}).start();
	
	while(!peerGroupSearchService.isFound()){
	    try {
		logger.info("not found! keep waiting");
		Thread.sleep(10*1000);
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
	
    }
}
