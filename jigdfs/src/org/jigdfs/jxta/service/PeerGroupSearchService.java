package org.jigdfs.jxta.service;

import java.io.IOException;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.log4j.Logger;
import org.jigdfs.baseInterface.Listenable;
import org.jigdfs.jxta.utils.PeerGroupUtil;
import org.jigdfs.serivce.Service;
import org.jigdfs.serivce.ServiceEvent;
import org.jigdfs.serivce.ServiceListener;
import org.jigdfs.serivce.ServiceResponseMsg;

/**
 * PeerGroupSearchSerice, now it only supports search by either PeerGroupID or
 * by the group name, but in the future, it should support search by both
 * 
 * @author jbian
 * 
 */
public class PeerGroupSearchService extends Service implements Listenable {

    public enum SearchType {
	PeerGroupID("GID"), Name("Name"), Description("Desc"), MSID("MSID"); // [Name,
									     // GID,
									     // Desc,
									     // MSID]

	private String searchField = null;

	SearchType(String searchField) {
	    this.searchField = searchField;
	}

	/**
	 * @param searchField
	 *            the searchField to set
	 */
	public void setSearchField(String searchField) {
	    this.searchField = searchField;
	}

	/**
	 * @return the searchField
	 */
	public String getSearchField() {
	    return searchField;
	}
    }

    final private long WAIT_TIME = 10 * 1000;
    final private long MAX_TRY = 5;

    private static Logger logger = Logger
	    .getLogger(PeerGroupSearchService.class.getName());

    private boolean isFound = false;

    private Set<ServiceListener> serviceListenerList = new HashSet<ServiceListener>();

    private PeerGroup netPeerGroup = null;

    private PeerGroup appPeerGroup = null;

    private String searchTerm = null;

    /**
     * searchType, search by PeerGroupID or PeerGroup name
     */
    private SearchType searchType = null;

    /**
     * constructor of this service, search based on PeerGroupID
     * 
     * @param peerGroupID
     */
    public PeerGroupSearchService(PeerGroupID peerGroupID,
	    PeerGroup netPeerGroup) {
	this.searchTerm = peerGroupID.toString();
	this.searchType = SearchType.PeerGroupID;
	this.netPeerGroup = netPeerGroup;
    }

    /**
     * constructor of this service, search based on PeerGroup name
     * 
     * @param peerGroupName
     */
    public PeerGroupSearchService(String peerGroupName, PeerGroup netPeerGroup) {
	this.searchTerm = peerGroupName;
	this.searchType = SearchType.Name;
	this.netPeerGroup = netPeerGroup;
    }

