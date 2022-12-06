import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementing probability inference using Variable Elimination algorithm
 */
public class variableElimination implements Comparator<ArrayList<HashMap<String, String>>> {

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
     * @return The solution
     */
    public double[] getSolution() {
        return solution;
    }

    /**
     * @return Calculate Variable Elimination
     */
    private double[] calculateVariableElimination() {
        HashMap<String, BayesianNetworkNode> relevantVariablesNetwork = new HashMap<>();
        relevantVariablesNetwork.put(queryVar, network.get(queryVar));
        relevantVariablesNetwork.putAll(evidence);
        // take only relevant variables
        for (String var : queryVarsOutcomesValues.keySet()) {
            for (BayesianNetworkNode hiddenVar : hidden.values()) {
                if (findIfHiddenRelevant(hiddenVar, network.get(var))) {
                    relevantVariablesNetwork.put(hiddenVar.getName(), hiddenVar);
                }
            }
        }
        // notice! relevantVariableNetwork contains ic particular all the relevant cpts for the current query, I'm going to
        // use it, so I wil not damage the original network.

        //TODO if necessary
//        // for query and evidence vars: keep only lines in the cpt with the right outcome for the query
//        HashMap<String, ArrayList<HashMap<String, String>>> onlyRelevantLines = new HashMap<>();
//        for (String queryVar : queryVarsOutcomesValues.keySet()) {
//            ArrayList<HashMap<String, String>> relevantLines = new ArrayList<>();
//            for (HashMap<String, String> line : relevantVariablesNetwork.get(queryVar).getCpt()) {
//                if (line.get(queryVar))
//            }
//        }


        // for each hidden, join and eliminate its factors
        for (BayesianNetworkNode hiddenVar : hidden.values()) {
            ArrayList<ArrayList<HashMap<String, String>>> hiddenFactors = new ArrayList<>();

            // collect all the factors which contain hiddenVar
            for (String var : relevantVariablesNetwork.keySet()) {
                ArrayList<HashMap<String, String>> varCpt = relevantVariablesNetwork.get(var).getCpt();
                if (var.equals(hiddenVar.getName())) hiddenFactors.add(varCpt); // if var is the hidden var
                else if (varCpt.get(0).containsKey(hiddenVar.getName())) { // if hidden is evidence of the current var
                    hiddenFactors.add(varCpt);
                }
            }

            // sort factors according size, for factor with same size according ASCII values
            hiddenFactors.sort(this);

            // join all hidden factors until there is only one
            ArrayList<HashMap<String, String>> hiddenFactorAfterJoin = join(hiddenVar, hiddenFactors);

            // eliminate hidden
            ArrayList<HashMap<String, String>> hiddenFactorAfterEliminate = eliminate(hiddenVar, hiddenFactorAfterJoin);

            // delete the factor if it's one value


        }


        return new double[]{0, 0, 0};
    }

    /**
     * @return Only relevant variables for the query
     */
    private boolean findIfHiddenRelevant(BayesianNetworkNode hiddenVar, BayesianNetworkNode queryVar) {
        // recursively find if the hidden var is ancestor of the query/evidence var
        if (hiddenVar.getName().equals(queryVar.getName())) return true;
        for (BayesianNetworkNode hid : queryVar.getEvidences()) {
            if (findIfHiddenRelevant(hiddenVar, hid)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param hiddenToJoin Hidden variable to join
     * @return the joined factor
     */
    private ArrayList<HashMap<String, String>> join(BayesianNetworkNode hiddenToJoin, ArrayList<ArrayList<HashMap<String, String>>> hiddenFactors) {
        // joining hidden factors until only one factor left
        while (hiddenFactors.size() > 1) {
            ArrayList<HashMap<String, String>> firstFactor = hiddenFactors.get(0);
            ArrayList<HashMap<String, String>> secondFactor = hiddenFactors.get(1);
            ArrayList<HashMap<String, String>> newFactor = new ArrayList<>();
            newFactor.add(new HashMap<>());
            newFactor.get(0).putAll(firstFactor.get(0));
            newFactor.get(0).putAll(secondFactor.get(0));
            // adding all the variables present in both factors to the set for later use in multiplication process
            Set<String> variablesPresentInBoth = firstFactor.get(0).keySet().stream().filter(key -> secondFactor.get(0).containsKey(key)).collect(Collectors.toSet());

            // iterating along firstFactor, and for each line find the line in secondFactor that matches the outcomes of the common variables
            // when found, multiply theirs probability and add to the new factor
            boolean firstFirstLine = true;
            for (HashMap<String, String> firstFactorLine : firstFactor) {
                if (firstFirstLine) { // no need to check first line (eg A->A)
                    firstFirstLine = false;
                    continue;
                }
                boolean secondFirstLine = true;
                for (HashMap<String, String> secondFactorLine : secondFactor) {
                    if (secondFirstLine) {
                        secondFirstLine = false;
                        continue;
                    }
                    boolean linesMatches = true;
                    // check if the outcomes of common are equal. if not, skip to the next line
                    for (String key : variablesPresentInBoth) {
                        if (!firstFactorLine.get(key).equals(secondFactorLine.get(key))) {
                            linesMatches = false;
                            break;
                        }
                    }
                    HashMap<String, String> newFactorLine = new HashMap<>();
                    // add all keys to the line, common variables outcomes are equal so no need to check again
                    for (String key : firstFactorLine.keySet())
                        newFactorLine.put(key, firstFactorLine.get(key));
                    secondFactorLine.keySet().forEach(key -> newFactorLine.put(key, secondFactorLine.get(key)));
                    // calculate the probability and put it in the new line
                    double multiplyProbability = Double.parseDouble(firstFactorLine.get("prob")) * Double.parseDouble(secondFactorLine.get("prob"));
                    newFactorLine.put("prob", String.valueOf(multiplyProbability));
                    multiplications++;
                    newFactor.add(newFactorLine);
                }
            }

        }
        return null;
    }

    /**
     * @param hiddenToEliminate Hidden variable to eliminate
     * @return the joined hidden factor after eliminating the hidden
     */
    private ArrayList<HashMap<String, String>> eliminate(BayesianNetworkNode hiddenToEliminate, ArrayList<HashMap<String, String>> hiddenJoinedFactor) {
        return null;
    }


    /**
     * @param firstCpt  first cpt
     * @param secondCpt second cpt
     * @return 1 if first > second, -1 otherwise
     */
    @Override
    public int compare(ArrayList<HashMap<String, String>> firstCpt, ArrayList<HashMap<String, String>> secondCpt) {
        // first sort by size
        if (firstCpt.size() > secondCpt.size()) return 1;
        if (firstCpt.size() < secondCpt.size()) return -1;
        else {
            // if sizes are equal, sort by ASCII
            int firstCptAsciiSum = firstCpt.get(0).keySet().stream().mapToInt(key -> key.charAt(0)).sum();
            ;
            int secondCptAsciiSum = secondCpt.get(0).keySet().stream().mapToInt(key -> key.charAt(0)).sum();
            if (firstCptAsciiSum > secondCptAsciiSum) return 1;
            else return -1;
        }
    }

//    /**
//     * @param cpt the cpt to calculate its keys ASCII values
//     * @return sum of keys ASCII values
//     */
//    private int sumAsciiValues(ArrayList<HashMap<String, String>> cpt) {
//        return
//    }

}
