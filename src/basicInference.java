import java.util.*;

/**
 * Implementing probability inference from Bayesian Network according the Law of Total Probability
 */
public class basicInference {

    int additions = 0;
    int multiplications = 0;
    private final double[] solution;

    private final String queryVar;
    private final String queryOutcomeValue;
    private final HashMap<String, BayesianNetworkNode> network;
    private final HashMap<String, BayesianNetworkNode> hidden;
    private final HashMap<String, BayesianNetworkNode> evidence;
    private final HashMap<String, String> queryVarsOutcomesValues;
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
        solution = calculateInference();
    }


    /**
     * @param isNominator permutations are generated according the part that should be calculated (nominator or denominator)
     * @return the answer of the part that should be calculated (nominator or denominator)
     */
    private double calculateProbability(boolean isNominator) {
        ArrayList<HashMap<String, String>> permutations = isNominator ? findPermutations(false) : findPermutations(true);
        if (isNominator) {
            // add query variable and its query outcome to all the permutations
            permutations.forEach(permutation -> permutation.put(queryVar, queryOutcomeValue));
        } else { // remove all the lines that are in both nominator and denominator in order to not make redundant moves
            permutations.removeIf(permutation -> permutation.get(queryVar).equals(queryOutcomeValue));
        }
        double result = 0;
        double currentResult = 0;
        boolean firstPermutation = true;
        // for each permutation, calculate its value and it to the general value
        for (HashMap<String, String> permutation : permutations) {
            boolean firstVar = true;
            for (String var : permutation.keySet()) {
                BayesianNetworkNode currVar = network.get(var);
                // find the line in the cpt where the outcome equal to the outcome in the permutation
                for (HashMap<String, String> line : currVar.getCpt()) {
                    boolean lineFound = true;
                    if (currVar.getEvidenceNames().size() == 0) { // if doesn't have parents
                        if (!line.get(var).equals(permutation.get(var))) {
                            lineFound = false;
                        }
                    } else { // var has parents
                        for (String evi : currVar.getEvidenceNames()) {
                            if (!line.get(var).equals(permutation.get(var)) || !line.get(evi).equals(permutation.get(evi))) { // outcomes are not equal
                                lineFound = false;
                                break;
                            }
                        }
                    }
                    if (lineFound) {
                        if (firstVar) {// no multiplication method is applied for the first probability because it is the first
                            currentResult = Double.parseDouble(line.get("prob"));
                            firstVar = false;
                        } else {
                            currentResult *= Double.parseDouble(line.get("prob"));
                            multiplications++;
                        }
                        break;
                    }
                }
            }
            if (firstPermutation) { // no multiplication method is applied for the first probability because it is the first
                result = currentResult;
                firstPermutation = false;
            } else {
                result += currentResult;
                additions++;
            }
        }
        return result;
    }


    /**
     * @return All permutations except of the query variable
     */
    private ArrayList<HashMap<String, String>> findPermutations(boolean permutationsForDenominator) {
        ArrayList<HashMap<String, String>> AllPermutations = new ArrayList<>();
        // number of AllPermutations is equal to the multiplication of all hidden outcomes
        int numberOfHiddenOutcomes = hidden.values().stream().mapToInt(hiddenVar -> hiddenVar.getOutcome().size())
                .reduce(1, (x, y) -> x * y);
        if (permutationsForDenominator)
            numberOfHiddenOutcomes *= network.get(queryVar).getOutcome().size();
        int i = 0;
        while (i < numberOfHiddenOutcomes) {
            HashMap<String, String> permutation = new HashMap<>();
            // add all evidences
            for (String evi : queryVarsOutcomesValues.keySet()) {
                if (!evi.equals(queryVar))
                    permutation.put(evi, queryVarsOutcomesValues.get(evi));
            }
            int count = 1;
            ArrayList<String> hiddenNames = new ArrayList<>(hidden.keySet());
            for (String hid : hiddenNames) {
                count *= hidden.get(hid).getOutcome().size();
                permutation.put(hidden.get(hid).getName(), hidden.get(hid).getOutcome().get((i / (numberOfHiddenOutcomes / count))
                        % hidden.get(hid).getOutcome().size()));
            }
            if (permutationsForDenominator) { // if the AllPermutations are for the denominator calculation, manipulate also with the query var as well
                BayesianNetworkNode queryNode = network.get(queryVar);
                permutation.put(queryVar, queryNode.getOutcome().get(i % queryNode.getOutcome().size()));
            }
            AllPermutations.add(permutation);
            i++;
        }
        return AllPermutations;
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
        // check each line in the cpt if the answer is in the line. if true, save the answer, else return false and check with the algorithm
        for (HashMap<String, String> line : queryCpt) {
            boolean linesNotEquals = line.get(queryVar).equals(queryVarsOutcomesValues.get(queryVar));
            for (BayesianNetworkNode evidenceVar : queryNode.getEvidences()) {
                if (!queryVarsOutcomesValues.get(evidenceVar.getName()).equals(line.get(evidenceVar.getName())))
                    linesNotEquals = false;
            }
            if (linesNotEquals) {
                // if we did not return false- the answer is in this line
                this.answerInCpt = Double.parseDouble(line.get("prob"));
                break; // the answer was found so no need to check anymore
            }
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
            double nominator = calculateProbability(true);
            double denominator = calculateProbability(false);
            // increase addition by 1 because of the addition in the denominator
            return new double[]{Double.parseDouble(String.format("%.5f", nominator / (nominator + denominator))), additions + 1, multiplications};
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

//    /**
//     * @return Calculating denominator value
//     */
//    public double calculateDenominator() {
//        ArrayList<HashMap<String, String>> denominatorPermutations = findPermutations(true);
//        double denominator = 0;
//        double currentDenominator = 0;
//        boolean firstPermutation = true;
//        // add query variable and its query outcome to all the permutations
////        nominatorPermutations.forEach(permutation -> permutation.put(queryVar, queryTruthValue));
//        // for each permutation, calculate its value and it to the general value
//        for (HashMap<String, String> permutation : denominatorPermutations) {
//            boolean firstVar = true;
//            for (String var : permutation.keySet()) {
//                BayesianNetworkNode currVar = network.get(var);
//                // find the line in the cpt where the outcome equal to the outcome in the permutation
//                for (HashMap<String, String> line : currVar.getCpt()) {
//                    boolean lineFound = true;
//                    if (currVar.getEvidenceNames().size() == 0) { // if doesn't have parents
//                        if (!line.get(var).equals(permutation.get(var))) {
//                            lineFound = false;
//                        }
//                    } else { // var has parents
//                        for (String evi : currVar.getEvidenceNames()) {
//                            if (!line.get(var).equals(permutation.get(var)) || !line.get(evi).equals(permutation.get(evi))) {// outcomes are not equal
//                                lineFound = false;
//                                break;
//                            }
//                        }
//                    }
//                    if (lineFound) {
//                        if (firstVar) {// no multiplication method is applied for the first probability because it is the first
//                            currentDenominator = Double.parseDouble(line.get("prob"));
//                            firstVar = false;
//                        } else {
//                            currentDenominator *= Double.parseDouble(line.get("prob"));
//                            multiplications++;
//                        }
//                        break;
//                    }
//                }
//            }
//            denominator += currentDenominator;
//            additions++;
//        }
//        return denominator;
//    }
//    }
//
//    private int findNumberOfIterations(HashMap<String, ArrayList<HashMap<String, String>>> longestCpt) {
//        int longestValueSize = 0;
//        for (ArrayList<HashMap<String, String>> value : longestCpt.values())
//            if (value.size() > longestValueSize) longestValueSize = value.size();
//        return longestValueSize;
//    }
//        HashMap<String, ArrayList<HashMap<String, String>>> linesForInference = reduceCptToNominatorCalculation();
////      finds the longest cpt
////        ArrayList<HashMap<String, String>> permutation = getAllPermutations(linesForInference, false);
//        HashMap<String, ArrayList<HashMap<String, String>>> longestCpt = findLongestCpt(linesForInference);
//        double currentNominator = 0;
////        int numberOfIterations = hidden.values().stream().mapToInt(hiddenVar -> hiddenVar.getOutcome().size())
////                .reduce(1, (a, b) -> a * b); // number of iterations is the product of all hidden values outcomes
//        int numberOfIterations = findNumberOfIterations(longestCpt);
////        // iterating over the longest cpt of valid lines and fins all
//        for (String currVar : longestCpt.keySet()) {
//            // going threw all vars, and for each line find its corresponding line in the other cpts
//            ArrayList<HashMap<String, String>> varLines = linesForInference.get(currVar);
//            for (HashMap<String, String> line : varLines) {
//                double currentProbability = Double.parseDouble(line.get("prob"));
//                for (BayesianNetworkNode comparisonVar : network.values()) {
//                    if (!comparisonVar.getName().equals(currVar)) { // no reason to compare var to itself
//                        ArrayList<HashMap<String, String>> compareLines = linesForInference.get(comparisonVar.getName());
//                        for (HashMap<String, String> compareLine : compareLines) {
//                            boolean commonVarsEqual = true;
//                            for (String key : line.keySet()) {
//                                if (!key.equals("prob") && compareLine.containsKey(key)) { // if both lines contains this var
//                                    if (!line.get(key).equals(compareLine.get(key))) {
//                                        commonVarsEqual = false; // truth values are not equal
//                                        break;
//                                    }
//                                }
//                            }
//                            if (commonVarsEqual) { // lines should be multiplication
//                                currentProbability *= Double.parseDouble(compareLine.get("prob"));
////                                multiplications++;
////                                compareLines.remove(compareLine);
//                                break;
//                            }
//                        }
//                    }
//                }
//                numberOfIterations--;
//                currentNominator += currentProbability;
////                if (numberOfIterations != 0)
////                    additions++;
//            }
//
//        }
////        }
//        return currentNominator;
//
//    /**
//     * @return Denominator value
//     */
//    private double calculateDenominator() {
//        HashMap<String, ArrayList<HashMap<String, String>>> linesForInference = reduceCptToDenominatorCalculation();
////        finds the longest cpt
//        HashMap<String, ArrayList<HashMap<String, String>>> longestCpt = findLongestCpt(linesForInference);
//        double currentDenominator = 0;
//        int numberOfIterations = findNumberOfIterations(longestCpt); // number of iterations is the product of all hidden values outcomes
//        // iterating over the longest cpt of valid lines and find all
//        /*for (String currVar : longestCpt.keySet()) {*/
//        String currVar = new ArrayList<>(longestCpt.keySet()).get(0);
//        // going threw all vars, and for each line find its corresponding line in the other cpts
//        ArrayList<HashMap<String, String>> varLines = linesForInference.get(currVar);
//        for (HashMap<String, String> line : varLines) {
//            double currentProbability = Double.parseDouble(line.get("prob"));
//            for (BayesianNetworkNode comparisonVar : network.values()) {
//                int i = 0;
//                if (!comparisonVar.getName().equals(currVar)) { // no reason to compare var to itself
//                    ArrayList<HashMap<String, String>> compareLines = linesForInference.get(comparisonVar.getName());
//                    for (HashMap<String, String> compareLine : compareLines) {
//                        boolean commonVarsEqual = true;
//                        for (String key : line.keySet()) {
//                            if (!key.equals("prob") && compareLine.containsKey(key)) { // if both lines contains this var
//                                if (!line.get(key).equals(compareLine.get(key))) {
//                                    commonVarsEqual = false; // truth values are not equal
//                                    break;
//                                }
//                            }
//                        }
//                        if (commonVarsEqual) { // lines should be multiplication
//                            currentProbability *= Double.parseDouble(compareLine.get("prob"));
//                            multiplications++;
////                            compareLines.remove(compareLine);
//                            break;
//                        }
//                    }
//                }
//            }
//            numberOfIterations--;
//            currentDenominator += currentProbability;
//            if (numberOfIterations != 0)
//                additions++;
//        }
//        return currentDenominator;
//    }
//
//    /**
//     * @param linesForInference All valid lines for inference
//     * @return the longest cpt
//     */
//    private HashMap<String, ArrayList<HashMap<String, String>>> findLongestCpt(HashMap<String, ArrayList<HashMap<String, String>>> linesForInference) {
//        HashMap<String, ArrayList<HashMap<String, String>>> longestCpt = new HashMap<>();
//        boolean flag = true;
//        String keyOfLongest = "";
//        for (String var : linesForInference.keySet()) {
//            if (flag) {
//                longestCpt.put(var, linesForInference.get(var));
//                keyOfLongest = var;
//                flag = false;
//            }
//            if (linesForInference.get(var).size() > longestCpt.get(keyOfLongest).size()) {
//                longestCpt.clear();
//                longestCpt.put(var, linesForInference.get(var));
//                keyOfLongest = var;
//            }
//        }
//        return longestCpt;
//    }
//
//    /**
//     * @return All Cpts with only the needed and valid lines for the nominator calculation
//     */
//    private HashMap<String, ArrayList<HashMap<String, String>>> reduceCptToNominatorCalculation() {
//        HashMap<String, ArrayList<HashMap<String, String>>> validLines = new HashMap<>();
//        // for each var in network:
//        Set<String> allVars = network.keySet();
//        for (String var : allVars) {
//            validLines.put(var, new ArrayList<>());
//            BayesianNetworkNode varNode = network.get(var);
//            ArrayList<HashMap<String, String>> varCpt = varNode.getCpt();
//            for (int i = 1; i < varCpt.size(); i++) {
//                boolean isLineValid = true;
//                HashMap<String, String> line = varCpt.get(i);
//                if ((queryVar.equals(var) || evidence.containsKey(var))
//                        && !queryVarsTruthValues.get(var).equals(line.get(var))) {
//                    isLineValid = false;// line is not valid, move to the next line
//                } else if (varNode.getEvidences().size() > 0) { // has given vars
//                    for (String evidenceVar : varNode.getEvidenceNames()) {
//                        if ((queryVar.equals(evidenceVar) || evidence.containsKey(evidenceVar))
//                                && !queryVarsTruthValues.get(evidenceVar).equals(line.get(evidenceVar))) {
//                            isLineValid = false;
//                            break;
//                        } // line is not valid, move to the next line
//                    }
//                }
//                if (isLineValid)
//                    validLines.get(var).add(line);
//            }
//        }
//        return validLines;
//    }
//
//    /**
//     * @return All Cpts with only the needed and valid lines for the denominator calculation
//     */
//    private HashMap<String, ArrayList<HashMap<String, String>>> reduceCptToDenominatorCalculation() {
//        HashMap<String, ArrayList<HashMap<String, String>>> validLines = new HashMap<>();
//        Set<String> allVars = network.keySet();
//        for (String var : allVars) {
//            validLines.put(var, new ArrayList<>());
//            BayesianNetworkNode varNode = network.get(var);
//            ArrayList<HashMap<String, String>> varCpt = varNode.getCpt();
//            for (int i = 1; i < varCpt.size(); i++) {
//                boolean isLineValid = true;
//                HashMap<String, String> line = varCpt.get(i);
//                if (evidence.containsKey(var) && !queryVarsTruthValues.get(var).equals(line.get(var))) {
//                    isLineValid = false;// line is not valid
//                } else if (varNode.getEvidences().size() > 0) { // has given vars
//                    for (String evidenceVar : varNode.getEvidenceNames()) {
//                        if (evidence.containsKey(evidenceVar) && !queryVarsTruthValues.get(evidenceVar).equals(line.get(evidenceVar))) {
//                            isLineValid = false;
//                            break;
//                        } // line is not valid, move to the next line
//                    }
//                }
//                if (isLineValid)
//                    validLines.get(var).add(line);
//            }
//        }
//        return validLines;
//    }
//
//
