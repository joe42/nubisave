/**
 * ClientAccessService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package matchmaker.client;

public interface ClientAccessService extends javax.xml.rpc.Service {
    public java.lang.String getClientAccessAddress();

    public matchmaker.client.ClientAccess getClientAccess() throws javax.xml.rpc.ServiceException;

    public matchmaker.client.ClientAccess getClientAccess(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
