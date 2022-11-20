import java.util.ArrayList;
import java.util.HashMap;

public class BayesianNetworkNode {
    /**
     * Class to create a bayesian network node
     */

    private String name;
    private ArrayList<String> outcome; // T or F
    private ArrayList<BayesianNetworkNode> evidence; // given variables
    private ArrayList<String> evidenceNames; // given nodes names, for convenience
    private ArrayList<HashMap<String, String>> cpt; // conditional probability table
    private String[] probabilities; // nodes probabilities

    public BayesianNetworkNode(String name, ArrayList<String> outcome, ArrayList<String> evidenceNames, String probabilities) {
        this.name = name;
        this.outcome = new ArrayList<>(outcome);
        this.evidenceNames = new ArrayList<>(evidenceNames);
        this.probabilities = probabilities.split(" ");

    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getOutcome() {
        return this.outcome;
    }

    public ArrayList<String> getEvidence() {
        return this.evidenceNames;
    }

    public String[] getProbabilities() {
        return this.probabilities;
    }

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
