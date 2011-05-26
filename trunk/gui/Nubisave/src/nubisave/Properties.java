/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

/**
 *
 * @author demo
 */
public class Properties {

    private String mntPoint;
    private String matchmakerURI;

    public String getMntPoint() {
        return mntPoint;
    }
    
    public void setMntPoint(String mntPoint) {
        this.mntPoint = mntPoint;
    }

    public String getMatchmakerURI() {
        return matchmakerURI;
    }
    
    public void setMatchmakerURI(String matchmakerURI) {
        this.matchmakerURI = matchmakerURI;
    }
    
    private Properties() {

        // default properties
        mntPoint = "/home/demo/nubisave";
        matchmakerURI = "http://localhost:8080/Matchmaker/services/ClientAccess";
    }

    public static Properties getInstance() {
        return PropertiesHolder.INSTANCE;
    }

    private static class PropertiesHolder {

        private static final Properties INSTANCE = new Properties();
    }
}
