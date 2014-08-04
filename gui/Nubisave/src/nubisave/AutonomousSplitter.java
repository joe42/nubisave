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
 *
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
     * We start at a redundancy of 1 and increase it by 10, until the desired availability is achieved.
     * 
     * @param desiredAvailability
     */
    public void findConfigurationForAvailability(double desiredAvailability) {
        int redundancy = 0; //start point
        Boolean hasReducedRedundancyInLastStep = null;
        int step = 10;
        int nrOfSwitchesBetweenRaisingAndDecreasingRedundancy = 0;
        while(true){
            setRedundancy(redundancy);
            System.out.println("AV:"+getAvailability());
            System.out.println("DAV:"+desiredAvailability);
            System.out.println("Red:"+getRedundancy());
            System.out.println("step:"+step);
            if(getAvailability() == desiredAvailability)
                break;
            if(getAvailability() < desiredAvailability){ // increase redundancy
                System.out.println("increasing AV:");
                
                //switch from reducing redundancy to increasing it:
                if(step < 0) {
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                    //We switched to increasing redundancy, because the current availability was less than the desired one.
                    //Make minimal steps, until redundancy is equal or greater than the desired availability.
                    step = 1;
                }
                if(redundancy >= 100) {
                    //We already increased redundancy in the last iteration, and redundancy is 100, so the availability cannot be increase anymore
                    if(hasReducedRedundancyInLastStep != null && ! hasReducedRedundancyInLastStep){                        
                        break;
                    }
                } else {
                    redundancy += step;
                }
                hasReducedRedundancyInLastStep = false;
            } else { // reduce redundancy
                
                System.out.println("decreasing AV:");
                //switch from increasing redundancy to reducing it:
                if(step > 0) {
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                    step = -1;
                }
                //Stop if we have already reduced redundancy once, and desiredAv has just been achieved the second time
                if(nrOfSwitchesBetweenRaisingAndDecreasingRedundancy > 2) {
                    break;
                }
                if(redundancy <= 0){
                    //We already reduced redundancy in the last iteration, and redundancy is 0, so the availability cannot be redunced anymore
                    if(hasReducedRedundancyInLastStep != null && hasReducedRedundancyInLastStep){
                        break;
                    }
                } else {
                    redundancy += step;
                }
                hasReducedRedundancyInLastStep = true;
            }
        }
    }
    
    /**
     * Find optimal configuration for the desired availability.
     * If possible, the configuration should achieve an availability equal or less than the desired availability.
     * We start at a redundancy of 1 and increase it by 10, until the desired availability is achieved.
     * 
     * @param desiredAvailability
     */
    public void findConfigurationForAvailabilityLessOrEqual(double desiredAvailability) {
        findConfigurationForAvailability(desiredAvailability);
        if(getAvailability() > desiredAvailability){
            int redundancy = getRedundancy();
            if(redundancy > 0)
                setRedundancy(redundancy - 1);
        }
    }

    /**
     * Find optimal configuration for the desired replication factor.
     * If possible, the configuration should achieve a replication factor equal or higher than the desired replication factor.
     * 
     * @param desiredReplicationFactor The desired redundancy factor - from 1, which equals no redundancy
     */
    public void findConfigurationForRedundancy(Double desiredReplicationFactor) {
        int redundancy = 0; //start point
        Boolean hasReducedRedundancyInLastStep = null;
        int step = 10;
        int nrOfSwitchesBetweenRaisingAndDecreasingRedundancy = 0;
        while(true){
            setRedundancy(redundancy);
            System.out.println("DRed:"+desiredReplicationFactor);
            System.out.println("Red:"+getRedundancy());
            System.out.println("step:"+step);
            if(getRedundancyFactor() == desiredReplicationFactor)
                break;
            if(getRedundancyFactor() < desiredReplicationFactor){ // increase redundancy
                System.out.println("increasing AV:");
                
                //switch from reducing redundancy to increasing it:
                if(step < 0) {
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                    //We switched to increasing redundancy, because the current availability was less than the desired one.
                    //Make minimal steps, until redundancy is equal or greater than the desired availability.
                    step = 1;
                }
                if(redundancy >= 100) {
                    //We already increased redundancy in the last iteration, and redundancy is 100, so the availability cannot be increase anymore
                    if(hasReducedRedundancyInLastStep != null && ! hasReducedRedundancyInLastStep){                        
                        break;
                    }
                } else {
                    redundancy += step;
                }
                hasReducedRedundancyInLastStep = false;
            } else { // reduce redundancy
                
                System.out.println("decreasing AV:");
                //switch from increasing redundancy to reducing it:
                if(step > 0) {
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                    step = -1;
                }
                //Stop if we have already reduced redundancy once, and desiredAv has just been achieved the second time
                if(nrOfSwitchesBetweenRaisingAndDecreasingRedundancy > 2) {
                    break;
                }
                if(redundancy <= 0){
                    //We already reduced redundancy in the last iteration, and redundancy is 0, so the availability cannot be redunced anymore
                    if(hasReducedRedundancyInLastStep != null && hasReducedRedundancyInLastStep){
                        break;
                    }
                } else {
                    redundancy += step;
                }
                hasReducedRedundancyInLastStep = true;
            }
        }
    }
    
    /**
     * Find optimal configuration for the desired replication factor.
     * If possible, the configuration should achieve a replication factor equal or less than the desired replication factor.
     * 
     * @param desiredReplicationFactor 
     */
    public void findConfigurationForRedundancyEqualOrLess(Double desiredReplicationFactor) {
        findConfigurationForRedundancy(desiredReplicationFactor);
        if(getRedundancyFactor() > desiredReplicationFactor){
            int redundancy = getRedundancy();
            if(redundancy > 0)
                setRedundancy(redundancy - 1);
        }
    }
    
    
    
}
