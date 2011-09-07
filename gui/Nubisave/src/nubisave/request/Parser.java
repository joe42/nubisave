/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.request;

import conqo.xml.parser.discovery.XMLAchieveGoalResponseParser;
import conqo.xml.types.discovery.InterfaceInfo;
import conqo.xml.types.discovery.Requirement;
import conqo.xml.types.discovery.XMLAchieveGoalRespData;
import java.util.LinkedList;
import java.util.List;
import nubisave.MatchmakerService;

/**
 *
 * @author demo
 */
public class Parser {

    public List<MatchmakerService> parseGoalText(String goalText) throws Exception {
        if (goalText == null) {
            return null;
        }

        XMLAchieveGoalResponseParser parser = new XMLAchieveGoalResponseParser(goalText);
        XMLAchieveGoalRespData respData = parser.parse();

        if (!respData.getStatus().equals("Success")) {
            return null;
        }

        List<InterfaceInfo> interfaces = respData.getRankedInterfaces();

        if (interfaces.isEmpty()) {
            return null;
        }

        List<MatchmakerService> results = new LinkedList<MatchmakerService>();

        for (InterfaceInfo inter : interfaces) {
            MatchmakerService mmService = new MatchmakerService(inter.getServiceLocalName());
            List<Requirement> reqms = inter.getGoalRequirements();
            for (Requirement reqm : reqms) {
                String paraName = reqm.getParameter();
                if (paraName.equals("Availability")) {
                    mmService.setSatAvailability(reqm.isSatisfied());
                } else if (paraName.equals("PricePerMonth")) {
                    mmService.setSatPricePerMonth(reqm.isSatisfied());
                } else if (paraName.equals("NetworkBandwidth")) {
                    mmService.setSatNetworkBandwith(reqm.isSatisfied());
                } else if (paraName.equals("ResponseTime")) {
                    mmService.setSatResponseTime(reqm.isSatisfied());
                } else if (paraName.equals("PricePerData")) {
                    mmService.setSatPricePerData(reqm.isSatisfied());
                } else if (paraName.equals("MaxDownTime")) {
                    mmService.setSatMaxDownTime(reqm.isSatisfied());
                }
            }
            results.add(mmService);
        }

        return results;
    }
}
