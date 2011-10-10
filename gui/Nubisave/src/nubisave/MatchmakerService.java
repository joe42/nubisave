/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

/**
 *
 * @author demo
 */
public class MatchmakerService extends StorageService {

    
    private double ranking;
    private boolean satAvailability;
    private boolean satPricePerMonth;
    private boolean satNetworkBandwith;
    private boolean satResponseTime;
    private boolean satPricePerData;
    private boolean satMaxDownTime;
    
            
    public MatchmakerService(String name) {
        super(name);
        super.setType(StorageType.MATCHMAKER);
    }

    public double getRanking() {
        return ranking;
    }

    public void setRanking(double ranking) {
        this.ranking = ranking;
    }

    public boolean isSatAvailability() {
        return satAvailability;
    }

    public void setSatAvailability(boolean satAvailability) {
        this.satAvailability = satAvailability;
    }

    public boolean isSatMaxDownTime() {
        return satMaxDownTime;
    }

    public void setSatMaxDownTime(boolean satMaxDownTime) {
        this.satMaxDownTime = satMaxDownTime;
    }

    public boolean isSatNetworkBandwith() {
        return satNetworkBandwith;
    }

    public void setSatNetworkBandwith(boolean satNetworkBandwith) {
        this.satNetworkBandwith = satNetworkBandwith;
    }

    public boolean isSatPricePerData() {
        return satPricePerData;
    }

    public void setSatPricePerData(boolean satPricePerData) {
        this.satPricePerData = satPricePerData;
    }

    public boolean isSatPricePerMonth() {
        return satPricePerMonth;
    }

    public void setSatPricePerMonth(boolean satPricePerMonth) {
        this.satPricePerMonth = satPricePerMonth;
    }

    public boolean isSatResponseTime() {
        return satResponseTime;
    }

    public void setSatResponseTime(boolean satResponseTime) {
        this.satResponseTime = satResponseTime;
    }

    

    
}
