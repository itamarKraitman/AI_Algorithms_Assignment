import java.io.*;
import java.util.*;

public class Ex1 {

    static HashMap<String, BayesianNetworkNode> network;
    static HashMap<String, String> evidence = new HashMap<>(); // evidences of the query
    static String query, queryVar, queryTruthValue, fullGivens;
    static String[] givens;
//
//    /**
//     * implements basic bayesian inference
//     *
//     * @param fullQuery probabilistic inference query
//     * @return array with the probability, number of additions, and number of multiplications
//     */
//    //TODO
//    public static double[] algo1(String fullQuery) {
//        int multiplications = 0, additions = 0;
//        // bayes rule: P(B|J,M)= P(B,J,M) / P(J,M)
//        ArrayList<String> nominatorHidden = new ArrayList<>();
//        ArrayList<String> denominatorHidden = new ArrayList<>();
//        HashMap<String, String> queryVariables = new HashMap<>();
//
//        // find all vars which should appear in the bayes rule
//        queryVariables.put(queryVar, queryTruthValue);
//        for (String var : network.keySet()) {
//            if (!evidence.containsKey(var)) {
//                if (!queryVar.equals(var))
//                    nominatorHidden.add(var);
//                denominatorHidden.add(var);
//            } else {
//                queryVariables.put(var, evidence.get(var));
//            }
//        }
//
//        // checking if the answer is in the cpt
//        BayesianNetworkNode queryNode = network.get(queryVar);
//        boolean inCpt = isInCpt(queryNode);
//        if (inCpt)
//            return ifnCpt(queryNode);
////            for (int i = 1; i < queryNode.getCpt().size(); i++) {
////                boolean isLineWithAnswer = queryNode.getCpt().get(i).get(queryVar).equals(queryTruthValue);
////                if (!isLineWithAnswer) continue;
////                for (int j = 0; j < queryNode.getEvidences().size(); j++) {
////                    String evidenceName = queryNode.getEvidences().get(j).getName();
////                    if (!evidence.get(evidenceName).equals(queryNode.getCpt().get(i).get(evidenceName)))
////                        isLineWithAnswer = false;
////                }
////                if (isLineWithAnswer) // the answer is in this line
////                    return new double[]{Double.parseDouble(queryNode.getCpt().get(i).get("prob")), 0, 0};
////            }
//        else {
//            // if variable depends on other var in the query, take only the lines from the cpt where the query var is equal to its value
//            Set<String> keys = network.keySet();
//            for (String var : keys) {
//                BayesianNetworkNode current = network.get(var);
//                ArrayList<String> varEvidences = current.getEvidenceNames();
//                if (varEvidences.size() != 0) {
//                    for (int i = 0; i < varEvidences.size(); i++) {
//                        if (queryVariables.containsKey(varEvidences.get(i))) {
//                            for (HashMap<String, String> line : current.getCpt()) {
//                                if (line.get(i).equals())
//                            }
//                        }
//                    }
//                }
//
//            }
//
//
//            // calculating nominator. first computing the multiplication of the query variables (not hidden).
//            // those vars values are constant during the calculation
//            double nominator = 1;
//            double currProbability = 1;
//            int numberOfHidden = nominatorHidden.size();
//            for (int i = 0; i <= numberOfHidden; i++) {
//                // when i is odd all hidden vars are false and vice versa
//                currProbability = 1;
//                for (String var : keys) {
//                    BayesianNetworkNode varNode = network.get(var);
//                    ArrayList<HashMap<String, String>> varCpt = network.get(var).getCpt();
////                    HashMap<String,HashMap<String, String>> givenTruthValue = new HashMap<>();
////                    if (varCpt.size() > 3) { // has given values
////                        for (String given : varNode.getEvidenceNames()) {
////                            HashMap<String, String> TruthValue = varCpt.get(given);
//////                            if (queryVariables.containsKey(given) && !queryVariables.get(given).equals(TruthValue))
//////                                TruthValue = TruthValue.equals("T") ? "F" : "T";
//////                            givenTruthValue.put(given, TruthValue);
////                        }
////                    }
//                    // if var is query or evidence- truth value should be the same
////                    if (var.equals(queryVar)) { // query
////                        if (!varCpt.get(i % (varCpt.size() - 1) + 1).get(var).equals(queryTruthValue))
////                            currProbability *= Double.parseDouble(varCpt.get(i % varCpt.size() + 1).get("prob"));
////                        else
////                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
////                        multiplications += 1;
////                    }
////
////////                else if evidence     else if (evidence.containsKey(var)) {
////                        if (!varCpt.get(i % (varCpt.size() - 1) + 1).get(var).equals(evidence.get(var)))
////                            currProbability *= Double.parseDouble(varCpt.get(i % varCpt.size() + 1).get("prob"));
////                        else
////                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
////                        multiplications += 1;
////                    }
////                else -> hidden
//                    if (queryVariables.containsKey(var)) { // var is query or evidence
//                        if (varCpt.get(i % (varCpt.size() - 1) + 1).get(var).equals(queryVariables.get(var)))
//                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
//                        else
//                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size())).get("prob"));
//                    } else {
//                        currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
//                    }
//                    multiplications += 1;
//                }
//                nominator += currProbability;
//                additions++;
//            }
//
//            // denominator
//            numberOfHidden = denominatorHidden.size();
//            double denominator = 0;
//            for (int i = 0; i < numberOfHidden; i++) {
//                currProbability = 1;
//                for (String var : keys) {
//                    ArrayList<HashMap<String, String>> varCpt = network.get(var).getCpt();
//                    if (evidence.containsKey(var)) { // if evidence
//                        if (!varCpt.get(i % (varCpt.size() - 1) + 1).get(var).equals(evidence.get(var)))
//                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
//                        else
//                            currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size()) + 1).get("prob"));
//                        multiplications += 1;
//                    } else { // query or hidden
//                        currProbability *= Double.parseDouble(varCpt.get(i % (varCpt.size() - 1) + 1).get("prob"));
//                        multiplications += 1;
//                    }
//                }
//                denominator += currProbability;
//                additions++;
//            }
//
//
//            System.out.println((nominator / denominator) + "," + additions + "," + multiplications);
//            return new double[]{nominator / denominator, additions, multiplications};
//        }
//    }
//
//    private static double[] ifnCpt(BayesianNetworkNode queryNode) {
//        double[] answer = new double[3];
//        for (int i = 1; i < queryNode.getCpt().size(); i++) {
//            boolean isLineWithAnswer = queryNode.getCpt().get(i).get(queryVar).equals(queryTruthValue);
//            if (!isLineWithAnswer) continue;
//            for (int j = 0; j < queryNode.getEvidences().size(); j++) {
//                String evidenceName = queryNode.getEvidences().get(j).getName();
//                if (!evidence.get(evidenceName).equals(queryNode.getCpt().get(i).get(evidenceName)))
//                    isLineWithAnswer = false;
//            }
//            if (isLineWithAnswer) { // the answer is in this line
//                answer[0] = Double.parseDouble(queryNode.getCpt().get(i).get("prob"));
//                answer[1] = 0;
//                answer[2] = 0;
//                break;
//            }
//        }
//        return answer;
//    }
//
//    /**
//     * checking if the answer for the probabilistic inference is in the cpt
//     *
//     * @param queryVar the query var
//     * @return if the answer is in the var cpt
//     */
//    private static boolean isInCpt(BayesianNetworkNode queryVar) {
//        boolean inCpt = true;
//        for (String evidence : evidence.keySet()) {
//            if (!queryVar.getEvidenceNames().contains(evidence)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * implement variable elimination according the ABC
//     *
//     * @param query probabilistic inference query
//     * @return array with the probability, number of additions, and number of multiplications
//     */
//    //TODO
//    public static double[] algo2(String query) {
//        return null;
//    }
//
//    /**
//     * implement variable elimination according a heuristic logic
//     *
//     * @param query probabilistic inference query
//     * @return array with the probability, number of additions, and number of multiplications
//     */
//    //TODO
//    public static double[] algo3(String query) {
//        return null;
//    }


    public static void main(String[] args) {
//        String version = System.getProperty("java.version");
//        System.out.println("java version: " + version);
        String networkPath = "src/"; // for reading the net name
        ArrayList<String> queries = new ArrayList<>();
        try {
            FileReader fr = new FileReader("src/input.txt");
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("output.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            // reading the network
            networkPath += br.readLine();
            network = ReadNetFromXmlFile.readNetFromXml(networkPath);
            // reading line by line and compute the result by the specified algorithm
            String query = br.readLine();

//            double[] result = algo1(query);
            while (query != null) {
                String answer = "";
                double[] result = new double[3];
                String[] querySplit = query.split("\\),");
//                parseQuery(querySplit[0]);
                switch (querySplit[1]) {
                    case "1":
                        result = new basicInference(querySplit[0], network).getSolution();
                        break;
//                    case "2":
//                        result = algo2(querySplit[0]);
//                        break;
//                    case "3":
//                        result = algo3(querySplit[0]);
//                        break;
                }
                answer += (result[0] + "," + result[1] + "," + result[2]);
                bw.write(answer);
                query = br.readLine();
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
