package org.jigdfs.jxta.utils;

import net.jxta.document.*;
import net.jxta.id.IDFactory;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Not actually an advertisement, but often acts as part of one.
 *
 * This internal class will eventually be removed. It has several
 * problems which make it difficult to support. (The most obvious that it
 * provides poor abstraction and provides references to its' own internal data
 * structures). This class is expected to be replaced by a public API class
 * performing a similar function though such an alternative is not yet available.
 * You are encouraged to copy this code into your own application or service if
 * if you depend upon it.
 *
 * Nanosek: class was moved into the myjxta repository
 */
public class StdPeerGroupParamAdv {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(StdPeerGroupParamAdv.class.getName());

    private static final String PARAM_TAG = "Parm";
    private static final String PROTO_TAG = "Proto";
    private static final String APP_TAG = "App";
    private static final String SVC_TAG = "Svc";
    private static final String MCID_TAG = "MCID";
    private static final String MSID_TAG = "MSID";
    private static final String MIA_TAG = ModuleImplAdvertisement.getAdvertisementType();

    // In the future we should be able to manipulate all modules regardless of
    // their kind, but right now it helps to keep them categorized as follows.

    /**
     * The services which will be loaded for this peer group.
     * <p/>
     * <ul>
     *     <li>Keys are {@link net.jxta.platform.ModuleClassID}.</li>
     *     <li>Values are {@link net.jxta.platform.ModuleSpecID} or
     *     {@link net.jxta.protocol.ModuleImplAdvertisement}.</li>
     * </ul>
     */
    private final Map<ModuleClassID, Object> services = new HashMap<ModuleClassID, Object>();

    /**
     * The protocols (message transports) which will be loaded for this peer
     * group.
     * <p/>
     * <ul>
     *     <li>Keys are {@link net.jxta.platform.ModuleClassID}.</li>
     *     <li>Values are {@link net.jxta.platform.ModuleSpecID} or
     *     {@link net.jxta.protocol.ModuleImplAdvertisement}.</li>
     * </ul>
     */
    private final Map<ModuleClassID, Object> transports = new HashMap<ModuleClassID, Object>();

    /**
     * The applications which will be loaded for this peer group.
     * <p/>
     * <ul>
     *     <li>Keys are {@link net.jxta.platform.ModuleClassID}.</li>
     *     <li>Values are {@link net.jxta.platform.ModuleSpecID} or
     *     {@link net.jxta.protocol.ModuleImplAdvertisement}.</li>
     * </ul>
     */
    private final Map<ModuleClassID, Object> apps = new HashMap<ModuleClassID, Object>();

    /**
     * Private constructor for new instances.
     */
    public StdPeerGroupParamAdv() {
    }

    /**
     * Private constructor for serialized instances.
     *
     * @param root the root element
     */
    public StdPeerGroupParamAdv(Element root) {
        if (!(root instanceof XMLElement)) {
            throw new IllegalArgumentException(getClass().getName() + " only supports XMLElement");
        }
        initialize((XMLElement) root);
    }

    /**
     * Return the services entries described in this Advertisement.
     * <p/>
     * The result (very unwisely) is the internal hashmap of this
     * Advertisement. Modifying it results in changes to this Advertisement.
     * For safety the Map should be copied before being modified.
     *
     * @return the services entries described in this Advertisement.
     */
    public Map<ModuleClassID, Object> getServices() {
        return services;
    }

