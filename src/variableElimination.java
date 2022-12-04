import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Implementing probability inference using Variable Elimination algorithm
 */
public class variableElimination {

    int additions = 0;
    int multiplications = 0;
    private double[] solution;

    private final String queryVar;
    private final String queryOutcomeValue;
    private final HashMap<String, BayesianNetworkNode> network;
    private final HashMap<String, BayesianNetworkNode> hidden;
    private final HashMap<String, BayesianNetworkNode> evidence;
    private final HashMap<String, String> queryVarsOutcomesValues;
    private double answerInCpt = 0;

    public variableElimination(String fullQuery, HashMap<String, BayesianNetworkNode> network) {
        this.network = network;
        queryVarsOutcomesValues = new HashMap<>();
        evidence = new HashMap<>();
        hidden = new HashMap<>();
        String query = fullQuery.substring(2, fullQuery.indexOf("|")); // B=T
        queryVar = query.substring(0, query.indexOf("=")); // B
        queryOutcomeValue = query.substring(query.indexOf("=") + 1); // T
        queryVarsOutcomesValues.put(queryVar, queryOutcomeValue); // insert query value to query values truth value map
        String fullGivens = fullQuery.substring(fullQuery.indexOf("|") + 1);
        List<String> givens = Arrays.asList(fullGivens.split(","));
        // insert all givens
        givens.forEach(varAndTruthGiven -> {
            int equalSignIndex = varAndTruthGiven.indexOf("=");
            evidence.put(varAndTruthGiven.substring(0, equalSignIndex), network.get(varAndTruthGiven.substring(0, equalSignIndex)));
            queryVarsOutcomesValues.put(varAndTruthGiven.substring(0, equalSignIndex), varAndTruthGiven.substring(equalSignIndex + 1));
        });
        // insert all hidden
        for (String varAndTruthHidden : network.keySet()) {
            if (!queryVarsOutcomesValues.containsKey(varAndTruthHidden)) {
                hidden.put(varAndTruthHidden, network.get(varAndTruthHidden));
            }
        }

        solution = calculateVariableElimination();
    }

    /**
     * @return The
     */
    public double[] getSolution() {
        return solution;
    }

    /**
     * @return Calculate Variable Elimination
     */
    private double[] calculateVariableElimination() {
        HashMap<String, BayesianNetworkNode> relevantVariablesNetwork = new HashMap<>();
        // take only relevant variables
        for (String var : queryVarsOutcomesValues.keySet()) {
            for (BayesianNetworkNode hiddenVar : hidden.values()) {
                if (findIfHiddenRelevant(hiddenVar, network.get(var)))
                    relevantVariablesNetwork.put(hiddenVar.getName(), hiddenVar);
            }
        }


        return new double[]{0, 0, 0};
    }

    /**
     * @return Only relevant variables for the query
     */
    private boolean findIfHiddenRelevant(BayesianNetworkNode hiddenVar, BayesianNetworkNode queryVar) {
        if (hiddenVar.getName().equals(queryVar.getName())) return true;
        for (BayesianNetworkNode hid : queryVar.getEvidences())
            if (findIfHiddenRelevant(hid, queryVar)) return true;
        return false;
    }


    /**
     * @param hiddenToJoin Hidden variable to join
     */
    private void join(BayesianNetworkNode hiddenToJoin) {
    }

    /**
     * @param hiddenToEliminate Hidden variable to eliminate
     */
    private void eliminate(BayesianNetworkNode hiddenToEliminate) {
    }


    /**
     * @param firstFactor  first factor to compare
     * @param secondFactor second factor to compare
     * @return lower factor between two according size or ASCII if sizes are equal
     */
    private int findLowerFactor(ArrayList<HashMap<String, String>> firstFactor, ArrayList<HashMap<String, String>> secondFactor) {
        return 0;
    }

}
