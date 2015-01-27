

package com.xt.bcloud.resource;

import com.xt.bcloud.worker.Cattle;
import com.xt.core.db.pm.IPOPersistenceManager;
import org.jdom.Element;
import static com.xt.gt.sys.SystemConfiguration.*;
import static com.xt.bcloud.resource.arm.ArmConf.*;

/**
 *
 * @author albert
 */
public class ArmProvider  implements ServiceProvider{

    public void createConf(Element root, Cattle cattle, IPOPersistenceManager persistenceManager) {
        Element system = root.getChild(CONF_FILE_TAG_SYSTEM);
        if (system == null) {
            system = new Element(CONF_FILE_TAG_SYSTEM);
            root.addContent(system);
        }
        ProviderHelper.createSimpleNode(system, PARAM_PROTOCOL, PROTOCOL);
        ProviderHelper.createSimpleNode(system, PARAM_IP, IP);
        ProviderHelper.createSimpleNode(system, PARAM_PORT, String.valueOf(PORT));
        ProviderHelper.createSimpleNode(system, PARAM_CONTEXT, CONTEXT);
    }

}
