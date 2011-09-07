package org.jigdfs.jxta.core;

import java.io.File;
import java.io.IOException;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.utils.PeerNodeConfigurator;

public class JigDFSJXTANetworkManager {
    private final static Logger logger = Logger
	    .getLogger(JigDFSJXTANetworkManager.class.getName());

    private static PeerGroup netPeerGroup = null;

    private static NetworkManager networkManager = null;
    
    private static JigDFSJXTANetworkManager jigDFSJXTANetworkManager = null;

    final private String peerName;
    final private String peerPrincipal;
    final private String peerPassword;
    final private String peerDescritpion;

    final private File configureFile;
    
    final private PeerNodeConfigurator peerNodeConfigurator;

    public PeerGroup getJigDFSNetPeerGroupFactory() throws IOException,
	    PeerGroupException {
	if (netPeerGroup != null) {
	    return netPeerGroup;
	}
	start();
	return netPeerGroup;
    }
    
    private JigDFSJXTANetworkManager(String peerName, String peerDescription, String peerPrincipal,
	    String peerPassword) throws IOException{
	this.peerName = peerName;
	this.peerDescritpion = peerDescription;
	this.peerPrincipal = peerPrincipal;
	this.peerPassword = peerPassword;
	this.configureFile = new File(new File(".jxtaConfig"), peerName);
	
	this.peerNodeConfigurator = new PeerNodeConfigurator(peerName, peerDescritpion, peerPrincipal, peerPassword);
	this.peerNodeConfigurator.configurePeer();	
	
    }
    
    public static void initJXTANetworkManager(String peerName, String peerDescription, String peerPrincipal,
	    String peerPassword) throws IOException {
	
	
	jigDFSJXTANetworkManager = new JigDFSJXTANetworkManager(peerName, peerDescription, peerPrincipal, peerPassword);
	
	
    }
    public static JigDFSJXTANetworkManager getInstance(){	
	return jigDFSJXTANetworkManager;
    }

    public void start() throws IOException, PeerGroupException {
	if (logger.isTraceEnabled()) {
	    logger
		    .trace("start the jxta network and get the default netpeergroup!");
	}
	networkManager = new NetworkManager(NetworkManager.ConfigMode.EDGE,
		this.peerName, configureFile.toURI());
	netPeerGroup = networkManager.startNetwork();
    }
    
    
    

    public void stop() {
	if (networkManager != null) {
	    networkManager.stopNetwork();
	    if (logger.isTraceEnabled()) {
		logger.trace("stop the jxta network...");
	    }
	}
    }

    /**
     * @return the peerName
     */
    public String getPeerName() {
	return peerName;
    }
    
    /**
     * @return the peerNodeConfigurator
     */
    public PeerNodeConfigurator getPeerNodeConfigurator() {
	return this.peerNodeConfigurator;
    }
    
    
}
