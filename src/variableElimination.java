import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementing probability inference using Variable Elimination algorithm
 */
public class variableElimination implements Comparator<ArrayList<HashMap<String, String>>> {

    int additions = 0;
    int multiplications = 0;
    private final double[] solution;
    private boolean secondfOrThirdAlgo;

    private final String queryVar;
    private final HashMap<String, BayesianNetworkNode> network;
    private final HashMap<String, BayesianNetworkNode> hidden;
    private final HashMap<String, BayesianNetworkNode> evidence;
    private final HashMap<String, String> queryVarsOutcomesValues;
    private double answerInCpt;

    public variableElimination(String fullQuery, HashMap<String, BayesianNetworkNode> network, Boolean secondOrThirdAlgo) {
        this.network = network;
        this.secondfOrThirdAlgo = secondOrThirdAlgo;
        queryVarsOutcomesValues = new HashMap<>();
        evidence = new HashMap<>();
        hidden = new HashMap<>();
        String query = fullQuery.substring(2, fullQuery.indexOf("|")); // B=T
        queryVar = query.substring(0, query.indexOf("=")); // B
        String queryOutcomeValue = query.substring(query.indexOf("=") + 1); // T
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

        NumberFormat formatter = new DecimalFormat("#0.00000");

        if (isInCpt()) { // if the answer is in the cpt, no need to make any calculation
            return new double[]{Double.parseDouble(formatter.format(answerInCpt)), 0, 0};
        }

        // find only relevant variables for the query
        HashMap<String, BayesianNetworkNode> relevantVariablesNetwork = findRelevantVariables();

        // find only the relevant lines for the query in each cpt
        ArrayList<ArrayList<HashMap<String, String>>> onlyRelevantLines = findRelevantLines(relevantVariablesNetwork);

        for (BayesianNetworkNode hiddenVar : hidden.values()) {

            // find all the hidden factors
            ArrayList<ArrayList<HashMap<String, String>>> hiddenFactors = collectAllHiddenFactors(hiddenVar, onlyRelevantLines);
            if (hiddenFactors.size() > 0) {

                onlyRelevantLines.removeAll(hiddenFactors);

                if (secondfOrThirdAlgo)
                    // sort the factors
                    hiddenFactors.sort(this);
                else
                {}

                // join all hidden factors until there is only one
                ArrayList<ArrayList<HashMap<String, String>>> hiddenFactorAfterJoin = join(hiddenFactors);

                // eliminate hidden
                ArrayList<HashMap<String, String>> hiddenFactorAfterEliminate = eliminate(hiddenVar, hiddenFactorAfterJoin);

                // delete the factor if it's one value
                if (hiddenFactorAfterEliminate.size() > 2) { // if contains only one line of probability (two with keys line), discard it
                    onlyRelevantLines.add(hiddenFactorAfterEliminate);
                }
            }
        }

        // after eliminating all hidden vars, join (if necessary) and normalize the query factors to get the final solution

        // fina all query factors and join them
        ArrayList<ArrayList<HashMap<String, String>>> queryFactors = new ArrayList<>();
        for (ArrayList<HashMap<String, String>> factor : onlyRelevantLines) {
            if (factor.get(0).containsKey(queryVar)) {
                queryFactors.add(factor);
            }
        }
        ArrayList<ArrayList<HashMap<String, String>>> queryFactorAfterJoin = join(queryFactors);

        // normalize
        double normalizedProbability = normalizeProbability(queryFactorAfterJoin);

        return new double[]{Double.parseDouble(formatter.format(normalizedProbability)), additions, multiplications};

    }

    /**
     * @param queryFactorAfterJoin query factor after the join method, the answer is extract from it
     * @return the answer
     */
    private double normalizeProbability(ArrayList<ArrayList<HashMap<String, String>>> queryFactorAfterJoin) {
        double probabilityInOutcomeLine = 0;
        boolean lineWithKeys = true;
        double probabilitiesSum = 0;
        for (HashMap<String, String> line : queryFactorAfterJoin.get(0)) {
            if (lineWithKeys) {
                lineWithKeys = false;
                continue;
            }
            // if this line is not with the query outcome, add its probability to the sum
            if (!line.get(queryVar).equals(queryVarsOutcomesValues.get(queryVar))) {
                probabilitiesSum += Double.parseDouble(line.get("prob"));
            }
            // if this is the line with the query outcome, keep it to normalize calculation
            else {
                probabilityInOutcomeLine = Double.parseDouble(line.get("prob"));
            }
        }
        double normalizedProbability = probabilityInOutcomeLine / (probabilitiesSum + probabilityInOutcomeLine);
        additions++; // interment by 1 for sum in normalize

        return normalizedProbability;
    }

