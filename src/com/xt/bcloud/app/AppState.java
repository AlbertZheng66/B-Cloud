

package com.xt.bcloud.app;

import com.xt.core.conv.impl.Ab;

/**
 * ����һ��Ӧ�õ�״̬��
 * @author albert
 */
public enum AppState {
//    /**
//     * ��δע��ĳ���(ֻ��һ����ʾ״̬���ڳ����в�Ӧ����)
//     */
//    NONE,        

    /**
     * �Ѿ�ע�ᣬ���ǻ�û�з�������汾
     */
    @Ab("RE")
    REGISTERED,

    /**
     * ��������(��������һ���汾�������У����ϵͳֻ��һ���汾�����У��Ҵ��ڲ���״̬��
     * ��ʱӦ�õ�״̬Ϊ��ע�ᡱ���ߡ�ֹͣ��״̬)��
     */
    @Ab("R")
    RUNNING,

    /**
     * ������ͣ״̬��ע�⣺��ʱ���ڴ�Ӧ�õ�����ʵ���������У���
     * ���Խ�����Ӧ������Ϊ��ͣ״̬��Ҳ��������ΪӦ�ð汾������
     * ״̬��Ӱ��Ӧ�õ�״̬�����磺һ��Ӧ��ֻ��һ����ǰ���еİ汾��
     * ��ֻ��һ��ʵ�������������Ӧ�ð汾��������Ӧ�ô�����ͣ״̬�����ַ�ʽ�д���ϸ���ǣ�����
     */
    @Ab("P")
    PAUSED,

//   /**
//     * ��������ͣ״̬��ʵ������Ϊ�����С�״̬��
//     */
//    @Ab("RS")
//    RESUMED,

    /**
     * ��Ӧ���Ѿ�ֹͣ�ṩ����ϵͳ��ֹͣ��Ӧ�õ���������ʵ����
     */
    @Ab("S")
    STOPED,
    
    /**
     * Ӧ�ô��ڲ���״̬
     */
    @Ab("T")
    TESTING,

    /**
     * ��Ӧ���Ѿ����Ƴ���ϵͳ��ɾ����Ӧ�е�����������á�
     */
    @Ab("RM")
    REMOVED;

    @Override
    public String toString() {
        switch (this) {
            case REGISTERED:
                return "ע��";
            case RUNNING:
                return "����";
            case PAUSED:
                return "��ͣ";
            case TESTING:
                return "����";
            case STOPED:
                return "ֹͣ";
            case REMOVED:
                return "�Ƴ�";
        }
        return super.toString();
    }
}

