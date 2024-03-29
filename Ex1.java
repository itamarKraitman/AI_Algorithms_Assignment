import java.io.*;
import java.util.*;

public class Ex1 {

    static HashMap<String, BayesianNetworkNode> network;

    public static void main(String[] args) {
//        String version = System.getProperty("java.version");
//        System.out.println("java version: " + version);
//        String networkPath = "src/"; // for reading the net name
        String networkPath = ""; // for cmd
        try {
            // remember to remove src/ before running from the cmd
//            FileReader fr = new FileReader("src/input2.txt"); // dor debug
            FileReader fr = new FileReader("input.txt"); // for cmd
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter("output.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            // reading the network
            networkPath += br.readLine();
            network = ReadNetFromXmlFile.readNetFromXml(networkPath);
            // reading line by line and compute the result by the specified algorithm
            String query = br.readLine();

            while (query != null) {
                if (query.endsWith("xml")) { // read another xml input
//                    network = ReadNetFromXmlFile.readNetFromXml("src/" +query); // for debug
                    network = ReadNetFromXmlFile.readNetFromXml(query); // for cmd
                    query = br.readLine();
                }
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
                bw.write(answer + "\n");
                query = br.readLine();
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
