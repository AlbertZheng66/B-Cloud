
package com.xt.bcloud.td;

import com.xt.bcloud.td.http.Request;
import com.xt.bcloud.worker.Cattle;
import java.util.List;

/**
 * ���������, ���ڽ�ָ�����������ض��ķ��������д���.
 * �˷���������һ����״�ṹ�������˳�����ն����˳���𼶴������˺�Ľ����
 * ��������һ�����������д���
 * ����κ�һ��ʵ�ַ��ؿգ������׳��쳣���ˡ����󡱽��������������
 * Ӧ�ô˹������ĳ���������:��������, ���Է��������.
 * @author albert
 */
public interface TaskFilter {

    /**
     * ���ݵ�ǰ������,���˵õ��ɴ���������"������ʵ��"��������ص�ʵ��Ϊ�գ�
     * ��ʾ���ܴ��������ϵͳ�����������롰��������֮�С�
     * @param request ����ʵ������Ϊ�ա�
     * @param cattles ��ѡ�ġ�������ʵ����
     * @return ���
     */
    public List<Cattle> filter(Request request, List<Cattle> cattles);
}
