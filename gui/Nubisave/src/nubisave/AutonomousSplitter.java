/*
 * Representation to abstract configuration of the actual Splitter module.
 */

package nubisave;

import com.github.joe42.splitter.util.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Ini;

/**
 * Implements methods for autonomous configuration of Nubisave's Splitter component.
 * Offers methods to find and set a configuration based on the properties desired by the user.
 * @author joe
 */
public class AutonomousSplitter extends Splitter {
    public AutonomousSplitter(String splitterMountpoint){
        super(splitterMountpoint);
    }
    public AutonomousSplitter(){
        super();
    }

    /**
     * Find optimal configuration for the desired availability.
     * If possible, the configuration should achieve an availability equal or higher than the desired availability.
     * 
     * @param desiredAvailability
     */
    public void findConfigurationForAvailability(double desiredAvailability) {
        double stop = desiredAvailability;
        increaseAvailability(10, stop);
        //Assume that we are at redundancy of i.e. 95 and can only increase it by using a smaller step size
        increaseAvailability(1, stop);
        //Assume that we are at redundancy of i.e. 3 and can only decrease it by using a smaller step size
        decreaseAvailability(5, stop);
        decreaseAvailability(1, stop);
        increaseAvailability(1, stop);
    }
    
    /**
     * Find optimal configuration for the desired availability.
     * If possible, the configuration should achieve an availability equal or less than the desired availability.
     * 
     * @param desiredAvailability
     */
    public void findConfigurationForAvailabilityLessOrEqual(double desiredAvailability) {
        double stop  = desiredAvailability;
        increaseAvailability(10, stop);
        //Assume that we are at redundancy of i.e. 95 and can only increase it by using a smaller step size
        increaseAvailability(1, stop);
        decreaseAvailability(5, stop);
        //Assume that we are at redundancy of i.e. 3 and can only decrease it by using a smaller step size
        decreaseAvailability(1, stop);
        increaseAvailability(1, stop);
        decreaseAvailability(1, stop);
    }

    /**
     * Find optimal configuration for the desired replication factor.
     * If possible, the configuration should achieve a replication factor equal or higher than the desired replication factor.
     * 
     * @param desiredReplicationFactor The desired redundancy factor - from 1, which equals no redundancy
     */
    public void findConfigurationForRedundancy(Double desiredReplicationFactor) {
        double stop = desiredReplicationFactor;
        increaseRedundancy(10, stop);
        //Assume that we are at redundancy of i.e. 95 and can only increase it by using a smaller step size
        increaseRedundancy(1, stop);
        decreaseRedundancy(5, stop);
        //Assume that we are at redundancy of i.e. 3 and can only decrease it by using a smaller step size
        decreaseRedundancy(1, stop);
        increaseRedundancy(1, stop);
    }
    
    /**
     * Find optimal configuration for the desired replication factor.
     * If possible, the configuration should achieve a replication factor equal or less than the desired replication factor.
     * 
     * @param desiredReplicationFactor 
     */
    public void findConfigurationForRedundancyEqualOrLess(Double desiredReplicationFactor) {
        double stop = desiredReplicationFactor;
        increaseRedundancy(10, stop);
        //Assume that we are at redundancy of i.e. 95 and can only increase it by using a smaller step size
        increaseRedundancy(1, stop); 
        decreaseRedundancy(5, stop);
        //Assume that we are at redundancy of i.e. 3 and can only decrease it by using a smaller step size
        decreaseRedundancy(1, stop);
        increaseRedundancy(1, stop);
        decreaseRedundancy(1, stop);
    }

    /**
     * Increase Nubisave's availability.
     * Stop when the availability is greater or equal limit, or
     * if availability cannot be increased anymore.
     * @param step step size to increase Nubisave's abstract redundancy parameter
     * @param limit 
     */
    private void increaseAvailability(int step, double limit) {
        int redundancy = getRedundancy();
        while(getAvailability() <= limit){
            redundancy += step;
            if(redundancy > 100){
                break;
            }
            setRedundancy(redundancy);
        }
    }
    
   /**
     * Decrease Nubisave's availability.
     * Stop when the availability is smaller or equal limit, or
     * if availability cannot be decreased anymore.
     * @param step step size to decrease Nubisave's abstract redundancy parameter
     * @param limit 
     */
    private void decreaseAvailability(int step, double limit) {
        int redundancy = getRedundancy();
        while(getAvailability() >= limit && redundancy-step > 0){
            redundancy -= step;
            if(redundancy < 0){
                break;
            }
            setRedundancy(redundancy);
        }
    }

    /**
     * Increase Nubisave's redundancy factor.
     * Stop when the redundancy factor is greater or equal limit, or
     * if the redundancy factor cannot be increased anymore.
     * @param step the step size to increase Nubisave's abstract redundancy parameter
     * @param limit 
     */
    private void increaseRedundancy(int step, double limit) {
        int redundancy = getRedundancy();
        while(getRedundancyFactor() <= limit && redundancy < 100){
            redundancy += step;
            if(redundancy > 100){
                break;
            }
            setRedundancy(redundancy);
        }
    }
    
   /**
     * Decrease Nubisave's redundancy factor.
     * Stop when the redundancy factor is smaller or equal limit, or
     * if the redundancy factor cannot be decreased anymore.
     * @param step the step size to decrease Nubisave's abstract redundancy parameter
     * @param limit 
     */
    private void decreaseRedundancy(int step, double limit) {
        int redundancy = getRedundancy();
        while(getRedundancyFactor()>= limit && redundancy > 0){
            redundancy -= step;
            if(redundancy < 0){
                break;
            }
            setRedundancy(redundancy);
        }
    }
    
    
    
}
