

package com.xt.bcloud.comm;

/**
 * ��¼һ��Ӧ�õĶ˿ںŵ�ʹ�÷�Χ�����ڵ�����ʼ�˿ںţ�С�ڽ����˿ںš�
 * @author albert
 */
public class PortRange {

    private String name;

    /**
     * ��ʼ�˿ں�
     */
    private int startIndex;

    /**
     * ��ֹ�˿ں�
     */
    private int endIndex;

    public PortRange() {
    }

    public PortRange(String name, int startIndex, int endIndex) {
        this.name = name;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PortRange other = (PortRange) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("name=").append(name).append("; ");
        strBld.append("startIndex=").append(startIndex).append("; ");
        strBld.append("endIndex=").append(endIndex);
        strBld.append("]");
        return strBld.toString();
    }



    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
