package org.jigdfs.jxta.core;

import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.utils.JXTAIDFactory;
import org.jigdfs.jxta.utils.PeerGroupUtil;

public class JigDFSPeerGroupFactory {
    
    private final static Logger logger = Logger.getLogger(JigDFSPeerGroupFactory.class
	    .getName());
    
    private static PeerGroupID jigDFSPeerGroupID = null;
    private static PeerGroup jigDFSPeerGroup = null;
    
    static{
	jigDFSPeerGroupID = JXTAIDFactory.createPeerGroupID("jigdfs-jxta-group");
    }
    /*
    public static PeerGroup getJigDFSPeerGroup() throws Exception {
	if(jigDFSPeerGroup == null){
	    createJigDFSPeerGroup();
	}
	return jigDFSPeerGroup;
    }
    */
    public synchronized static PeerGroup createJigDFSPeerGroup(PeerGroup netPeerGroup) throws Exception{
	
	if(jigDFSPeerGroup != null) return jigDFSPeerGroup;	
	
	
		PeerGroupAdvertisement peerGroupAdv = PeerGroupUtil.create(
			netPeerGroup, "jigdfs-jxta-group", "jigdfs-jxta-group", null,
			1000l, jigDFSPeerGroupID); // no password group

		jigDFSPeerGroup = netPeerGroup.newGroup(peerGroupAdv);

		netPeerGroup.publishGroup(peerGroupAdv.getName(), peerGroupAdv
			.getDescription());

		
		// publishing new group advertisements
		// netPGDiscoveryService.publish(peerGroupAdv);
		// netPGDiscoveryService.remotePublish(peerGroupAdv);

		logger.info("New Peer Group Successfully created :-)");
		logger.info("Publishing new Group Advertisements.");
		logger.info("Group Information:");
		logger.info("[===========================]");
		logger.info("[+]Group Name: " + peerGroupAdv.getName() + "\n");
		logger.info("[+]Group ID:" + peerGroupAdv.getPeerGroupID().toString()
			+ "\n");
		logger.info("[+]Group Description: " + peerGroupAdv.getDescription()
			+ "\n");
		logger.info("[+]Group Module ID: "
			+ peerGroupAdv.getModuleSpecID().toString() + "\n");
		logger.info("[+]Advertisement Type: " + peerGroupAdv.getAdvType()
			+ "\n");
		logger.info("[===========================]\n");
		
		return jigDFSPeerGroup;
		   
    }
}
