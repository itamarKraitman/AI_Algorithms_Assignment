import sun.security.krb5.internal.KdcErrException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class basicInference {

    private double nominator;
    private double denominator;
    static int additions = 0;
    static int multiplications = 0;

    //    private HashMap<String, String> evidence = new HashMap<>(); // evidences of the query
    private String query, fullQuery, queryVar, queryTruthValue, fullGivens;
    private List<String> givens;
    private HashMap<String, BayesianNetworkNode> network, hidden, evidence;
    private HashMap<String, String> queryVarsTruthValues;

    public basicInference(String fullQuery, HashMap<String, BayesianNetworkNode> network) {
        this.fullQuery = fullQuery;
        this.network = network;
        // parsing the query
        queryVarsTruthValues = new HashMap<>();
        evidence = new HashMap<>();
        hidden = new HashMap<>();
        query = fullQuery.substring(2, fullQuery.indexOf("|")); // B=T
        queryVar = query.substring(0, query.indexOf("=")); // B
        queryTruthValue = query.substring(query.indexOf("=") + 1); // T
        queryVarsTruthValues.put(queryVar, queryTruthValue); // insert query value to query values truth value map
        fullGivens = fullQuery.substring(fullQuery.indexOf("|") + 1);
        givens = Arrays.asList(fullGivens.split(","));
        for (String varAndTruthGiven : givens) { // insert all givens
            int equalSignIndex = varAndTruthGiven.indexOf("=");
            evidence.put(varAndTruthGiven.substring(0, equalSignIndex), network.get(varAndTruthGiven.substring(0, equalSignIndex)));
            queryVarsTruthValues.put(varAndTruthGiven.substring(0, equalSignIndex), varAndTruthGiven.substring(equalSignIndex + 1));
        }
        for (String varAndTruthHidden : network.keySet()) { // insert all hidden
            if (!evidence.containsKey(varAndTruthHidden)) {
                hidden.put(varAndTruthHidden, network.get(varAndTruthHidden));
            }
        }
        nominator = calculateNominator();
        denominator = calculateDenominator();
//        solution = calculateInference();
    }

    /**
     * @return nominator value for the law of total probability
     */
    private double calculateNominator() {
        int numberOfExpressions = (int) Math.pow(2, hidden.size());
        double currentNominator = 0;
        HashMap<String, String> currentTruthValues = new HashMap<>();
        for (int i = 0; i < numberOfExpressions; i++) {
            // for each hidden:
            for (BayesianNetworkNode hidden : network.values()) {
                currentTruthValues.clear();
                for (int j = 1; j < hidden.getCpt().size(); j++) {
                    double currentProbability = 1;
                    HashMap<String, String> currLine = hidden.getCpt().get(j); //current line
                    boolean isLineValid = true;
                    if (hidden.getEvidenceNames().size() > 0) {
                        for (String givenVar : queryVarsTruthValues.keySet()) {
                            if (currLine.containsKey(givenVar) && !currLine.get(givenVar).equals(queryVarsTruthValues.get(givenVar))) {
                                isLineValid = false;
                                break;
                            }
                        }
                    }
                    if (isLineValid) {
                        currentProbability *= Double.parseDouble(currLine.get("prob"));
                        multiplications++;
                        for (String givenVar : hidden.getEvidenceNames()) {
                                currentProbability *= Double.parseDouble(network.get(givenVar).getCpt().)
                        }
                        for () {
//                            currentProbability *= Double.parseDouble(given.g)
                        }

                    }

                    currentNominator += currentProbability;
                    additions++;
                }
            }
        }
        // calculate prob according truth value- i % outcomes.size?
        // multiply by each known node corresponding to hidden truth value
        return nominator;
    }

    /**
     * @return denominator value for the law of total probability
     */
    private double calculateDenominator() {
        return denominator;
    }

    /**
     * @return double array with 3 arguments- solution, number of additions, and number of multiplications.
     */
    public double[] calculateInference() {
        return new double[]{(nominator / denominator), additions, multiplications};
    }
}
