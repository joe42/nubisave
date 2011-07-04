/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

/**
 *
 * @author demo
 */
public class CustomMntPoint extends StorageService {

    public String mntCmd;
    private String pass;
    private String user;

    public CustomMntPoint(String name) {
        super(name);
    }

    public String getMntCmd() {
        return mntCmd;
    }

    public void setMntCmd(String mntCmd) {
        this.mntCmd = mntCmd;
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
