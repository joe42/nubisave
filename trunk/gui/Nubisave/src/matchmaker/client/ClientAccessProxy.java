package matchmaker.client;

public class ClientAccessProxy implements matchmaker.client.ClientAccess {
  private String _endpoint = null;
  private matchmaker.client.ClientAccess clientAccess = null;
  
  public ClientAccessProxy() {
    _initClientAccessProxy();
  }
  
  public ClientAccessProxy(String endpoint) {
    _endpoint = endpoint;
    _initClientAccessProxy();
  }
  
  private void _initClientAccessProxy() {
    try {
      clientAccess = (new matchmaker.client.ClientAccessServiceLocator()).getClientAccess();
      if (clientAccess != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)clientAccess)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)clientAccess)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (clientAccess != null)
      ((javax.xml.rpc.Stub)clientAccess)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public matchmaker.client.ClientAccess getClientAccess() {
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess;
  }
  
  public java.lang.String[] getWebServices(int domainID) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getWebServices(domainID);
  }
  
  public java.lang.String achieveGoalText(java.lang.String wsml) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalText(wsml);
  }
  
  public java.lang.String achieveGoalTextDyn(java.lang.String wsml, boolean dynsearch) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalTextDyn(wsml, dynsearch);
  }
  
  public java.lang.String achieveGoalIDDyn(int goalID) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalIDDyn(goalID);
  }
  
  public java.lang.String achieveGoalURL(java.lang.String url) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalURL(url);
  }
  
  public java.lang.String achieveGoalTextString(java.lang.String wsml) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalTextString(wsml);
  }
  
  public java.lang.String achieveGoalURLString(java.lang.String url) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.achieveGoalURLString(url);
  }
  
  public java.lang.String[] getWebServicesByDomainIRI(java.lang.String iri) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getWebServicesByDomainIRI(iri);
  }
  
  public java.lang.String getAllWebServices() throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getAllWebServices();
  }
  
  public java.lang.String getWebServicesFiltered(java.lang.String[] requiredDocs, java.lang.String username, java.lang.String domainIRI) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getWebServicesFiltered(requiredDocs, username, domainIRI);
  }
  
  public java.lang.String getAllDomains() throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getAllDomains();
  }
  
  public java.lang.String getDomainDetails(int domainID) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getDomainDetails(domainID);
  }
  
  public java.lang.String getReputationParametersByDomainIRI(java.lang.String iri) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getReputationParametersByDomainIRI(iri);
  }
  
  public java.lang.String getReputationParameters(java.lang.String iri) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getReputationParameters(iri);
  }
  
  public java.lang.String[] getInterfaceDocs(java.lang.String iri) throws java.rmi.RemoteException{
    if (clientAccess == null)
      _initClientAccessProxy();
    return clientAccess.getInterfaceDocs(iri);
  }
  
  
}