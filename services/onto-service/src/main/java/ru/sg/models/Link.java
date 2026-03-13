package ru.sg.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Link class provides container for ontology relations.
 */
public class Link {
    JSONObject m_link;

    /**
     * Link constructor.
     */
    public Link(JSONObject link) {
        m_link = link;
    }

    /**
     * @return relation unique identifier.
     */
    public int getID() {
        try {
            return m_link.getInt("id");
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * @return unique identifier of the relation source node.
     */
    public int getSourceID() {
        try {
            return m_link.getInt("source_node_id");
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * @return unique identifier of the relation target node.
     */
    public int getTargetID() {
        try {
            return m_link.getInt("destination_node_id");
        } catch (JSONException e) {
            return -1;
        }
    }

    /**
     * @return relation name.
     */
    public String getName() {
        try {
            return m_link.getString("name");
        } catch (JSONException e) {
            return null;
        }
    }
}
