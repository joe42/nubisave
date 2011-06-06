/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.request;

import java.util.HashMap;

/**
 *
 * @author demo
 */
public class GoalGenerator {

    String goalWSML;
    WSMOGoal wsmogoal;

    public GoalGenerator() {
        goalWSML = null;
        wsmogoal = new WSMOGoal();
    }

    public String generateGoalWSML(HashMap<String, String> QoS) {
        wsmogoal.addGoalOntologie("http://localhost:8080/Matchmaker/ontologies/Filehosting/FileQoSBase.wsml");
        /*
         * TODO: add QoS parameter from GUI
         */
        return wsmogoal.generateGoal();
    }

    public String getGoalWSML() {
        return wsmogoal.generateGoal();
    }
}
