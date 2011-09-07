package org.jigdfs.jxta.test;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.log4j.Logger;

import org.jigdfs.jxta.utils.JXTAIDFactory;
import org.jigdfs.jxta.utils.PeerGroupUtil;

public class CreatePeerGroup {
	private final static Logger logger = Logger.getLogger(CreatePeerGroup.class
			.getName());

	public static void main(String[] args) throws Exception {
		/*
		 * JigDFSPeerNode jigDFSPeerNode = new JigDFSPeerNode("test-1",
		 * "principal", "password");
		 * 
		 * final PeerGroup netPeerGroup = jigDFSPeerNode.getNetPeerGroup();
		 */
		NetworkManager networkManager =  new NetworkManager(NetworkManager.ConfigMode.ADHOC, "NetPeerGroup", new File(new File(".cache"), "NetPeerGroup").toURI());
		final PeerGroup netPeerGroup = networkManager.startNetwork();

		final PeerGroupID peerGroupID = JXTAIDFactory
				.createPeerGroupID("jigdfs-jxta-group");
		final PeerGroupAdvertisement peerGroupAdv = PeerGroupUtil.create(
				netPeerGroup, "jigdfs-jxta-group", "jigdfs-jxta-group",
				"whatever", 1000l, peerGroupID);
		
		logger.info(Arrays.asList(peerGroupAdv.getIndexFields()));
		
		final PeerGroup jigDFSPeerGroup = netPeerGroup.newGroup(peerGroupAdv);
		
		final DiscoveryListener peerDiscoveryListener = new DiscoveryListener(){

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

					    if (adv instanceof PeerAdvertisement) {
					    	PeerAdvertisement peerAdv = (PeerAdvertisement) adv;					    	
					    	logger.debug("i found Peer " + peerAdv.getName());
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
		
		final DiscoveryService jigDFSGroupDiscoveryService = jigDFSPeerGroup.getDiscoveryService();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					logger.info("send discovery message to search for peers!");
					jigDFSGroupDiscoveryService.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 10, peerDiscoveryListener);
					
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

		final DiscoveryService s = netPeerGroup.getDiscoveryService();
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					logger.info("publish the group!");

					try {
						s.publish(jigDFSPeerGroup.getAllPurposePeerGroupImplAdvertisement());
						s.remotePublish(jigDFSPeerGroup.getAllPurposePeerGroupImplAdvertisement());
						jigDFSPeerGroup.publishGroup("jigdfs-jxta-group",
								"jigdfs-jxta-group");
						Thread.sleep(20000);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

		}).start();
	}

}
