/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.BufferedReader;
import java.io.FileReader;
 

/**
 *
 * @author demo
 */
import java.io.IOException;
public class Searcher {

    /*
     * load external WSML
     */
    private String loadWSML() {
        String wsmlString = null;

        try {
            BufferedReader in = new BufferedReader(new FileReader("test.wsml"));
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

    public String find() {
        String wsml = loadWSML();
        return achieveGoalText(wsml);
    }

    private static String achieveGoalText(java.lang.String wsml) {
        nubisave.client.ClientAccessService service = new nubisave.client.ClientAccessService();
        nubisave.client.ClientAccess port = service.getClientAccess();

        return port.achieveGoalText(wsml);
    }
}