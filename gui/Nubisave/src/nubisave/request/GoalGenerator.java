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
        wsmogoal.addNameSpace("rank", "http://localhost:8080/Matchmaker/ontologies/Common/Ranking.wsml#");
        wsmogoal.addNameSpace("cloudstorage", "http://localhost:8080/Matchmaker/ontologies/CloudStorage.wsml#");
        wsmogoal.addNameSpace("qosbase", "http://localhost:8080/Matchmaker/ontologies/QoSBase.wsml#");
        wsmogoal.addNameSpace("contextbase", "http://localhost:8080/Matchmaker/ontologies/ContextBase.wsml#");
        wsmogoal.addNameSpace("remoteqosbase", "http://localhost:8080/Matchmaker/ontologies/RemoteQoSBase.wsml#");
        wsmogoal.addNameSpace("businessbase", "http://localhost:8080/Matchmaker/ontologies/BusinessBase.wsml#");
        wsmogoal.addNameSpace("wsml", "http://www.wsmo.org/wsml/wsml-syntax#");

        wsmogoal.addGoalOntologie("http://localhost:8080/Matchmaker/ontologies/CloudStorage.wsml#");

        wsmogoal.setCapability("postcondition definedBy ?serviceType memberOf cloudstorage#CloudStorage");

        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/CloudStorage.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/RemoteQoSBase.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/QoSBase.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/ContextBase.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/BusinessBase.wsml#");
        wsmogoal.addOntologie("http://localhost:8080/Matchmaker/ontologies/Common/Ranking.wsml#");

        String iname;
        iname = "instAvailability";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("remoteqosbase#Availability");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getAvailabilityTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Percentage");

        iname = "instPricePerPeriod";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("businessbase#PricePerPeriod");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getPricePerMonthTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Euro");

        iname = "instPricePeriod";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("businessbase#PricePeriod");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", "0.0");
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Hour");
        // FIXME: introduce dialog.getPricePeriodCB() and dialog.getPricePerPeriodUnitDB()

        iname = "instThroughput";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("remoteqosbase#Throughput");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getBandwidthTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getBandwidthUnitCB().getSelectedItem());

        iname = "instMaxDownTime";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("remoteqosbase#MaxDownTime");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getMaxDownTimeTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getMaxDownTimeUnitCB().getSelectedItem());

        iname = "instPricePerData";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("businessbase#PricePerData");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getPricePerDataTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#Euro");

        iname = "instResponseTime";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("remoteqosbase#ResponseTime");
        wsmogoal.getInstance(iname).addConcept("qosbase#GoalRequirement");
        wsmogoal.getInstance(iname).addParam("qosbase#value", dialog.getResponseTimeTextField().getText());
        wsmogoal.getInstance(iname).addParam("qosbase#unit", "qosbase#"+dialog.getResponseTimeUnitCB().getSelectedItem());

        iname = "instResponseTimeConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "remoteqosbase#ResponseTime");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getResponseTimeWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getResponseTimeCheckBox().isSelected()?"1.0":"0.0");

        iname = "instPricePerPeriodConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "businessbase#PricePerPeriod");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getPricePerMonthWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerMonthCheckBox().isSelected()?"1.0":"0.0");

        iname = "instPricePeriodConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "businessbase#PricePeriod");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getPricePerMonthWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerMonthCheckBox().isSelected()?"1.0":"0.0");

        iname = "instMaxDownTimeConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "remoteqosbase#MaxDownTime");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getMaxDownTimeWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerMonthCheckBox().isSelected()?"1.0":"0.0");

        iname = "instAvailabilityConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "remoteqosbase#Availability");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getAvailabilityWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getAvailabilityCheckBox().isSelected()?"1.0":"0.0");

        iname = "instPricePerDataConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "businessbase#PricePerData");
        wsmogoal.getInstance(iname).addParam("rank#hasWeight", dialog.getPricePerDataWeightCB().getSelectedIndex()+1+".0");
        wsmogoal.getInstance(iname).addParam("rank#hasMatchingThreshold", dialog.getPricePerDataCheckBox().isSelected()?"1.0":"0.0");

        iname = "instThroughputConfiguration";
        wsmogoal.addInstance(iname);
        wsmogoal.getInstance(iname).addConcept("rank#QoSConceptConfiguration");
        wsmogoal.getInstance(iname).addParam("rank#hasReputationScore", "1.0");
        wsmogoal.getInstance(iname).addParam("rank#hasQoSConceptIRI", "remoteqosbase#Throughput");
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
