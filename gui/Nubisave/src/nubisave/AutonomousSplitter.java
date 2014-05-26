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
        int redundancy = 1; //start point
        Boolean hasReducedRedundancyInLastStep = null;
        int step = 10;
        int nrOfSwitchesBetweenRaisingAndDecreasingRedundancy = 0;
        while(true){
            setRedundancy(redundancy);
            System.out.println("AV:"+getAvailability());
            System.out.println("DAV:"+desiredAvailability);
            System.out.println("Red:"+getRedundancy());
            System.out.println("step:"+step);
            if(getAvailability() < desiredAvailability){ // increase redundancy
                System.out.println("increasing AV:");
                
                //switch from reducing redundancy to increasing it:
                if(hasReducedRedundancyInLastStep != null && hasReducedRedundancyInLastStep) { 
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                }
                step = 1;
                //We already increased redundancy in the last iteration, and redundancy is 100, so the availability cannot be increase anymore
                if(redundancy >= 100 && hasReducedRedundancyInLastStep != null && ! hasReducedRedundancyInLastStep)
                    break;
                redundancy += step;
                hasReducedRedundancyInLastStep = false;
            } else { // reduce redundancy
                
                System.out.println("decreasing AV:");
                //switch from increasing redundancy to reducing it:
                if(hasReducedRedundancyInLastStep != null && ! hasReducedRedundancyInLastStep) { 
                    nrOfSwitchesBetweenRaisingAndDecreasingRedundancy += 1;
                }
                step = -1;
                //Stop if we have already reduced redundancy once, and desiredAv has just been achieved the second time
                if(nrOfSwitchesBetweenRaisingAndDecreasingRedundancy > 2) {
                    break;
                }
                //We already reduced redundancy in the last iteration, and redundancy is 0, so the availability cannot be redunced anymore
                if(redundancy <= 0 && hasReducedRedundancyInLastStep != null && hasReducedRedundancyInLastStep)
                    break;
                redundancy += step;
                hasReducedRedundancyInLastStep = true;
            }
        }
        //
        if(getAvailability() < desiredAvailability){
                if(hasReducedRedundancyInLastStep != null && hasReducedRedundancyInLastStep){
                    redundancy += 1;
                    setRedundancy(redundancy);
                }
        }
    }
    
}