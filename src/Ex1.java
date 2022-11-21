import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Ex1 {

    static HashMap<String, BayesianNetworkNode> network;
    static HashMap<String, String> evidence = new HashMap<>(); // evidences of the query
    static String query, queryVar, queryTruthValue, fullGivens;
    static String[] givens;

    /**
     * implements basic bayesian inference
     *
     * @param fullQuery probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo1(String fullQuery) {
        int multiplications = 0, additions = 0;
        // bayes rule: P(B|J,M)= P(B,J,M) / P(J,M)
        ArrayList<String> nominatorHidden = new ArrayList<>();
        ArrayList<String> denominatorHidden = new ArrayList<>();
        ArrayList<String> queryVariables = new ArrayList<>();

        // find all vars which should appear in the bayes rule
        queryVariables.add(queryVar);
        for (String var : network.keySet()) {
            // nominator
            if (!evidence.containsKey(var)) {
                if (!queryVar.equals(var))
                    nominatorHidden.add(var);
                denominatorHidden.add(var);
            } else {
                queryVariables.add(var);
            }
        }

        // checking if the answer is in the cpt
        BayesianNetworkNode queryNode = network.get(queryVar);
        boolean inCpt = isInCpt(queryNode);
        if (inCpt) {
            for (int i = 1; i < queryNode.getCpt().size(); i++) {
                boolean isLineWithAnswer = queryNode.getCpt().get(i).get(queryVar).equals(queryTruthValue);
                if (!isLineWithAnswer) continue;
                for (int j = 0; j < queryNode.getEvidences().size(); j++) {
                    String evidenceName = queryNode.getEvidences().get(j).getName();
                    if (!evidence.get(evidenceName).equals(queryNode.getCpt().get(i).get(evidenceName)))
                        isLineWithAnswer = false;
                }
                if (isLineWithAnswer) // the answer is in this line
                    return new double[]{Double.parseDouble(queryNode.getCpt().get(i).get("prob")), 0, 0};
            }
        }

        // calculating nominator. first computing the multiplication of the query variables (not hidden).
        // those vars values are constant during the calculation
        double nominator = 1;
        double notHiddenMul = 0;
        for (String queryVar : queryVariables) {
            if (network.get(queryVar).getEvidenceNames().size() != 0) { // if it has evidences- the prob does
                // change during the computation
                for (HashMap<String, String> line : network.get(queryVar).getCpt()) {
                    if (line.containsValue(queryTruthValue)) {
                        notHiddenMul *= Double.parseDouble(line.get("prob"));
                        multiplications++;
                        break;
                    }
                }
            }
        }
        return null;
    }

    /**
     * checking if the answer for the probabilistic inference is in the cpt
     *
     * @param queryVar the query var
     * @return if the answer is in the var cpt
     */
    private static boolean isInCpt(BayesianNetworkNode queryVar) {
        boolean inCpt = true;
        for (String evidence : evidence.keySet()) {
            if (!queryVar.getEvidenceNames().contains(evidence)) {
                return false;
            }
        }
        return true;
    }

    /**
     * implement variable elimination according the ABC
     *
     * @param query probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo2(String query) {
        return new double[]{1, 2, 5};
    }

    /**
     * implement variable elimination according a heuristic logic
     *
     * @param query probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo3(String query) {
        return new double[]{1, 2, 6};
    }

    public static void parseQuery(String fullQuery) {
        query = fullQuery.substring(2, fullQuery.indexOf("|")); // B=T
        queryVar = query.substring(0, query.indexOf("=")); // B
        queryTruthValue = query.substring(query.indexOf("=") + 1); // T
        fullGivens = fullQuery.substring(fullQuery.indexOf("|") + 1);
        givens = fullGivens.split(",");
        for (String given : givens) {
            int equalSignIndex = given.indexOf("=");
            evidence.put(given.substring(0, equalSignIndex), given.substring(equalSignIndex + 1));
        }
    }

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
                parseQuery(querySplit[0]);
                switch (querySplit[1]) {
                    case "1":
                        result = algo1(querySplit[0]);
                        break;
                    case "2":
                        result = algo2(querySplit[0]);
                        break;
                    case "3":
                        result = algo3(querySplit[0]);
                        break;
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
