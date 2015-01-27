
package com.xt.bcloud.worker;

import com.xt.bcloud.comm.Load;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author albert
 */
public class RanchMessage implements Serializable {
    
    private static final long serialVersionUID = -7436819372155213625L;

    /**
     * 存放所有牛的数组。
     */
    public static final String CATTLES_LOAD = "cattlesLoad";

    /**
     * 当前处理的“牛”，注册，怀疑和注销时将使用此参数传递
     */
    private Cattle cattle;

//    /**
//     * 按照负载的情况进行排序的“牛”
//     */
//    private List<Cattle> cattles;

    /**
     * 消息的操作
     */
    private Operator operator = Operator.REGISTER;

    /**
     * 当前应用实例或者当前实例组的负载
     */
    private Load load;

    /**
     * 定义属性
     */
    private Map<String, Serializable> attibutes;

    public RanchMessage() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RanchMessage other = (RanchMessage) obj;
        return (other == this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.cattle != null ? this.cattle.hashCode() : 0);
        //hash = 53 * hash + (this.cattles != null ? this.cattles.hashCode() : 0);
        hash = 53 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append(super.toString()).append("[");
        strBld.append("cattle=").append(cattle).append("; ");
        strBld.append("load=").append(load).append("; ");
        strBld.append("operator=").append(operator).append("; ");
        //strBld.append("cattles=").append(cattles);
        strBld.append("]");
        return strBld.toString();
    }



    public Cattle getCattle() {
        return cattle;
    }

    public void setCattle(Cattle cattle) {
        this.cattle = cattle;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

//    public List<Cattle> getCattles() {
//        return cattles;
//    }
//
//    public void setCattles(List<Cattle> cattles) {
//        this.cattles = cattles;
//    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public Serializable getAttibute(String name) {
        if (name == null || this.attibutes == null) {
            return null;
        }
        return this.attibutes.get(name);
    }

    public void setAttibute(String name, Serializable value) {
        if (name == null || value == null) {
            return;
        }
        if (this.attibutes == null) {
            this.attibutes = new HashMap<String, Serializable>();
        }
        this.attibutes.put(name, value);
    }


}
