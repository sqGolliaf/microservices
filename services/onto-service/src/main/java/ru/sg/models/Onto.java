package ru.sg.models;

import org.json.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * The Onto class provides container to store ontology.
 */
public class Onto {
    JSONObject m_onto;
    Node[] m_nodes;
    Link[] m_links;

    /**
     * Onth constructor.
     * @param ontoFile - input file stream containing the ontology in ONT format.
     * The content of file is going to be parsed as JSON.
     */
    public Onto(InputStream ontoFile) throws IOException, JSONException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(ontoFile));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }
        String ontoString = sb.toString();
        m_onto = new JSONObject(ontoString);

        JSONArray nodes = m_onto.getJSONArray("nodes");
        int n = nodes.length();
        m_nodes = new Node[n];
        for (int i = 0; i < n; ++i) {
            m_nodes[i] = new Node(nodes.getJSONObject(i), this);
        }

        JSONArray links = m_onto.getJSONArray("relations");
        n = links.length();
        m_links = new Link[n];
        for (int i = 0; i < n; ++i) {
            m_links[i] = new Link(links.getJSONObject(i));
        }
    }

    /**
     * @return array of ontology nodes.
     */
    public Node[] getNodes() {
        return m_nodes;
    }

    /**
     * @return array of ontology relations (all the links bewteen nodes, which exist in the ontology).
     */
    public Link[] getLinks() {
        return m_links;
    }

    /**
     * @param id - unique identifier of node.
     * @return node with given unique identifier. If no one node matches the given identifier, null is returned.
     */
    public Node getNodeByID(int id) {
        for (int i = 0; i < m_nodes.length; ++i) {
            if (m_nodes[i].getID() == id) {
                return m_nodes[i];
            }
        }
        return null;
    }

    /**
     * @param id - unique identifier of relation.
     * @return relation with given unique identifier. If no one relation matches the given identifier, null is returned.
     */
    public Link getLinkByID(int id) {
        for (int i = 0; i < m_links.length; ++i) {
            if (m_links[i].getID() == id) {
                return m_links[i];
            }
        }
        return null;
    }

    /**
     * @param name - name of node.
     * @return array of nodes matching the given name.
     * There can be more than one, since the name is not necessary unique within the ontology.
     * If no one node matches the given name, array will be empty.
     */
    public ArrayList<Node> getNodesByName(String name) {
        ArrayList<Node> result = new ArrayList<Node>();
        for (int i = 0; i < m_nodes.length; ++i) {
            if (m_nodes[i].getName().equals(name))
                result.add(m_nodes[i]);
        }
        return result;
    }

    /**
     * @param name - name of node.
     * @return first node matching the given name. If no one node matches the given name, null is returned.
     */
    public Node getFirstNodeByName(String name) {
        for (int i = 0; i < m_nodes.length; ++i) {
            if (m_nodes[i].getName().equals(name)) {
                return m_nodes[i];
            }
        }
        return null;
    }

    /**
     * @param node - node to find relations from.
     * @param linkName - name of the relation.
     * @return array of nodes, which are connected with the given one by the relation (link) with given name. 
     * The direction of relation (link) is from the given node to nodes returned.
     */
    public ArrayList<Node> getNodesLinkedFrom(Node node, String linkName) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        int id = node.getID();
        for (int i = 0; i < m_links.length; ++i) {
            if (m_links[i].getSourceID() == id && m_links[i].getName().equals(linkName)) {
                nodes.add(getNodeByID(m_links[i].getTargetID()));
            }
        }
        return nodes;
    }

    /**
     * @param node - node to find relations to.
     * @param linkName - name of the relation.
     * @return array of nodes, which are connected with the given one by the relation (link) with given name.
     * The direction of relation (link) is from nodes returned to the given node.
     */
    public ArrayList<Node> getNodesLinkedTo(Node node, String linkName) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        int id = node.getID();
        for (int i = 0; i < m_links.length; ++i) {
            if (m_links[i].getTargetID() == id && m_links[i].getName().equals(linkName)) {
                nodes.add(getNodeByID(m_links[i].getSourceID()));
            }
        }
        return nodes;
    }

    /**
     * @param node - node to find relations from.
     * @param linkName - name of the relation.
     * @param typeName - name of the type defining node.
     * @return array of nodes, which are connected with the given one by the relation (link) with given name
     * and are connected by is_a to the node with given name (say, have given type).
     * The direction of relation (link) is from the given node to nodes returned.
     */
    public ArrayList<Node> getTypedNodesLinkedFrom(Node node, String linkName, String typeName) {
        ArrayList<Node> result = new ArrayList<Node>();
        ArrayList<Node> linked = getNodesLinkedFrom(node, linkName);
        for (int i = 0, n = linked.size(); i < n; ++i) {
            Node lNode = linked.get(i);
            ArrayList<Node> protos = getNodesLinkedFrom(lNode, "is_a");
            for (int j = 0, m = protos.size(); j < m; ++j) {
                if (protos.get(j).getName().equals(typeName)) {
                    result.add(lNode);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param node - node to find relations to.
     * @param linkName - name of the relation.
     * @param typeName - name of the type defining node.
     * @return array of nodes, which are connected with the given one by the relation (link) with given name
     * and are connected by is_a to the node with given name (say, have given type).
     * The direction of relation (link) is from nodes returned to the given node.
     */
    public ArrayList<Node> getTypedNodesLinkedTo(Node node, String linkName, String typeName) {
        ArrayList<Node> result = new ArrayList<Node>();
        ArrayList<Node> linked = getNodesLinkedTo(node, linkName);
        for (int i = 0, n = linked.size(); i < n; ++i) {
            Node lNode = linked.get(i);
            ArrayList<Node> protos = getNodesLinkedFrom(lNode, "is_a");
            for (int j = 0, m = protos.size(); j < m; ++j) {
                if (protos.get(j).getName().equals(typeName)) {
                    result.add(lNode);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param node - node to check type of.
     * @param typeName - name of the type defining node.
     * @return true if node has direct is_a connection with node of given name (say, has given type), false otherwise.
     */
    public boolean isNodeOfType(Node node, String typeName) {
        ArrayList<Node> protos = getNodesLinkedFrom(node, "is_a");
        for (int i = 0, n = protos.size(); i < n; ++i) {
            if (protos.get(i).getName().equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param node - node to check type of.
     * @param typeName - name of the type defining node.
     * @return true if node has is_a connection with node of given name (say, has given type) at any inheritance level, false otherwise.
     */
    public boolean isNodeOfTypeRecursive(Node node, String typeName) {
        boolean result = false;
        ArrayList<Node> protos = getNodesLinkedFrom(node, "is_a");
        for (int i = 0, n = protos.size(); i < n; ++i) {
            if (protos.get(i).getName().equals(typeName)) {
                result = true;
                break;
            } else {
                result = isNodeOfTypeRecursive(protos.get(i), typeName);
            }
        }

        return result;
    }

    /**
     * @param node - node to check type of.
     * @param linkName - name of the relation.
     * @param typeName - name of the type defining node.
     * @return true if node has connection (link) with given name with another node,
     * which is connected by is_a to the node with given name (say, has given type) at any inheritance level, false otherwise.
     * The direction of relation (link) is from the given node to nodes returned.
     */
    public boolean isNodeOfTypeRecursiveFrom(Node node, String linkName, String typeName) {
        boolean result = false;
        ArrayList<Node> linked = getNodesLinkedFrom(node, linkName);
        for (int i = 0, n = linked.size(); i < n; ++i) {
            Node lNode = linked.get(i);
            if (isNodeOfTypeRecursive(lNode, typeName)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * @param node - node to check type of.
     * @param linkName - name of the relation.
     * @param typeName - name of the type defining node.
     * @return true if node has connection (link) with given name with another node,
     * which is connected by is_a to the node with given name (say, has given type) at any inheritance level, false otherwise.
     * The direction of relation (link) is from nodes returned to the given node.
     */
    public boolean isNodeOfTypeRecursiveTo(Node node, String linkName, String typeName) {
        boolean result = false;
        ArrayList<Node> linked = getNodesLinkedTo(node, linkName);
        for (int i = 0, n = linked.size(); i < n; ++i) {
            Node lNode = linked.get(i);
            if (isNodeOfTypeRecursive(lNode, typeName)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * @param node - node to get attribute's value of.
     * @param name - name of the attribute.
     * @return value of the requested attribute of the given node, taking into account the inheritance.
     * The inheritance means that if given node has no attribute requested, it is going to be searched upwards by the
     * is_a links. If the entire hierarchy of nodes contains no attribute with the given name, null is returned.
     */
    public String getInheritedAttributeOfNode(Node node, String name) {
        String result = node.getUniqueAttribute(name);
        if (result != null)
            return result;
        ArrayList<Node> protos = getNodesLinkedFrom(node, "is_a");
        for (int i = 0, n = protos.size(); i < n; ++i) {
            result = getInheritedAttributeOfNode(protos.get(i), name);
            if (result != null)
                return result;
        }
        return null;
    }
}
