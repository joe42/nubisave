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

    private String user;
    private String pass;
    
    public MatchmakerService(String name) {
        super(name);
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    

    
}
