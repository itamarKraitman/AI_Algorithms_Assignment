import java.io.*;
import java.util.*;

public class Ex1 {

    static HashMap<String, BayesianNetworkNode> network;

    public static void main(String[] args) {
//        String version = System.getProperty("java.version");
//        System.out.println("java version: " + version);
        String networkPath = ""; // for reading the net name
        ArrayList<String> queries = new ArrayList<>();
        try {
            // remember to remove src/ before running from the cmd
            FileReader fr = new FileReader("input2.txt");
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("output.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            // reading the network
            networkPath += br.readLine();
            network = ReadNetFromXmlFile.readNetFromXml(networkPath);
            // reading line by line and compute the result by the specified algorithm
            String query = br.readLine();

            while (query != null) {
                String answer = "";
                double[] result = new double[3];
                String[] querySplit = query.split("\\),");
                switch (querySplit[1]) {
                    case "1":
                        result = new basicInference(querySplit[0], network).getSolution();
                        break;
                    case "2":
                        result = new variableElimination(querySplit[0], network, true).getSolution();
                        break;
                    case "3":
                        result = new variableElimination(querySplit[0], network, false).getSolution();
                        break;
                }
                answer += (result[0] + "," + (int) result[1] + "," + (int) result[2]);
                bw.write(answer+"\n");
                query = br.readLine();
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