    /**
     * @param hiddenFactors the hidden factors which ar needed to e joined
     * @return the joined factor
     */
    private ArrayList<ArrayList<HashMap<String, String>>> join(ArrayList<ArrayList<HashMap<String, String>>> hiddenFactors) {
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
                        }
                    }
                    if (linesMatches) {
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
            hiddenFactors.remove(firstFactor);
            hiddenFactors.remove(secondFactor);
            hiddenFactors.add(newFactor);
            hiddenFactors.sort(this);

        }
        return hiddenFactors;
    }

    /**
     * @param hiddenToEliminate     Hidden variable to eliminate
     * @param hiddenEliminateFactor the factor which is need to eliminate hidden from
     * @return the joined hidden factor after eliminating the hidden
     */
    private ArrayList<HashMap<String, String>> eliminate(BayesianNetworkNode hiddenToEliminate, ArrayList<ArrayList<HashMap<String, String>>> hiddenEliminateFactor) {
        ArrayList<HashMap<String, String>> factorAfterElimination = new ArrayList<>();
        ArrayList<HashMap<String, String>> eliminateFactor = hiddenEliminateFactor.get(0);
        ArrayList<HashMap<String, String>> linesToRemove = new ArrayList<>();
        Set<String> keysWithoutHidden = eliminateFactor.get(0).keySet();
        keysWithoutHidden.remove(hiddenToEliminate.getName());
        // add all keys to the new factor
        factorAfterElimination.add(new HashMap<>());
        keysWithoutHidden.forEach(key -> factorAfterElimination.get(0).put(key, key));
        // remove the hidden var since it is not needed to be checked
        keysWithoutHidden.remove(hiddenToEliminate.getName());
        while (eliminateFactor.size() > 1) {
            linesToRemove.clear();
            // for each line, find its match lines where the outcomes of the vars which are not the hidden are equals
            HashMap<String, String> line = eliminateFactor.get(1); //starting at index 1 to skip the only keys line
            double sum = Double.parseDouble(line.get("prob"));
            linesToRemove.add(line);
            for (int i = 2; i < eliminateFactor.size(); i++) {
                boolean linesEquals = true;
                HashMap<String, String> otherLine = eliminateFactor.get(i);
                for (String key : keysWithoutHidden) {
                    if (!line.get(key).equals(otherLine.get(key))) { // if other line is not a match, skip to the next line
                        linesEquals = false;
                        break;
                    }
                }
                if (linesEquals) { // if other line is a match, add its probability to sum
                    sum += Double.parseDouble(otherLine.get("prob"));
                    additions++;
                    linesToRemove.add(otherLine);
                }
            }
            // after finding all the match lines, add theirs sum to the new factor
            HashMap<String, String> matchLine = new HashMap<>();
            for (String key : keysWithoutHidden) { // add all keys and outcomes
                matchLine.put(key, line.get(key));
            }
            matchLine.put("prob", String.valueOf(sum));
            factorAfterElimination.add(matchLine);
            eliminateFactor.removeAll(linesToRemove);
        }
        return factorAfterElimination;
    }


    /**
     * @return only relevant variables for the query
     */
    private HashMap<String, BayesianNetworkNode> findRelevantVariables() {
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
        return relevantVariablesNetwork;
    }

    /**
     * @param hiddenVar         hidden var to find its factors
     * @param onlyRelevantLines factors collection to find the relevant from
     * @return all the relevant factors
     */
    private ArrayList<ArrayList<HashMap<String, String>>> collectAllHiddenFactors(BayesianNetworkNode hiddenVar, ArrayList<ArrayList<HashMap<String, String>>> onlyRelevantLines) {
        ArrayList<ArrayList<HashMap<String, String>>> hiddenFactors = new ArrayList<>();
        for (ArrayList<HashMap<String, String>> factor : onlyRelevantLines) {
            if (factor.get(0).containsKey(hiddenVar.getName()))
                hiddenFactors.add(factor);
        }
        return hiddenFactors;
    }

    /**
     * @param relevantVariablesNetwork network of the relevant nodes  for the query
     * @return only relevant lines for each node
     */
    private ArrayList<ArrayList<HashMap<String, String>>> findRelevantLines(HashMap<String, BayesianNetworkNode> relevantVariablesNetwork) {
        ArrayList<ArrayList<HashMap<String, String>>> onlyRelevantLines = new ArrayList<>();
        for (String var : relevantVariablesNetwork.keySet()) {
            ArrayList<HashMap<String, String>> varCpt = relevantVariablesNetwork.get(var).getCpt();
            ArrayList<HashMap<String, String>> varRelevantLines = new ArrayList<>();
            varRelevantLines.add(varCpt.get(0));
            // if var cpt contains evidence var: keep only lines where the outcome of evidence is equal to the evidence outcome in the query outcome
            boolean containsEvidence = false;
            for (String evidenceVar : evidence.keySet()) {
                if (varCpt.get(0).containsKey(evidenceVar)) {
                    containsEvidence = true;
                    // if the line is valid, add it to the list
                    for (HashMap<String, String> line : varCpt) {
                        if (line.get(evidenceVar).equals(queryVarsOutcomesValues.get(evidenceVar))) {
//                            line.remove(evidenceVar);
                            varRelevantLines.add(line);
                        }
                    }
//                    varRelevantLines.get(0).remove(evidenceVar);
                }
            }
            // if node cpt does not contain any evidence, keep all the lines
            if (!containsEvidence)
                onlyRelevantLines.add(varCpt);
            else
                onlyRelevantLines.add(varRelevantLines);
        }
        return onlyRelevantLines;
    }

    /**
     * @param hiddenVar hidden var to check if relevant
     * @param queryVar  query var to check if hiddenVar is its ancestor
     * @return if hiddenVar is relevant for the query
     */
    private boolean findIfHiddenRelevant(BayesianNetworkNode hiddenVar, BayesianNetworkNode queryVar) {
        // recursively find if the hidden var is ancestor of the query/evidence var
        if (hiddenVar.getName().equals(queryVar.getName())) return true;
        for (BayesianNetworkNode evidenceVar : queryVar.getEvidences()) {
            if (findIfHiddenRelevant(hiddenVar, evidenceVar)) {
                return true;
            }
        }
        return false;
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
            if (!queryCpt.get(i).get(queryVar).equals(queryVarsOutcomesValues.get(queryVar)))
                return false; // if the outcomes are not equals, the answer is not in this line
            for (BayesianNetworkNode evidenceVar : queryNode.getEvidences()) {
                if (!queryVarsOutcomesValues.get(evidenceVar.getName()).equals(queryCpt.get(i).get(evidenceVar.getName())))
                    return false;
            }
            // if we did not return false- the answer is in this line
            this.answerInCpt = Double.parseDouble(queryCpt.get(i).get("prob"));
        }
        return true;
    }


}