    private void searchPeerGroup() throws PeerGroupException, IOException {
	if (this.netPeerGroup == null) {
	    logger.error("netPeerGroup is null!");

	    return;
	}

	// search for the group locally
	logger.debug("Searching for the group locally");

	List<PeerGroupAdvertisement> peerGroupAdvsList = PeerGroupUtil
		.getLocalPeerGroupAdvertisements(netPeerGroup, this.searchType
			.getSearchField(), this.searchTerm);

	if (peerGroupAdvsList != null && peerGroupAdvsList.size() > 0) {
	    if (logger.isTraceEnabled()) {
		logger.trace("JigDFS found in Local advertisement.");
	    }
	    this.appPeerGroup = netPeerGroup.newGroup(peerGroupAdvsList.get(0));

	    this.isFound = true;
	    ServiceResponseMsg response = new ServiceResponseMsg("PeerGroup "
		    + this.searchTerm + " is found!", this.appPeerGroup, this);
	    ServiceEvent serviceEvent = new ServiceEvent(this, response);
	    notifyListeners(serviceEvent);
	    return;

	} else {

	    logger.debug("No Group Found in Local advertisement.");
	    logger.debug("Starting Remote Search...");

	    DiscoveryListener peerGroupDiscoveryListener = new DiscoveryListener() {
		@Override
		public void discoveryEvent(DiscoveryEvent event) {

		    logger.debug("Got a Discovery Event");

		    Enumeration<Advertisement> res = event.getSearchResults();
		    if (res != null) {
			if (!res.hasMoreElements()) {
			    logger.error("empty search results...");
			}
			while (res.hasMoreElements()) {

			    Advertisement adv = res.nextElement();

			    logger.debug("Adv Type: "
				    + adv.getAdvType().toString());

			    if (adv instanceof PeerGroupAdvertisement) {
				PeerGroupAdvertisement peerGroupAdv = (PeerGroupAdvertisement) adv;

				logger
					.debug("Peer Group name from getSearchResults() = "
						+ peerGroupAdv.getName());

				try {
				    appPeerGroup = netPeerGroup
					    .newGroup(peerGroupAdv);
				    isFound = true;
				    ServiceResponseMsg response = new ServiceResponseMsg(
					    "PeerGroup "
						    + PeerGroupSearchService.this.searchTerm
						    + " is found!",
					    PeerGroupSearchService.this.appPeerGroup,
					    PeerGroupSearchService.this);
				    ServiceEvent serviceEvent = new ServiceEvent(
					    PeerGroupSearchService.this,
					    response);
				    notifyListeners(serviceEvent);

				} catch (PeerGroupException e) {
				    logger
					    .error("Exception while creating the PeerGroup"
						    + " from the received Peer Group Advertisement"
						    + e.getMessage());
				    e.printStackTrace();
				}

			    } else {

				logger
					.debug("The Received event is an instance of "
						+ adv.getClass()
						+ " and it will be ignored.");

			    }
			}
		    } else {
			logger.error("empty search results...");
		    }

		}

	    };

	    int count = 1;
	    while (!isFound) {
		if (logger.isTraceEnabled()) {
		    logger.trace("send a discory message, search for "
			    + this.searchType.getSearchField() + "="
			    + this.searchTerm);
		}
		PeerGroupUtil.discoverRemotePeerGroupAdvertisements(
			netPeerGroup, this.searchType.getSearchField(), "*"
				+ this.searchTerm + "*",
			peerGroupDiscoveryListener);
		try {
		    if (logger.isTraceEnabled()) {
			logger.trace("wait for " + WAIT_TIME);
		    }
		    Thread.sleep(WAIT_TIME);
		} catch (InterruptedException e) {

		    e.printStackTrace();
		}

		if (count > MAX_TRY) {
		    if (logger.isTraceEnabled()) {
			logger.trace("tried " + MAX_TRY
				+ " times, still nothing found!");
		    }

		    ServiceResponseMsg response = new ServiceResponseMsg(
			    "PeerGroup " + this.searchTerm + " is not found!",
			    null, this);
		    ServiceEvent serviceEvent = new ServiceEvent(this, response);
		    notifyListeners(serviceEvent);

		    break;
		}
	    }
	}
    }

    @Override
    public void runService() throws PeerGroupException, IOException {
	this.searchPeerGroup();
    }

    /**
     * @param searchType
     *            the searchType to set
     */
    public void setSearchType(SearchType searchType) {
	this.searchType = searchType;
    }

    /**
     * @return the searchType
     */
    public SearchType getSearchType() {
	return searchType;
    }

    /**
     * @param netPeerGroup
     *            the netPeerGroup to set
     */
    public void setNetPeerGroup(PeerGroup netPeerGroup) {
	this.netPeerGroup = netPeerGroup;
    }

    /**
     * @return the netPeerGroup
     */
    public PeerGroup getNetPeerGroup() {
	return netPeerGroup;
    }

    /**
     * @param appPeerGroup
     *            the appPeerGroup to set
     */
    public void setAppPeerGroup(PeerGroup appPeerGroup) {
	this.appPeerGroup = appPeerGroup;
    }

    /**
     * @return the appPeerGroup
     */
    public PeerGroup getAppPeerGroup() {
	return appPeerGroup;
    }

    /**
     * @return the isFound
     */
    public boolean isFound() {
	return isFound;
    }

    @Override
    public Object getResult() {
	return this.appPeerGroup;
    }

    public void addListener(ServiceListener listener) {
	this.serviceListenerList.add(listener);
    }

    public void removeListener(ServiceListener listener) {
	this.serviceListenerList.remove(listener);
    }

    /**
     * @return the serviceListenerList
     */
    public Set<ServiceListener> getServiceListenerList() {
	return serviceListenerList;
    }

    @Override
    public void notifyListeners(EventObject event) {
	for (ServiceListener l : this.serviceListenerList) {
	    l.serviceFinishedEvent((ServiceEvent) event);
	}
    }

}
