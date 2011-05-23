/**
 * ClientAccess.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package matchmaker.client;

public interface ClientAccess extends java.rmi.Remote {
    public java.lang.String[] getWebServices(int domainID) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalText(java.lang.String wsml) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalTextDyn(java.lang.String wsml, boolean dynsearch) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalIDDyn(int goalID) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalURL(java.lang.String url) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalTextString(java.lang.String wsml) throws java.rmi.RemoteException;
    public java.lang.String achieveGoalURLString(java.lang.String url) throws java.rmi.RemoteException;
    public java.lang.String[] getWebServicesByDomainIRI(java.lang.String iri) throws java.rmi.RemoteException;
    public java.lang.String getAllWebServices() throws java.rmi.RemoteException;
    public java.lang.String getWebServicesFiltered(java.lang.String[] requiredDocs, java.lang.String username, java.lang.String domainIRI) throws java.rmi.RemoteException;
    public java.lang.String getAllDomains() throws java.rmi.RemoteException;
    public java.lang.String getDomainDetails(int domainID) throws java.rmi.RemoteException;
    public java.lang.String getReputationParametersByDomainIRI(java.lang.String iri) throws java.rmi.RemoteException;
    public java.lang.String getReputationParameters(java.lang.String iri) throws java.rmi.RemoteException;
    public java.lang.String[] getInterfaceDocs(java.lang.String iri) throws java.rmi.RemoteException;
}
