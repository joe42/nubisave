/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.request;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import nubisave.MatchmakerService;
import nubisave.ui.AddServiceDialog;
import nubisave.Nubisave;

/**
 *
 * @author demo
 */
public class Searcher {


    public void find(AddServiceDialog dialog) {

        String goalWSML;

        GoalGenerator gen = new GoalGenerator();
        goalWSML = gen.generateGoalWSML(dialog);

        Parser parser = new Parser();
        try {
            List<MatchmakerService> results = parser.parseGoalText(achieveGoalText(goalWSML));
            dialog.setServices(results);
        } catch (Exception ex) {
            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String achieveGoalText(java.lang.String wsml) {
        nubisave.client.ClientAccessService service = new nubisave.client.ClientAccessService();
        nubisave.client.ClientAccess port = service.getClientAccess();

        String endpoint = Nubisave.properties.getProperty("matchmakerURI");
        BindingProvider binding = (BindingProvider) port;
        binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        System.out.println("Search: " + endpoint);

        return port.achieveGoalText(wsml);
    }
}
