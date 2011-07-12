package org.jigdfs.jxta.test;

import java.io.File;
import java.util.Arrays;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.utils.JXTAIDFactory;
import org.jigdfs.jxta.utils.PeerGroupUtil;

public class PeerGroupTest {
	private static Logger logger = Logger.getLogger(PeerGroupTest.class.getName());
	
	public static void main(String[] args) throws Exception {
		String peerName = "test";
		
		NetworkConfigurator localConfig = new NetworkConfigurator(
				NetworkConfigurator.EDGE_NODE, new File(new File(
						".jxtaConfig"), peerName).toURI());
		
		NetPeerGroupFactory factory = new NetPeerGroupFactory(
				(ConfigParams) localConfig.getPlatformConfig(), new File(
						new File(".jxtaConfig"), peerName).toURI());
		
		PeerGroup netPeerGroup = factory.getInterface();
		PeerGroupID peerGroupID = JXTAIDFactory.createPeerGroupID("jigdfs-jxta-group");
		PeerGroupAdvertisement peerGroupAdv = PeerGroupUtil.create(netPeerGroup, "jigdfs-jxta-group", "jigdfs-jxta-group", "whatever", 1000l, peerGroupID);
		
		
		
		logger.info(Arrays.asList(peerGroupAdv.getIndexFields()));

		

	}
}
