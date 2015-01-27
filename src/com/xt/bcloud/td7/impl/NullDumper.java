
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Dumpable;
import java.nio.ByteBuffer;

/**
 *
 * @author Albert
 */
public class NullDumper  implements Dumpable {

    public NullDumper() {
    }

    public void open() {
        // do nothing...
    }

    public void write(ByteBuffer[] b) {
        // do nothing...
    }
    
    
    public void write(ByteBuffer b) {
        // do nothing...
    }
    

    public void close() {
        // do nothing...
    }

    public void setFilenamePattern(String fileNamePattern) {
    }

    
    
}
