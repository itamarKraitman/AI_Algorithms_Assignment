import java.util.*;

public class basicInference {

//    private double nominator = 0;
//    private double denominator = 0;
    static int additions = 0;
    static int multiplications = 0;
    static double[] solution = new double[3];

    private String query;
    private final String queryVar;
    private final String queryTruthValue;
    private String fullGivens;
    private List<String> givens;
    private final HashMap<String, BayesianNetworkNode> network;
    private final HashMap<String, BayesianNetworkNode> hidden;
    private final HashMap<String, BayesianNetworkNode> evidence;
    private final HashMap<String, String> queryVarsTruthValues;
    private boolean inCpt = false;
    private double answerInCpt = 0;

    public basicInference(String fullQuery, HashMap<String, BayesianNetworkNode> network) {
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
        inCpt = isInCpt();
        solution = calculateInference(inCpt);
    }

    /**
     * @return nominator value for the law of total probability
     */
    private double calculateNominator() {
        double currentNominator = 0;
        HashMap<String, ArrayList<HashMap<String, String>>> linesForInference = reduceCptToQueryInference();

        return currentNominator;
    }
//        int numberOfExpressions = (int) Math.pow(2, hidden.size());
//        double currentNominator = 0;
//        HashMap<String, String> currentTruthValues = new HashMap<>();
//        for (int i = 0; i < numberOfExpressions; i++) {
//            // for each hidden:
//            for (BayesianNetworkNode hidden : network.values()) {
//                currentTruthValues.clear();
//                for (int j = 1; j < hidden.getCpt().size(); j++) {
//                    double currentProbability = 1;
//                    HashMap<String, String> currLine = hidden.getCpt().get(j); //current line
//                    boolean isLineValid = true;
//                    if (hidden.getEvidenceNames().size() > 0) {
//                        for (String givenVar : queryVarsTruthValues.keySet()) {
//                            if (currLine.containsKey(givenVar) && !currLine.get(givenVar).equals(queryVarsTruthValues.get(givenVar))) {
//                                isLineValid = false;
//                                break;
//                            }
//                        }
//                    }
//                    if (isLineValid) {
//                        currentProbability *= Double.parseDouble(currLine.get("prob"));
//                        multiplications++;
//                        for (String givenVar : hidden.getEvidenceNames()) {
//                            currentProbability *= Double.parseDouble(network.get(givenVar).getCpt().g)
//                        }
//                        for () {
////                            currentProbability *= Double.parseDouble(given.g)
//                        }
//
//                    }
//
//                    currentNominator += currentProbability;
//                    additions++;
//                }
//            }
//        }
//        // calculate prob according truth value- i % outcomes.size?
//        // multiply by each known node corresponding to hidden truth value
//        return nominator;
//    }

    private HashMap<String, ArrayList<HashMap<String, String>>> reduceCptToQueryInference() {
        HashMap<String, ArrayList<HashMap<String, String>>> validLines = new HashMap<>();
        // for each var in network:
        Set<String> allVars = network.keySet();
        for (String var : allVars) {
            validLines.put(var, new ArrayList<>());
            BayesianNetworkNode varNode = network.get(var);
            ArrayList<HashMap<String, String>> varCpt = varNode.getCpt();
            for (int i = 1; i < varCpt.size(); i++) {
                boolean isLineValid = true;
//            for (HashMap<String, String> line : varCpt) {
                HashMap<String, String> line = varCpt.get(i);
                if ((queryVar.equals(var) || evidence.containsKey(var))
                        && !queryVarsTruthValues.get(var).equals(line.get(var))) {
                    isLineValid = false;// line is not valid, move to the next line
                }
                else if (varNode.getEvidences().size() > 0) { // has given vars
                    for (String evidenceVar : varNode.getEvidenceNames()) {
                        if ((queryVar.equals(evidenceVar) || evidence.containsKey(evidenceVar))
                                && !queryVarsTruthValues.get(evidenceVar).equals(line.get(evidenceVar))) {
                            isLineValid = false;
                            break;
                        } // line is not valid, move to the next line
                    }
                }
                if (isLineValid)
                    validLines.get(var).add(line);
            }
        }
        return validLines;
    }


    private double calculateDenominator() {
        double currentDenominator = 0;
        return currentDenominator;
    }


    private boolean isInCpt() {
        BayesianNetworkNode queryNode = network.get(queryVar);
        // if the evidences vars are not given vars || given vars are not in evidence vars- the answer not in the cpt
        for (String evidence : evidence.keySet()) {
            if (!queryNode.getEvidenceNames().contains(evidence)) {
                return false;
            }
        }
        for (String evidenceVar : queryNode.getEvidenceNames()) {
            if (!evidence.containsKey(evidenceVar))
                return false;
        }
        // if both conditions above are false- the answer might be in the cpt
        ArrayList<HashMap<String, String>> queryCpt = queryNode.getCpt();
        for (int i = 1; i < queryCpt.size(); i++) {
            if (!queryCpt.get(i).get(queryVar).equals(queryTruthValue))
                return false; // if the truth value not equals the answer not in this line
            for (BayesianNetworkNode evidenceVar : queryNode.getEvidences()) {
                if (!queryVarsTruthValues.get(evidenceVar.getName()).equals(queryCpt.get(i).get(evidenceVar.getName())))
                    return false;
            }
            // if we did not return false- the answer is in this line
            answerInCpt = Double.parseDouble(queryCpt.get(i).get("prob"));
        }
        return true;
    }

    /**
     * @return double array with 3 arguments- solution, number of additions, and number of multiplications.
     */
    private double[] calculateInference(boolean inCpt) {
        if (!inCpt) {
            double nominator = calculateNominator();
            double denominator = calculateDenominator();
            return new double[]{(nominator / denominator), additions, multiplications};
        } else
            return new double[]{answerInCpt, 0, 0};
    }

    /**
     * @return solutions of the inference
     */
    public double[] getSolution() {
        return solution;
    }
}
