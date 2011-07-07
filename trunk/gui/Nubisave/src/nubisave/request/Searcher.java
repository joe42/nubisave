/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.request;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import nubisave.MatchmakerService;
import nubisave.ui.AddServiceDialog;


/**
 *
 * @author demo
 */
public class Searcher {

    
    /*
     * load external WSML
     * debugging
     */
    private String loadWSML() {
        String wsmlString = "";

        try {
            BufferedReader in = new BufferedReader(new FileReader("goal.wsml"));
            String line;
            while ((line = in.readLine()) != null) {
                if (wsmlString != null) {
                    wsmlString += "\n";
                }
                wsmlString += line;
            }
            in.close();
        } catch (IOException e) {
        }

        return wsmlString;
    }

    public void find(AddServiceDialog dialog) {
        
        String goalWSML;
        // if (dialog == null) {
        goalWSML = loadWSML();
//        } else {
//            GoalGenerator gen = new GoalGenerator();
//            goalWSML = gen.generateGoalWSML(dialog);
//        }

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

        String endpoint = nubisave.Properties.getProperty("matchmakerURI");
        BindingProvider binding = (BindingProvider) port;
        binding.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        System.out.println("Search: " + endpoint);

        return port.achieveGoalText(wsml);
    }

}
