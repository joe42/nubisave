package org.jigdfs.jxta.utils;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.core.JigDFSPeerNode;

import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;

public class PeerNodeConfigurator {
    private final static Logger logger = Logger.getLogger(JigDFSPeerNode.class
	    .getName());

    final private String peerName;
    final private String peerPrincipal;
    final private String peerPassword;
    final private String peerDescritpion;

    final private File configureFile;

    public PeerNodeConfigurator(String peerName, String peerDescritpion, String peerPrincipal,
	    String peerPassword) {
	this.peerName = peerName;
	this.peerDescritpion = peerDescritpion;
	this.peerPrincipal = peerPrincipal;
	this.peerPassword = peerPassword;
	this.configureFile = new File(new File(".jxtaConfig"), peerName);
    }

    public void configurePeer() throws IOException {
	NetworkConfigurator configurator = new NetworkConfigurator(
		NetworkConfigurator.EDGE_NODE, this.configureFile.toURI());
	if (configurator.exists()) {
	    if (logger.isTraceEnabled()) {
		logger.trace("local Config exist");
	    }

	} else {
	    NetworkManager networkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, peerName, configureFile.toURI());
            networkManager.setUseDefaultSeeds(true);
            networkManager.setConfigPersistent(true);
            
            configurator = networkManager.getConfigurator();
            
            configurator.setName(peerName);
            configurator.setPrincipal(peerPrincipal);
            configurator.setPassword(peerPassword);

	    
            configurator.save();

	}
	
    }

    
}
