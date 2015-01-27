

package com.xt.bcloud.comm;

import java.io.Serializable;

/**
 * ��������һ����Դ������, ��� CPU ��Ƶ��, �ڴ�Ĵ�С����Ϣ.
 * @author albert
 */
public class Capacity implements Serializable {

    /**
     * ��С�ڴ棨��λ��M����Ĭ���ǣ�64M��
     */
    private int minMem = 64;

    /**
     * ����ڴ棨��λ��M����Ĭ���ǣ�256M��
     */
    private int maxMem = 256;

    public Capacity() {
    }

    public int getMaxMem() {
        return maxMem;
    }

    public void setMaxMem(int maxMem) {
        this.maxMem = maxMem;
    }

    public int getMinMem() {
        return minMem;
    }

    public void setMinMem(int minMem) {
        this.minMem = minMem;
    }
}
