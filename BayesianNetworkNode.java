import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class BayesianNetworkNode {
    /**
     * Class to create a bayesian network node
     */

    private String name;
    private ArrayList<String> outcome; // T or F
    private ArrayList<BayesianNetworkNode> evidence = new ArrayList<>(); // given variables
    private ArrayList<BayesianNetworkNode> children = new ArrayList<>(); // all variables which are children
    private ArrayList<String> evidenceNames; // given nodes names, for convenience
    private ArrayList<HashMap<String, String>> cpt = new ArrayList<>(); // conditional probability table
    private String[] probabilities; // nodes probabilities

    public void setName(String name) {
        this.name = name;
    }

    public  BayesianNetworkNode() {

    }

    public void setOutcome(ArrayList<String> outcome) {
        this.outcome = outcome;
    }

    public void setEvidence(ArrayList<BayesianNetworkNode> evidence) {
        this.evidence = evidence;
    }

    public void setChildren(ArrayList<BayesianNetworkNode> children) {
        this.children = children;
    }

    public void setEvidenceNames(ArrayList<String> evidenceNames) {
        this.evidenceNames = evidenceNames;
    }

    public void setCpt(ArrayList<HashMap<String, String>> cpt) {
        this.cpt = cpt;
    }

    public void setProbabilities(String[] probabilities) {
        this.probabilities = probabilities;
    }

    public BayesianNetworkNode(String name, ArrayList<String> outcome, ArrayList<String> evidenceNames, String probabilities) {
        this.name = name;
        this.outcome = new ArrayList<>(outcome);
        this.evidenceNames = new ArrayList<>(evidenceNames);
        this.probabilities = probabilities.split(" ");

    }

    public BayesianNetworkNode(BayesianNetworkNode bayesianNetworkNode) {
        this.name = bayesianNetworkNode.getName();
        this.outcome = bayesianNetworkNode.getOutcome();
        this.evidenceNames = bayesianNetworkNode.getEvidenceNames();
        this.probabilities = bayesianNetworkNode.getProbabilities();
        this.cpt = bayesianNetworkNode.getCpt();
        this.evidence = bayesianNetworkNode.getEvidences();
        this.children = (ArrayList<BayesianNetworkNode>) bayesianNetworkNode.getChildren();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getOutcome() {
        return this.outcome;
    }

    public ArrayList<String> getEvidenceNames() {
        return this.evidenceNames;
    }

    public String[] getProbabilities() {
        return this.probabilities;
    }

    public ArrayList<BayesianNetworkNode> getEvidences() {return this.evidence;}

    public Collection<BayesianNetworkNode> getChildren() {return this.children;}

    public ArrayList<HashMap<String, String>> getCpt() {return this.cpt;}

    /**
     * Creating the CPT of the node, according its probabilities and given nodes
     */
    public void createCpt() {
        int i = 0;
        // adding all evidence (givens) nodes names to the cpt
        HashMap<String, String> evidences = new HashMap<>();
        for (BayesianNetworkNode node : evidence)
            evidences.put(node.name, node.name);
        evidences.put(name, name);
        this.cpt.add(evidences);
        // filling the cpt with the probabilities
        while (i < probabilities.length) {
            int count = 1;
            HashMap<String, String> line = new HashMap<>();
            // adding the evidence nodes truth value to the line
            for (int j = 0; j < evidenceNames.size(); j++) {
                count *= evidence.get(j).outcome.size();
                line.put(evidence.get(j).name, evidence.get(j).outcome.get((i / (probabilities.length / count))
                        % evidence.get(j).outcome.size()));
            }
            // adding the node truth value and the corresponding probability to the line
            line.put(name, outcome.get(i % outcome.size()));
            line.put("prob", probabilities[i]);
            cpt.add(line);
            i++;
        }
    }


}
