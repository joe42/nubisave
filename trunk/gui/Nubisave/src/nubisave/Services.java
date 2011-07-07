/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author demo
 */
public class Services {

    private List<MatchmakerService> mmServices;
    private List<AgreementService> aServices;
    private List<CustomMntPoint> cstmMntPnts;

    public Services() {
        mmServices = new LinkedList<MatchmakerService>();
        aServices = new LinkedList<AgreementService>();
        cstmMntPnts = new LinkedList<CustomMntPoint>();
    }

    public List<AgreementService> getAServices() {
        return aServices;
    }

    public List<CustomMntPoint> getCstmMntPnts() {
        return cstmMntPnts;
    }

    public List<MatchmakerService> getMmServices() {
        return mmServices;
    }

    public StorageService get(int i) {
        if (i < 0) {
            return null;
        }
        int size = aServices.size() + cstmMntPnts.size() + mmServices.size();
        if (i >= size) {
            return null;
        }
        if (i < mmServices.size()) {
            return mmServices.get(i);
        } else if (i < mmServices.size() + aServices.size() - 1) { // Object is AgreementService
            i -= mmServices.size() - 1;
            return aServices.get(i);
        } else { // Object is CustomMntPoint
            i -= mmServices.size();
            i -= aServices.size();
            return cstmMntPnts.get(i);
        }
    }
}
