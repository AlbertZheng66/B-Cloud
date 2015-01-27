package com.xt.bcloud.resource;

import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import static com.xt.gt.sys.SystemConfiguration.*;

/**
 *
 * @author albert
 */
public class ProviderHelper {

    public static final String TAG_PROCESSOR_FACTORIES = "processorFactories";

    static public Element createListNode(Element parent, String paramName) {
        return createParamNode(parent, paramName, PAPAM_TYPE_LIST);
    }

    static public Element createSimpleNode(Element parent, String paramName, String paramValue) {
        Element element = new Element(CONF_FILE_TAG_PARAM);
        element.setAttribute(CONF_FILE_TAG_PARAM_NAME, paramName);
        element.setAttribute(CONF_FILE_TAG_PARAM_VALUE, paramValue);
        parent.addContent(element);
        return element;
    }

    static public Element createParamNode(Element parent, String paramName, String paramType) {
        Element paramNode = new Element(CONF_FILE_TAG_PARAM);
        paramNode.setAttribute(CONF_FILE_TAG_PARAM_NAME, paramName);
        paramNode.setAttribute(CONF_FILE_TAG_PARAM_TYPE, paramType);
        paramNode.setAttribute(CONF_FILE_TAG_COLLISION, CONF_FILE_TAG_COLLISION_MERGE);
        parent.addContent(paramNode);
        return paramNode;
    }

    static public Element getChild(Element parent, String name) {
        List children = parent.getChildren(CONF_FILE_TAG_PARAM);
        if (children != null && !children.isEmpty()) {
            for (Iterator it = children.iterator(); it.hasNext();) {
                Element child = (Element) it.next();
                String attrName = child.getAttributeValue(CONF_FILE_TAG_PARAM_NAME);
                if (name.equals(attrName)) {
                    return child;
                }
            }
        }
        return null;
    }
}
