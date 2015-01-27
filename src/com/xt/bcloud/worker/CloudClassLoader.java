
package com.xt.bcloud.worker;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author albert
 */
public class CloudClassLoader extends URLClassLoader {

    public CloudClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
