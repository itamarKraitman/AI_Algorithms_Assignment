import java.util.*;
import java.util.stream.Stream;

public class basicInference {

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
    private double answerInCpt = 0;

    /**
     * Constructor
     *
     * @param fullQuery query
     * @param network   Bayesian network
     */
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
            if (!queryVarsTruthValues.containsKey(varAndTruthHidden)) {
                hidden.put(varAndTruthHidden, network.get(varAndTruthHidden));
            }
        }
        solution = calculateInference();
    }


    /**
     * @return Nominator value
     */
    private double calculateNominator() {
        HashMap<String, ArrayList<HashMap<String, String>>> linesForInference = reduceCptToNominatorCalculation();
        double currentNominator = 0;
        HashMap<String, String> visited = new HashMap<>();
        int numberOfIterations = hidden.values().stream().mapToInt(hiddenVar -> hiddenVar.getOutcome().size())
                .reduce(1, (a, b) -> a * b); // number of iterations is the product of all hidden values outcomes
        for (int i = 0; i < numberOfIterations; i++) {
            double currentProbability = 1;
            visited.clear();
            for (BayesianNetworkNode hiddenNode : hidden.values()) {
                String hiddenName = hiddenNode.getName();
                HashMap<String, String> currLine = linesForInference.get(hiddenName).get(i);
                visited.put(hiddenName, currLine.get(hiddenName));
                currentProbability *= Double.parseDouble(currLine.get("prob")); // multiply by the prob of the hidden
                multiplications++;
                for (String varName : currLine.keySet()) {
                    if (!varName.equals(hiddenName) && !visited.containsKey(varName)) {
                        visited.put(varName, currLine.get(varName)); // adding var name to the visited map
                    }
                }
            }
            currentNominator += currentProbability;
            additions++;
        }
        return currentNominator;
    }

    /**
     * @return Denominator value
     */
    private double calculateDenominator() {
        HashMap<String, ArrayList<HashMap<String, String>>> linesForInference = reduceCptToDenominatorCalculation();
        double currentDenominator = 0;
        return currentDenominator;
    }

    /**
     * @return All Cpts with only the needed and valid lines for the nominator calculation
     */
    private HashMap<String, ArrayList<HashMap<String, String>>> reduceCptToNominatorCalculation() {
        HashMap<String, ArrayList<HashMap<String, String>>> validLines = new HashMap<>();
        // for each var in network:
        Set<String> allVars = network.keySet();
        for (String var : allVars) {
            validLines.put(var, new ArrayList<>());
            BayesianNetworkNode varNode = network.get(var);
            ArrayList<HashMap<String, String>> varCpt = varNode.getCpt();
            for (int i = 1; i < varCpt.size(); i++) {
                boolean isLineValid = true;
                HashMap<String, String> line = varCpt.get(i);
                if ((queryVar.equals(var) || evidence.containsKey(var))
                        && !queryVarsTruthValues.get(var).equals(line.get(var))) {
                    isLineValid = false;// line is not valid, move to the next line
                } else if (varNode.getEvidences().size() > 0) { // has given vars
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

    /**
     * @return All Cpts with only the needed and valid lines for the denominator calculation
     */
    private HashMap<String, ArrayList<HashMap<String, String>>> reduceCptToDenominatorCalculation() {
        HashMap<String, ArrayList<HashMap<String, String>>> validLines = new HashMap<>();
        Set<String> allVars = network.keySet();
        for (String var : allVars) {
            validLines.put(var, new ArrayList<>());
            BayesianNetworkNode varNode = network.get(var);
            ArrayList<HashMap<String, String>> varCpt = varNode.getCpt();
            for (int i = 1; i < varCpt.size(); i++) {
                boolean isLineValid = true;
                HashMap<String, String> line = varCpt.get(i);
                if (evidence.containsKey(var) && !queryVarsTruthValues.get(var).equals(line.get(var))) {
                    isLineValid = false;// line is not valid
                } else if (varNode.getEvidences().size() > 0) { // has given vars
                    for (String evidenceVar : varNode.getEvidenceNames()) {
                        if (evidence.containsKey(evidenceVar) && !queryVarsTruthValues.get(evidenceVar).equals(line.get(evidenceVar))) {
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


    /**
     * @return True if the answer for the query is in the cpt, false otherwise
     */
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
     * Calculates the probability of the query
     *
     * @return Double array with 3 arguments- solution, number of additions, and number of multiplications.
     */
    private double[] calculateInference() {
        boolean inCpt = isInCpt();
        if (!inCpt) {
            double nominator = calculateNominator();
            double denominator = calculateDenominator();
            return new double[]{(nominator / denominator), additions, multiplications};
        } else
            return new double[]{answerInCpt, 0, 0};
    }

    /**
     * @return Solutions of the inference
     */
    public double[] getSolution() {
        return solution;
    }
}
