import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Ex1 {

    /** implements basic bayesian inference
     * @param query probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo1(String query) {return null;}

    /** implement variable elimination according the ABC
     * @param query probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo2(String query) {return null;}

    /** implement variable elimination according a heuristic logic
     * @param query probabilistic inference query
     * @return array with the probability, number of additions, and number of multiplications
     */
    //TODO
    public static double[] algo3(String query) {return null;}

    public static void main(String[] args) {
        String networkPath = "src/"; // for reading the net name
        ArrayList<String> queries = new ArrayList<>();
        try {
            FileReader fr = new FileReader("src/input.txt");
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("output.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            // reading the network
            networkPath += br.readLine();
            HashMap<String, BayesianNetworkNode> network = ReadNetFromXmlFile.readNetFromXml(networkPath);
            // reading line by line and compute the result by the specified algorithm
            String query = br.readLine();

            while (query != null) {
                double[] result = new double[3];
                String answer = "";
                String[] querySplit = query.split(",");
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