    private void initialize(XMLElement doc) {

        if (!doc.getName().equals(PARAM_TAG)) {
            throw new IllegalArgumentException("Can not construct " + getClass().getName() + "from doc containing a " + doc.getName());
        }

        // set defaults
        int appCount = 0;
        Enumeration<XMLElement> modules = doc.getChildren();

        while (modules.hasMoreElements()) {
            XMLElement module = modules.nextElement();
            String tagName = module.getName();

            Map<ModuleClassID, Object> theTable;

            if (SVC_TAG.equals(tagName)) {
                theTable = services;
            } else if (APP_TAG.equals(tagName)) {
                theTable = apps;
            } else if (PROTO_TAG.equals(tagName)) {
                theTable = transports;
            } else {
                if (net.jxta.logging.Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Unhandled top-level tag : " + tagName);
                }
                continue;
            }

            ModuleSpecID specID = null;
            ModuleClassID classID = null;
            ModuleImplAdvertisement inLineAdv = null;

            try {
                if (module.getTextValue() != null) {
                    specID = (ModuleSpecID) IDFactory.fromURI(new URI(module.getTextValue()));
                }

                // Check for children anyway.
                Enumeration<XMLElement> fields = module.getChildren();

                while (fields.hasMoreElements()) {
                    XMLElement field = fields.nextElement();

                    String fieldName = field.getName();

                    if (MCID_TAG.equals(fieldName)) {
                        classID = (ModuleClassID) IDFactory.fromURI(new URI(field.getTextValue()));
                    } else if (MSID_TAG.equals(field.getName())) {
                        specID = (ModuleSpecID) IDFactory.fromURI(new URI(field.getTextValue()));
                    } else if (MIA_TAG.equals(field.getName())) {
                        inLineAdv = (ModuleImplAdvertisement) AdvertisementFactory.newAdvertisement(field);
                    } else {
                        if (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, "Unhandled field : " + fieldName);
                        }
                    }
                }
            } catch (Exception any) {
                if (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
                    LOG.log(Level.WARNING, "Broken entry; skipping", any);
                }
                continue;
            }

            if (inLineAdv == null && specID == null) {
                if (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("Insufficent entry; skipping");
                }
                continue;
            }

            Object theValue;

            if (inLineAdv != null) {
                specID = inLineAdv.getModuleSpecID();
                theValue = inLineAdv;
            } else {
                theValue = specID;
            }

            if (classID == null) {
                classID = specID.getBaseClass();
            }

            // For applications, the role does not matter. We just create a
            // unique role ID on the fly.
            // When outputing the adv we get rid of it to save space.

            if (theTable == apps) {
                // Only the first (or only) one may use the base class.
                if (classID == PeerGroup.applicationClassID) {
                    if (appCount++ != 0) {
                        classID = IDFactory.newModuleClassID(classID);
                    }
                }
            }
            theTable.put(classID, theValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(encodeAs, PARAM_TAG);

        outputModules(doc, services, SVC_TAG);
        outputModules(doc, transports, PROTO_TAG);
        outputModules(doc, apps, APP_TAG);

        return doc;
    }

    private void outputModules(StructuredDocument doc, Map<ModuleClassID, Object> modulesTable, String mainTag) {

        for (Map.Entry<ModuleClassID, Object> entry : modulesTable.entrySet()) {
            ModuleClassID mcid = entry.getKey();
            Object val = entry.getValue();
            Element m;

            // For applications, we ignore the role ID. It is not meaningfull,
            // and a new one is assigned on the fly when loading this adv.

            if (val instanceof Advertisement) {
                m = doc.createElement(mainTag);
                doc.appendChild(m);

                if (modulesTable != apps && !mcid.equals(mcid.getBaseClass())) {
                    // It is not an app and there is a role ID. Output it.
                    Element i = doc.createElement(MCID_TAG, mcid.toString());

                    m.appendChild(i);
                }

                StructuredDocument advdoc = (StructuredDocument) ((Advertisement) val).getDocument(doc.getMimeType());

                StructuredDocumentUtils.copyElements(doc, m, advdoc);
            } else if (val instanceof ModuleSpecID) {
                if (modulesTable == apps || mcid.equals(mcid.getBaseClass())) {
                    // Either it is an app or there is no role ID.
                    // So the specId is good enough.
                    m = doc.createElement(mainTag, val.toString());
                    doc.appendChild(m);
                } else {
                    // The role ID matters, so the classId must be separate.
                    m = doc.createElement(mainTag);
                    doc.appendChild(m);

                    Element i;

                    i = doc.createElement(MCID_TAG, mcid.toString());
                    m.appendChild(i);

                    i = doc.createElement(MSID_TAG, val.toString());
                    m.appendChild(i);
                }
            } else {
                if (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
                    LOG.warning("unsupported class in modules table");
                }
                throw new IllegalStateException("unsupported class in modules table : " + val);
            }
        }
    }
}
