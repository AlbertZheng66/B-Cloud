/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xt.bcloud.td7;

import com.xt.bcloud.worker.Cattle;

/**
 * ���������Ӧ���и�д�����磺�����޸������У����µ��� Cookie����
 * ע�⣺����ӿ�Ϊ���̹߳��ã������Ҫ��֤���̰߳�ȫ��
 * @author albert
 */
public interface Rewritable {

    /**
     * ��д����
     * @param req  ����ʵ��,��Ϊ��.
     * @return  �޸ĺ����Ӧʵ��,������ؿ�,����Ϊ����δ���޸�.
     */
    public Request rewrite(Cattle cattle, Request req);

    /**
     * ���¸�д��Ӧ.
     * @param req ����ʵ��,��Ϊ��.
     * @param res ��Ӧʵ��,��Ϊ��.
     * @return �޸ĺ����Ӧʵ��,������ؿ�,����Ϊ��Ӧδ���޸�.
     */
    public Response rewrite(Cattle cattle, Request req, Response res);
}

