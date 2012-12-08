package org.jigdfs.jxta.core;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.exception.JXTAInvalidParametersException;
import org.jigdfs.jxta.utils.PeerGroupUtil;
import org.jigdfs.jxta.utils.PeerNodeConfigurator;

import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;

public class JigDFSPeerNode {
    private final static Logger logger = Logger.getLogger(JigDFSPeerNode.class
	    .getName());

    final private PeerNodeConfigurator peerConfig;

    final private PeerGroup netPeerGroup;
    

    final private JigDFSJXTANetworkManager jigDFSJXTANetworkManager;

    private String peerName = null;

    private PeerGroup jigDFSPeerGroup = null;

    public JigDFSPeerNode(String peerName, String peerDescription,
	    String peerPrincipal, String peerPassword)
	    throws JXTAInvalidParametersException, IOException,
	    PeerGroupException {

	if (peerName.isEmpty() || peerPrincipal.isEmpty()
		|| peerPassword.isEmpty()) {
	    throw new JXTAInvalidParametersException(
		    "peerName, peerPrincipal, peerPassword, can't be null!");
	}

	this.peerName = peerName;

	JigDFSJXTANetworkManager.initJXTANetworkManager(peerName,
		peerDescription, peerPrincipal, peerPassword);
	jigDFSJXTANetworkManager = JigDFSJXTANetworkManager.getInstance();

	peerConfig = jigDFSJXTANetworkManager.getPeerNodeConfigurator();

	netPeerGroup = jigDFSJXTANetworkManager.getJigDFSNetPeerGroupFactory();

    }

    public void joinJigDFSPeerGroup() {
	if (this.jigDFSPeerGroup != null) {

	    logger.debug("joinning the PeerGroup: "
		    + this.jigDFSPeerGroup.getPeerGroupName());
	    logger.debug("peer name: " + this.jigDFSPeerGroup.getPeerName());
	    logger.debug("parent group name: "
		    + this.jigDFSPeerGroup.getParentGroup().getPeerGroupName());

	    PeerGroupUtil.joinToGroup(this.jigDFSPeerGroup); 
	    
	}
    }
    
    public void startPublishSelf(){
	
	final DiscoveryService s = jigDFSPeerGroup.getDiscoveryService();
	final PeerAdvertisement peerAdv = jigDFSPeerGroup.getPeerAdvertisement();
	
	new Thread(new Runnable(){
	    @Override
	    public void run() {
		while(true){
    		logger.debug("publish peerAdv about myself");
    		
    		try {
			s.publish(peerAdv);
			s.remotePublish(peerAdv);
			Thread.sleep(10 * 1000);
		    } catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    } catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
			break;
		    }
		}
	    }
	    
	}).start();
	
    }

    public String toString() {
	return "Peer Name: " + this.peerName + "; ";
    }

    /**
     * @return the peerName
     */
    public String getPeerName() {
	return peerName;
    }

    /**
     * @return the netPeerGroup
     */
    public PeerGroup getNetPeerGroup() {
	return netPeerGroup;
    }

    /**
     * @param jigDFSPeerGroup
     *            the jigDFSPeerGroup to set
     */
    public void setJigDFSPeerGroup(PeerGroup jigDFSPeerGroup) {
	this.jigDFSPeerGroup = jigDFSPeerGroup;
    }

    /**
     * @return the jigDFSPeerGroup
     */
    public PeerGroup getJigDFSPeerGroup() {
	return jigDFSPeerGroup;
    }

}
