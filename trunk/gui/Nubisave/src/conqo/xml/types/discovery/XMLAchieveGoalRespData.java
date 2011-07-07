package conqo.xml.types.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains data of a discovery result.
 * 
 * @author Bastian Buder
 *
 */
public class XMLAchieveGoalRespData extends XMLRespData {

	/**
	 * Number of registered services in ConQo.
	 */
	private int servicesTotal = 0;
	
	
	/**
	 * Number of ranked (useful) services.
	 */
	private int rankedServices = 0;
	
	
	/**
	 * Taken time for functional discovery.
	 */
	private int funcDiscTime = 0;
	
	
	/**
	 * Taken time for nonfunctional discovery.
	 */
	private int nonFuncDiscTime = 0;
	
	
	/**
	 * Taken time for ranking.
	 */
	private int rankingTime = 0;
	
	
	/**
	 * Number of rejected interfaces by functional discovery.
	 */
	private int funcRejected = 0;
	
	
	/**
	 * Number of rejected interfaces by nonfunctional discovery.
	 */
	private int nonFuncRejected = 0;
	
	
	/**
	 * List of ranked (useful) interfaces.
	 */
	private List<InterfaceInfo> rankedInterfaces = new ArrayList<InterfaceInfo>();
	
	
	/**
	 * List of interfaces, which rejected by nonfunctional discovery.
	 */
	private List<InterfaceInfo> nfFailInterfaces = new ArrayList<InterfaceInfo>();
	
	
	/**
	 * Get the number of services which was rejected by functional discovery.
	 * 
	 * @return Number of services which was rejected by functional discovery
	 */
	public int getFuncRejected() {
		return funcRejected;
	}

	
	/**
	 * Set the number of services which was rejected by functional discovery.
	 * 
	 * @param funcRejected Number of services which was rejected by functional discovery
	 */
	public void setFuncRejected(int funcRejected) {
		this.funcRejected = funcRejected;
	}

	
	/**
	 * Get the number of services which was rejected by nonfunctional discovery. 
	 * 
	 * @return Nmber of services which was rejected by nonfunctional discovery
	 */
	public int getNonFuncRejected() {
		return nonFuncRejected;
	}

	
	/**
	 * Set the number of services which was rejected by nonfunctional discovery.
	 * 
	 * @param nonFuncRejected Nmber of services which was rejected by nonfunctional discovery
	 */
	public void setNonFuncRejected(int nonFuncRejected) {
		this.nonFuncRejected = nonFuncRejected;
	}

	
	/**
	 * Constructor
	 */
	public XMLAchieveGoalRespData() {
		super();
	}
	
	
	/**
	 * Get number of total services in matchmaker.
	 * 
	 * @return Number of total services in matchmaker
	 */
	public int getServicesTotal() {
		return servicesTotal;
	}
	
	
	/**
	 * Set number of total services in matchmaker.
	 * 
	 * @param servicesTotal Number of total services in matchmaker
	 */
	public void setServicesTotal(int servicesTotal) {
		this.servicesTotal = servicesTotal;
	}
	
	
	/**
	 * Get number of ranked services.
	 * 
	 * @return Number of ranked services
	 */
	public int getRankedServices() {
		return rankedServices;
	}
	
	
	/**
	 * Set number of ranked services.
	 * 
	 * @param rankedServices Number of ranked services
	 */
	public void setRankedServices(int rankedServices) {
		this.rankedServices = rankedServices;
	}

	
	/**
	 * Get taken time for functional discovery.
	 * 
	 * @return Taken time for functional discovery
	 */
	public int getFuncDiscTime() {
		return funcDiscTime;
	}

	
	/**
	 * Set taken time for functional discovery.
	 * 
	 * @param funcDiscTime Taken time for functional discovery
	 */
	public void setFuncDiscTime(int funcDiscTime) {
		this.funcDiscTime = funcDiscTime;
	}

	
	/**
	 * Get taken time for nonfunctional discovery.
	 * 
	 * @return Taken time for nonfunctional discovery
	 */
	public int getNonFuncDiscTime() {
		return nonFuncDiscTime;
	}

	
	/**
	 * Set taken time for nonfunctional discovery.
	 * 
	 * @param nonFuncDiscTime Taken time for nonfunctional discovery
	 */
	public void setNonFuncDiscTime(int nonFuncDiscTime) {
		this.nonFuncDiscTime = nonFuncDiscTime;
	}

	
	/**
	 * Get taken time for ranking.
	 * 
	 * @return Taken time for ranking
	 */
	public int getRankingTime() {
		return rankingTime;
	}

	
	/**
	 * Set taken time for ranking.
	 * 
	 * @param rankingTime Taken time for ranking
	 */
	public void setRankingTime(int rankingTime) {
		this.rankingTime = rankingTime;
	}
	
	
	/**
	 * Get ranked interfaces from discovery result.
	 * 
	 * @return Ranked interfaces
	 */
	public List<InterfaceInfo> getRankedInterfaces() {
		return rankedInterfaces;
	}
	
	
	/**
	 * Set ranked interfaces to discovery result. 
	 * 
	 * @param rankedInterfaces Ranked interfaces
	 */
	public void setRankedInterfaces(List<InterfaceInfo> rankedInterfaces) {
		this.rankedInterfaces = rankedInterfaces;
	}
	
	
	/**
	 * Add a ranked interface to discovery result.
	 * 
	 * @param iInfo Ranked interface
	 */
	public void addRankedInterface(InterfaceInfo iInfo) {
		this.rankedInterfaces.add(iInfo);
	}
	
	
	/**
	 * Get interfaces, which was rejected by nonfuntional disvovery.
	 * 
	 * @return Interface infos
	 */
	public List<InterfaceInfo> getNfFailInterfaces() {
		return nfFailInterfaces;
	}
	
	
	/**
	 * Set interfaces, which was rejected by nonfuntional disvovery.
	 * 
	 * @param nfFailInterfaces Interface infos
	 */
	public void setNfFailInterfaces(List<InterfaceInfo> nfFailInterfaces) {
		this.nfFailInterfaces = nfFailInterfaces;
	}
	
	
	/**
	 * Add interface, which was rejected by nonfuntional disvovery.
	 * 
	 * @param iInfo Interface info
	 */
	public void addNFFailInterface(InterfaceInfo iInfo) {
		this.nfFailInterfaces.add(iInfo);
	}
	
}
