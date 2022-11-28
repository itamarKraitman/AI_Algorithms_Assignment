import java.io.*;
import java.util.*;

public class Ex1 {

    static HashMap<String, BayesianNetworkNode> network;

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

            while (query != null) {
                String answer = "";
                double[] result = new double[3];
                String[] querySplit = query.split("\\),");
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
