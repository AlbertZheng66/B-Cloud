
package com.xt.bcloud.td7;

/**
 * ������һ��ת���Ĺ���
 * @author Albert
 */
public class Process {
    /**
     * ������
     */
    private String oid;
    
    /**
     * �����Ŀͻ������Ӷ��󣬿�����ClientSocket��������������
     */
    private Object client;
    
    /**
     * ��ȡ��������Ϣ
     */
    private Message requestMessage = null;
    
    /**
     * ���Է���������Ӧ��Ϣ
     */
    private Message responseMessage = null;
    
    /**
     * 
     */
    private Object server;
    
    
}
