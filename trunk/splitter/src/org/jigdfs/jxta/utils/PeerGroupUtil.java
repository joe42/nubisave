/*
*  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
*  reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions
*  are met:
*
*  1. Redistributions of source code must retain the above copyright
*  notice, this list of conditions and the following disclaimer.
*
*  2. Redistributions in binary form must reproduce the above copyright
*  notice, this list of conditions and the following disclaimer in
*  the documentation and/or other materials provided with the
*  distribution.
*
*  3. The end-user documentation included with the redistribution,
*  if any, must include the following acknowledgment:
*  "This product includes software developed by the
*  Sun Microsystems, Inc. for Project JXTA."
*  Alternately, this acknowledgment may appear in the software itself,
*  if and wherever such third-party acknowledgments normally appear.
*
*  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
*  must not be used to endorse or promote products derived from this
*  software without prior written permission. For written
*  permission, please contact Project JXTA at http://www.jxta.org.
*
*  5. Products derived from this software may not be called "JXTA",
*  nor may "JXTA" appear in their name, without prior written
*  permission of Sun.
*
*  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
*  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
*  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
*  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
*  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
*  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
*  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
*  SUCH DAMAGE.
*  ====================================================================
*
*  This software consists of voluntary contributions made by many
*  individuals on behalf of Project JXTA.  For more
*  information on Project JXTA, please see
*  <http://www.jxta.org/>.
*
*  This license is based on the BSD license adopted by the Apache Foundation.
*
*  $Id: PeerGroupUtil.java,v 1.7 2007/05/28 22:00:51 nano Exp $
*/

package org.jigdfs.jxta.utils;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.*;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.passwd.PasswdMembershipService;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.apache.log4j.Logger;
import org.jigdfs.jxta.core.JigDFSPeerNode;
import org.jigdfs.jxta.exception.JXTAInvalidParametersException;


/**
 * @author james todd [gonzo at jxta dot org]
 * @version $Id: PeerGroupUtil.java,v 1.7 2007/05/28 22:00:51 nano Exp $
 */

public class PeerGroupUtil {

    private final static Logger logger = Logger.getLogger(PeerGroupUtil.class
	    .getName());
    
    public static final String MEMBERSHIP_ID = "jigdfs-user";

    private static final long MILLISECONDS_IN_A_WEEK = 7 * 24 * 60 * 60 * 1000;

    /**
     * Create a new PeerGroupAdvertisment  from which a
     * new PeerGroup will be created.
     * <p/>
     * See "Create a Secure Peer Group" in
     * <a href="http://www.jxta.org/docs/JxtaProgGuide_v2.pdf">Jxta
     * Programmers Guide</a> for how to create a secure group. Most
     * of the code in this class is taken direcly from that chapter
     *
     * @param parentGroup the parent PeerGroup
     * @param name        the name of the new PeerGroup
     * @param description the description of the new PeerGroup
     * @param password    the password for the new Peergroup.
     *                    If it is null or an empty string this
     *                    peer group is not password protected
     * @return a new  PeerGroupAdvertisement
     */
    public static PeerGroupAdvertisement create(PeerGroup parentGroup, String name,
                                                String description, String password, long expiration)
            throws Exception {
        return create(parentGroup, name, description, password, expiration, null);
    }

    public static PeerGroupAdvertisement create(PeerGroup parentGroup, String name,
                                                String description, String password, long expiration, PeerGroupID id)
            throws Exception {
        PeerGroupAdvertisement pga;
        ModuleImplAdvertisement mia;
        boolean passProt = (password != null &&
                !password.trim().equals(""));

        // create the ModuleImplAdvertisement and publish it
        mia = parentGroup.getAllPurposePeerGroupImplAdvertisement();

        if (passProt) {
            createPasswordModuleImpl(mia);
        }

        parentGroup.getDiscoveryService().publish(mia);
        parentGroup.getDiscoveryService().remotePublish(mia);

        // create the PeerGroupAdvertisment and publish it
        pga = (PeerGroupAdvertisement) AdvertisementFactory.newAdvertisement(
                PeerGroupAdvertisement.getAdvertisementType());
        pga.setPeerGroupID(id != null ? id : IDFactory.newPeerGroupID());
        pga.setName(name);
        pga.setDescription(description);
        pga.setModuleSpecID(mia.getModuleSpecID());

        if (passProt) {
            StructuredTextDocument login =
                    (StructuredTextDocument)
                            StructuredDocumentFactory.newStructuredDocument(
                                    MimeMediaType.XMLUTF8, "Param");
            String loginString =
                    MEMBERSHIP_ID + ":" + PasswdMembershipService.makePsswd(password) +
                            ":";
            TextElement loginElement =
                    login.createElement("login", loginString);

            login.appendChild(loginElement);
            pga.putServiceParam(PeerGroup.membershipClassID, login);
        }

        DiscoveryService ds = parentGroup.getDiscoveryService();

        ds.publish(pga, expiration != 0 ?
                expiration : 2 * MILLISECONDS_IN_A_WEEK,
                expiration != 0 ? expiration : 2 * MILLISECONDS_IN_A_WEEK);
        ds.remotePublish(pga, expiration != 0 ?
                expiration : 2 * MILLISECONDS_IN_A_WEEK);

        return pga;
    }

