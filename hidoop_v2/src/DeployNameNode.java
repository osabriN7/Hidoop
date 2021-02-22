import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeployNameNode {

    public static void main(String[] args) {
        BufferedReader reader;
        String user = "mdahhoum";
        String nameNode = "scorpion";

        // Lancer le NameNode sur la machine azote
        String[] cmd = { "xterm", "-e", "ssh","-o", "StrictHostKeyChecking=no", user + "@" + nameNode, "java", "-cp", "Bureau/hidoop.jar",
                "hdfs.NameNodeImpl" };
        ProcessBuilder pb1 = new ProcessBuilder(cmd);
        try {
            Process process1 = pb1.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("NameNode deploye");


    }
    
}