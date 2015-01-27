
package com.xt.bcloud.td7;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Albert
 */
public class Header {
    
    public final List<String> lines = new ArrayList();
    
    /**
     * 头信息（Headers）
     */
    protected final Map<String, String> fields = new LinkedHashMap();
        
    /**
     * 原始上传头
     */
    private Buffers originalHeader;
    
    private String host;

    public List<String> getLines() {
        return lines;
    }

    public void addLine(String line) {
        if (line != null) {
            this.lines.add(line);
        }
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void appendOriginalHeader(ByteBuffer originalHeader) {
        if (originalHeader != null) {
            this.originalHeader.append(originalHeader);
        }
    }

    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Buffers getOriginalHeader() {
        return originalHeader;
    }

    public void setOriginalHeader(Buffers originalHeader) {
        this.originalHeader = originalHeader;
    }
    
}
