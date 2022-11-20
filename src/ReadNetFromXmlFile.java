import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to use for reading the bayesian networks from a XML file
 */
public class ReadNetFromXmlFile {

    public static HashMap<String, BayesianNetworkNode> readNetFromXml(String filename) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> outcome = new ArrayList<>();
        ArrayList<String> evidence = new ArrayList<>();
        HashMap<String, BayesianNetworkNode> network = new HashMap<>();

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filename));
            doc.getDocumentElement().normalize();

            // reading all variables and their properties, and putting them all in the net
            NodeList variables = doc.getElementsByTagName("VARIABLE"); // reading all variables
            for (int i = 0; i < variables.getLength(); i++) {
                // reading variable name and outcomes
                Node current = variables.item(i);
                String name = "";
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) current;
                    name = element.getElementsByTagName("NAME").item(0).getTextContent();
//                    names.add(name); //reading name
                    NodeList outcomeElements = element.getElementsByTagName("OUTCOME");
                    for (int j = 0; j < outcomeElements.getLength(); j++) { // reading outcomes (T and F)
                        outcome.add(outcomeElements.item(j).getTextContent());
                    }
                }

                // reading given variables and probabilities
                NodeList definitions = doc.getElementsByTagName("DEFINITION");
                current = definitions.item(i);
                String probabilities = "";
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) current;
                    probabilities = element.getElementsByTagName("TABLE").item(0).
                            getTextContent(); //  reading probabilities
                    NodeList given = element.getElementsByTagName("GIVEN");
                    for (int k = 0; k < given.getLength(); k++) { // reading evidences
                        evidence.add(given.item(k).getTextContent());
                    }
                }
                // creating the node, its cpt, and putting it in the network
                BayesianNetworkNode currentNode = new BayesianNetworkNode(name, outcome, evidence, probabilities);
                currentNode.createCpt();
                network.put(name, currentNode);
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return network;
    }
}


