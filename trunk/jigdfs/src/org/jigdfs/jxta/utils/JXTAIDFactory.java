package org.jigdfs.jxta.utils;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;

public class JXTAIDFactory {
	private static Logger logger = Logger.getLogger(JXTAIDFactory.class
			.getName());

	private static final String SEED = "jigdfs-whatever";

	/**
	 * Given a peer name generates a Peer ID who's value is chosen based upon
	 * that name.
	 * 
	 * @param peerName
	 *            instance name
	 * @param peerGroupID
	 *            the group ID encoding
	 * @return The PeerID value
	 */
	public static PeerID createPeerID(final PeerGroupID peerGroupID, final String peerName) {
		// Use lower case to avoid any locale conversion inconsistencies
		String seed = (peerName + SEED).toLowerCase();

		byte[] digestByteArray = getSHA256Hash(seed);

		if (logger.isTraceEnabled()) {
			logger.trace("Peer: " + peerName + "; seed Hash Value: "
					+ new String(Hex.encode(digestByteArray)));
		}

		return IDFactory.newPeerID(peerGroupID, digestByteArray);
	}
	
	
	/**
     * Given a group name generates a Peer Group ID who's value is chosen based upon that name.
     *
     * @param groupName group name encoding value
     * @return The PeerGroupID value
     */
    public static PeerGroupID createPeerGroupID(final String groupName) {
        // Use lower case to avoid any locale conversion inconsistencies
    	String seed = (groupName + SEED).toLowerCase();

		byte[] digestByteArray = getSHA256Hash(seed);
		
		if (logger.isTraceEnabled()) {
			logger.trace("PeerGroup Name: " + groupName + "; seed Hash Value: "
					+ new String(Hex.encode(digestByteArray)));
		}
		
		
        return IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, digestByteArray);
    }
    
    
    private static byte[] getSHA256Hash(final String input){
    	 // Use lower case to avoid any locale conversion inconsistencies
    	String seed = (input + SEED).toLowerCase();

		byte[] seedByteArray = seed.getBytes();

		Digest digestFunc = new SHA256Digest();
		if (logger.isTraceEnabled()) {
			logger.trace("Digest Function: " + digestFunc.getAlgorithmName()
					+ "Digest Size: " + digestFunc.getDigestSize());
		}

		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];

		digestFunc.update(seedByteArray, 0, seedByteArray.length);

		digestFunc.doFinal(digestByteArray, 0);

		return seedByteArray;
    }
}
