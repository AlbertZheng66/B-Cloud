
package com.xt.bcloud.td7.impl;

import com.xt.bcloud.td7.Response;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * ʹ��Mina����Ϊ����������������
 * @author Albert
 */
public class MinaConnector extends AbstractBlockingConnector {

    @Override
    protected boolean isAsynchronized() {
        return false;
    }

    @Override
    protected Object listen() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected ByteBuffer readFromClient(Object client) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
}
