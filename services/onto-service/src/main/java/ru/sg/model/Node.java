package ru.sg.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

/**
 * The Node class provides container for ontology nodes (concepts).
 */
public class Node {
    JSONObject m_node;
    Onto m_onto;
    Hashtable<String, Object> m_storage;

    /**
     * Node constructor.
     */
    public Node(JSONObject node, Onto onto) {
        m_node = node;
        m_onto = onto;
        m_storage = new Hashtable<String, Object>();
    }

    /**
     * @return node unique identifier.
     */
    public int getID() {
        try {
            return m_node.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * @return node name.
     */
    public String getName() {
        try {
            return m_node.getString("name");
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * @param name - name of the attribute.
     * @return value of the attribute with given name stored exactly in this node.
     */
    public String getUniqueAttribute(String name) {
        try {
            return m_node.getJSONObject("attributes").getString(name);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * @param name - name of the attribute.
     * @return value of the attribute stored either in this node, or inherited from node's prototypes (upwards by is_a links).
     */
    public String getAttribute(String name) {
        return m_onto.getInheritedAttributeOfNode(this, name);
    }

    /**
     * @return internal storage of the node (as a dictionary), which can be used to assign arbitrary Java-objects to the node.
     * This may be useful during the reasoning, for exanple, to store some particular data in the instance of data source
     * (for future access via ontology) and so on.
     */
    public Hashtable<String, Object> getStorage() {
        return m_storage;
    }
}
