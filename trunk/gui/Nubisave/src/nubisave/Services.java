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
        cstmMntPnts = new LinkedList<CustomMntPoint > ();
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
}
