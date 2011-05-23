/**
 * ClientAccessServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package matchmaker.client;

public class ClientAccessServiceLocator extends org.apache.axis.client.Service implements matchmaker.client.ClientAccessService {

    public ClientAccessServiceLocator() {
    }


    public ClientAccessServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ClientAccessServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ClientAccess
    private java.lang.String ClientAccess_address = "http://localhost:8080/Matchmaker/services/ClientAccess";

    public java.lang.String getClientAccessAddress() {
        return ClientAccess_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ClientAccessWSDDServiceName = "ClientAccess";

    public java.lang.String getClientAccessWSDDServiceName() {
        return ClientAccessWSDDServiceName;
    }

    public void setClientAccessWSDDServiceName(java.lang.String name) {
        ClientAccessWSDDServiceName = name;
    }

    public matchmaker.client.ClientAccess getClientAccess() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ClientAccess_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getClientAccess(endpoint);
    }

    public matchmaker.client.ClientAccess getClientAccess(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            matchmaker.client.ClientAccessSoapBindingStub _stub = new matchmaker.client.ClientAccessSoapBindingStub(portAddress, this);
            _stub.setPortName(getClientAccessWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setClientAccessEndpointAddress(java.lang.String address) {
        ClientAccess_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (matchmaker.client.ClientAccess.class.isAssignableFrom(serviceEndpointInterface)) {
                matchmaker.client.ClientAccessSoapBindingStub _stub = new matchmaker.client.ClientAccessSoapBindingStub(new java.net.URL(ClientAccess_address), this);
                _stub.setPortName(getClientAccessWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ClientAccess".equals(inputPortName)) {
            return getClientAccess();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://client", "ClientAccessService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://client", "ClientAccess"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ClientAccess".equals(portName)) {
            setClientAccessEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
