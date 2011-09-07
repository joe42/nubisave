/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.request;

import java.util.Random;
import nubisave.ui.AddServiceDialog;

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

    public String generateGoalWSML(AddServiceDialog dialog) {
        String name = "Goal" + (new Random()).nextInt(100000000);
        wsmogoal.setName(name);
        wsmogoal.addNameSpace("own", "http://localhost:8080/Matchmaker/ontologies/goals/" + name + ".wsml#");
        wsmogoal.addNameSpace("dc", "http://purl.org/dc/elements/1.1#");
        wsmogoal.addNameSpace("rank", "http://localhost:8080/Matchmaker/ontologies/Common/Ranking.wsml#");
        wsmogoal.addNameSpace("cloudqos", "http://localhost:8080/Matchmaker/ontologies/CloudQoS.wsml#");
        wsmogoal.addNameSpace("qosbase", "http://localhost:8080/Matchmaker/ontologies/QoSBase.wsml#");
        wsmogoal.addNameSpace("wsml", "http://www.wsmo.org/wsml/wsml-syntax#");

        wsmogoal.addGoalOntologie("http://localhost:8080/Matchmaker/ontologies/CloudQoS.wsml#");
       
        wsmogoal.setCapability("postcondition definedBy ?serviceType memberOf cloudqos#CloudStorage");
        
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/CloudQoS.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/Common/Ranking.wsml#");
        
        String iname;
        iname = "instAvailability";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#Availability");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getAvailabilityTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Percentage");
        
        iname = "instPricePerMonth";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#PricePerMonth");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getPricePerMonthTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Euro");
        
        iname = "instNetworkBandwidth";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#NetworkBandwidth");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getBandwidthTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getBandwidthUnitCB().getSelectedItem());
        
        iname = "instMaxDownTime";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#MaxDownTime");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getMaxDownTimeTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getMaxDownTimeUnitCB().getSelectedItem());
        
        iname = "instPricePerData";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#PricePerData");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getPricePerDataTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Euro");
        
        iname = "instResponseTime";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("cloudqos#ResponseTime");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getResponseTimeTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getResponseTimeUnitCB().getSelectedItem());
        
        iname = "instResponseTimeConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#ResponseTime");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getResponseTimeWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getResponseTimeCheckBox().isSelected()?"1.0":"0.0");
        
        iname = "instPricePerMonthConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#PricePerMonth");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getPricePerMonthWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerMonthCheckBox().isSelected()?"1.0":"0.0");
        
        iname = "instMaxDownTimeConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#MaxDownTime");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getMaxDownTimeWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerMonthCheckBox().isSelected()?"1.0":"0.0");       
                
        iname = "instAvailabilityConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#Availability");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getAvailabilityWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getAvailabilityCheckBox().isSelected()?"1.0":"0.0");            
         
        iname = "instPricePerDataConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#PricePerData");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getPricePerDataWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerDataCheckBox().isSelected()?"1.0":"0.0");  
        
        iname = "instNetworkBandwidthConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "cloudqos#NetworkBandwidth");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getBandwidthWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getBandwidthCheckBox().isSelected()?"1.0":"0.0");
      
        iname = "ranking";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSRankingScoreThreshold");
        wsmogoal.getInstance(iname).addParam("rank#hasLowerRankThreshold", "0.15");
        wsmogoal.getInstance(iname).addParam("rank#hasHigherPartialScore", "3.0");
        wsmogoal.getInstance(iname).addParam("rank#hasEqualPartialScore","1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasLowerPartialScore", "0.0");
        wsmogoal.getInstance(iname).addParam("rank#hasHigherRankThreshold", "0.15");
        
        return wsmogoal.generateGoal();
    }

    public String getGoalWSML() {
        return wsmogoal.generateGoal();
    }
}