    // indexed field [Name, GID, Desc, MSID]
    public static List<PeerGroupAdvertisement> getLocalPeerGroupAdvertisements(PeerGroup pg, String fieldName, String value) {
        List<PeerGroupAdvertisement> p = new ArrayList<PeerGroupAdvertisement>();

        try {
            for (Enumeration<Advertisement> gas = pg.getDiscoveryService().
                    getLocalAdvertisements(DiscoveryService.GROUP,
                    		(fieldName != null ? fieldName : null), value);
                 gas.hasMoreElements();) {
                Object o = gas.nextElement();

                if (o instanceof PeerGroupAdvertisement) {
                    p.add((PeerGroupAdvertisement) o);
                }
            }
        } catch (IOException ioe) {
        }

        return p;
    }

    public static void discoverRemotePeerGroupAdvertisements(PeerGroup pg, String fieldName, String value,
                                    DiscoveryListener listener) {
    	
        DiscoveryService s = pg.getDiscoveryService();

        s.getRemoteAdvertisements(null, DiscoveryService.GROUP,
        		(fieldName != null ? fieldName : null), value, 10, listener);
    }

    /**
     * Updates the ModuleImplAdvertisement  of the PeerGroupAdvertisement
     * to reflect the fact that we want to use the PasswordService in order
     * to manage the membership in this group
     *
     * @param mia the ModuleImplAdvertisement  to update
     */
    private static void createPasswordModuleImpl(ModuleImplAdvertisement mia)
            throws Exception {
        StdPeerGroupParamAdv stdPgParams = new StdPeerGroupParamAdv(mia.getParam());
        Map<ModuleClassID, Object> params = stdPgParams.getServices();
        boolean found = false;

        // loop until the MembershipService is found
        for (Iterator<ModuleClassID> pi = params.keySet().iterator();
             pi.hasNext() && !found;) {
            ModuleClassID serviceID = pi.next();

            if (serviceID.equals(PeerGroup.membershipClassID)) {
                // get the  Advertisement for the MembershipService
                ModuleImplAdvertisement memServices = (ModuleImplAdvertisement)
                        params.get(serviceID);

                // create a new Advertisement describing the password service
                ModuleImplAdvertisement newMemServices =
                        createPasswordServiceImpl(memServices);

                // update the services hashtable
                params.remove(serviceID);
                params.put(PeerGroup.membershipClassID, newMemServices);
                found = true;

                // and update the Service parameters list for the
                // ModuleImplAdvertisement
                mia.setParam((Element) stdPgParams.getDocument(
                        MimeMediaType.XMLUTF8));

                // change the ModuleSpecID since this
                if (!mia.getModuleSpecID().equals(
                        PeerGroup.allPurposePeerGroupSpecID)) {
                    mia.setModuleSpecID(IDFactory.newModuleSpecID(
                            mia.getModuleSpecID().getBaseClass()));
                } else {
                    ID passID = ID.nullID;

                    try {
                        passID = IDFactory.fromURI(new URI("urn", "jxta:uuid-" +
                                "DeadBeefDeafBabaFeedBabe00000001" +
                                "04" + "06", null));
                    } catch (URISyntaxException use) {
                        use.printStackTrace();
                    }

                    mia.setModuleSpecID((ModuleSpecID) passID);
                }
            }
        }
    }

    /**
     * Create the ModuleImplAdvertisement that describes the
     * PasswordService that this group is going to use
     *
     * @param template the previous ModuleImplAdvertisement that we use as
     *                 a template
     * @return the  ModuleImplAdvertisement that describes the
     *         PasswordService that this group is going to use
     */
    private static ModuleImplAdvertisement createPasswordServiceImpl(
            ModuleImplAdvertisement template) {
        ModuleImplAdvertisement passMember = (ModuleImplAdvertisement)
                AdvertisementFactory.newAdvertisement(
                        ModuleImplAdvertisement.getAdvertisementType());

        passMember.setModuleSpecID(PasswdMembershipService.passwordMembershipSpecID);
        passMember.setCode(PasswdMembershipService.class.getName());
        passMember.setDescription("Membership Services for MyJXTA");
        passMember.setCompat(template.getCompat());
        passMember.setUri(template.getUri());
        passMember.setProvider(template.getProvider());

        return passMember;
    }
    
    public static void joinToGroup(PeerGroup group) //This method will join to either found group or created group
    {
        StructuredDocument creds = null;
        
        logger.debug("Joining into " + group.getPeerGroupName() + "...");    
        
        
        try{
            //Athenticate and join to group
        AuthenticationCredential authCred = new AuthenticationCredential(group,null,creds);
        MembershipService membership = group.getMembershipService();
        Authenticator auth = membership.apply(authCred);
            if(auth.isReadyForJoin()){
                Credential myCred = membership.join(auth);
                
                logger.debug("===== Group Details =====");
                             
                
                StructuredTextDocument doc = (StructuredTextDocument)myCred.getDocument(new MimeMediaType("text/plain"));
                StringWriter out = new StringWriter();
                doc.sendToWriter(out);
                
                //System.out.println(out.toString());
                if(logger.isTraceEnabled()){
                    logger.trace(out.toString());
                    
                }
                
                //Publishing Peer Advertisements.
                DiscoveryService groupDiscoveryService = group.getDiscoveryService();
                
                logger.debug("Peer Name : " + group.getPeerName() + " is now online :-)");
                logger.debug("Obtaining SaEeDGroup Services.");
                logger.debug("Publishing Peer Advertisement.");
                
                
                groupDiscoveryService.publish(group.getPeerAdvertisement());
                groupDiscoveryService.remotePublish(group.getPeerAdvertisement());
                
                logger.debug("[===========================]\n");
            }
            else{
        	logger.fatal("[!!]Fatal Error: Cannot Join to The Group!");
                //System.exit(-1);
            }            
        }catch(Exception e){
            	logger.fatal("[!]Fatal Error: " + e.getMessage());
                e.printStackTrace();
                //System.exit(-1);
            }
    }
}
