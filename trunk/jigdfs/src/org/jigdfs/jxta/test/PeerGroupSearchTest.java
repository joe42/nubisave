package org.jigdfs.jxta.test;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.core.JigDFSPeerNode;
import org.jigdfs.jxta.exception.JXTAInvalidParametersException;
import org.jigdfs.jxta.service.PeerGroupSearchService.SearchType;
import org.jigdfs.jxta.utils.JXTAIDFactory;
import org.jigdfs.jxta.utils.PeerGroupUtil;

public class PeerGroupSearchTest {

    private final static Logger logger = Logger.getLogger(PeerGroupSearchTest.class
	    .getName());
    /**
     * @param args
     * @throws IOException 
     * @throws PeerGroupException 
     * @throws JXTAInvalidParametersException 
     */
    public static void main(String[] args) throws JXTAInvalidParametersException, PeerGroupException, IOException {
	/*JigDFSPeerNode jigDFSPeerNode = new JigDFSPeerNode("test-0", "principal", "password" );
	
	final PeerGroup netPeerGroup = jigDFSPeerNode.getNetPeerGroup();
	*/
	
	NetworkManager networkManager =  new NetworkManager(NetworkManager.ConfigMode.ADHOC, "NetPeerGroup",  new File(new File(".cache"), "NetPeerGroup").toURI());
	
	final PeerGroup netPeerGroup = networkManager.startNetwork();
	
	
	DiscoveryListener peerGroupDiscoveryListener = new DiscoveryListener() {
		@Override
		public void discoveryEvent(DiscoveryEvent event) {
		    
		    if (logger.isTraceEnabled()) {
			logger.trace("Got a Discovery Event");
		    }

		    Enumeration<Advertisement> res = event.getSearchResults();
		    if (res != null) {
			if (!res.hasMoreElements()) {
			    logger.error("empty search results...");
			}
			while (res.hasMoreElements()) {
			   
			    Advertisement adv = res.nextElement();
			    if (logger.isTraceEnabled()) {
				logger.trace("Adv Type: "
					+ adv.getAdvType().toString());
			    }

			    if (adv instanceof PeerGroupAdvertisement) {
				PeerGroupAdvertisement peerGroupAdv = (PeerGroupAdvertisement) adv;
				if (logger.isTraceEnabled()) {
				    logger
					    .trace("Peer Group name from getSearchResults() = "
						    + peerGroupAdv.getName());
				}
				try {
				    PeerGroup appPeerGroup = netPeerGroup.newGroup(peerGroupAdv);
				    logger.info("found " + appPeerGroup.getPeerGroupName());
				    logger.info("-----------------------------------------");
				    logger.info(appPeerGroup.getParentGroup().getPeerGroupName());
				    logger.info(appPeerGroup.getPeerGroupID());
				    logger.info("-----------------------------------------");
				    System.exit(0);
				    
				} catch (PeerGroupException e) {
				    logger
					    .error("Exception while creating the PeerGroup"
						    + " from the received Peer Group Advertisement"
						    + e.getMessage());
				    e.printStackTrace();
				}

			    } else {
				if (logger.isTraceEnabled()) {
				    logger
					    .trace("The Received event is an instance of "
						    + adv.getClass()
						    + " and it will be ignored.");
				}

			    }
			}
		    } else {
			logger.error("empty search results...");
		    }

		}

	    };
	    
	    final PeerGroupID peerGroupID = JXTAIDFactory
		.createPeerGroupID("jigdfs-jxta-group");
	    
	    final DiscoveryService s = netPeerGroup.getDiscoveryService();

	    long waittime = 6 * 1000L;
	    
	    while(true){
		try {
                    System.out.println("Sleeping for :" + waittime);
                    Thread.sleep(waittime);
                } catch (Exception e) {
                    // ignored
                }
                System.out.println("Sending a Discovery Message");
		s.getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 10, peerGroupDiscoveryListener);
	    }
	    //System.in.read();
	// PeerGroupUtil.discoverRemotePeerGroupAdvertisements(netPeerGroup, "NAME", "jigdfs-jxta-group", peerGroupDiscoveryListener);

    }

}
