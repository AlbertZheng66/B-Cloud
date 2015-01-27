
package com.xt.bcloud.mdu;

import com.xt.core.app.Startable;
import com.xt.core.app.Starter;

/**
 * MDU 的启动程序，只是一个启动外壳而已。
 * @author Albert
 */
public class MduManagerStarter implements Startable{

    public MduManagerStarter() {
    }

    public boolean init() {
        return MduManager.getInstance().init();
    }

    public void start() {        
        MduManager.getInstance().start();
    }

    public void stop() {
        MduManager.getInstance().stop();
    }
    
    static public void main(String[] argv) {
        // -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml
        if (argv.length == 0) {
            argv = new String[]{"-l", "com.xt.gt.sys.loader.CommandLineSystemLoader",
                "-m", "CLIENT_SERVER",
                "-p", "local", "-f", "conf\\gt-config.xml"};
        }
        System.setProperty("starter.class", MduManagerStarter.class.getName());
        Starter.main(argv);
    }
    
}
