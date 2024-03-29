package cr;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.concurrent.TimeUnit;

public class TCPClient {
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private String inFromServer = "";
    private String outToServer = "";
    private BufferedReader reader = null;
    private int[] randArray = new int[10];
    private int receiveMemes = 0;
    private static long[] retreiveTime1Array = new long[100]; // memeTimes
    private static int retreiveTime1ArrayIndex = 0;
    private static long[] setupTime2Array = new long[10];
    private static int setupTime2ArrayIndex = 0;

    private static long[] totalCompletionTimes = new long[10];
    // private static long[] memeTimes = new long[100];
    private static int completionIndex = 0;
    // private static int memeIndex = 0;

    public TCPClient(String hostname, int port) {
        try {
            // Setting up connection
            System.out.println("Attempting to connect to " + hostname + " on port " + port);
            Instant startSetup = Instant.now(); // TCP setup time (2)
            socket = new Socket(hostname, port);
            Instant endSetup = Instant.now(); // TCP setup time (2)
            Duration timeElapsedSetup = Duration.between(startSetup, endSetup); // TCP setup time (2)
            long timeElapsedNanosSetup = timeElapsedSetup.toNanos(); // TCP setup time (2)
            setupTime2Array[setupTime2ArrayIndex] = timeElapsedNanosSetup; // TCP setup time (2)
            setupTime2ArrayIndex++; // TCP setup time (2)
            System.out.println(timeElapsedNanosSetup + " x 10^(-6) milliseconds");
            System.out.println("Successfully connected to " + hostname + " on port " + port);

            // Initializing input/output streams
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(System.in));

            // Server sends "Hello!" to ensure connection is established before client can
            // send a message
            inFromServer = in.readUTF();
            System.out.println("Received from server: " + inFromServer);
            /*
             * Instant start = Instant.now();
             * for (int i = 0; i < 5; i++) {
             * System.out.println("sleeping");
             * }
             * Instant end = Instant.now();
             * Duration timeElapsed = Duration.between(start, end);
             * long timeElapsedMillis = timeElapsed.toNanos();
             * System.out.println(timeElapsedMillis + " x 10^(-6) milliseconds");
             */
            while (receiveMemes < 10) {
                try {
                    Instant start = Instant.now(); // meme retrieval (1)
                    int memeNum = getRandomNum();
                    System.out.print("Request server for: " + memeNum + "\n");
                    // System.out.print("i: " + i + "\n");
                    outToServer = String.valueOf(memeNum); // send number 1-10 to server
                    out.writeUTF(outToServer);
                    out.flush();

                    //int fileSize = (int) (in.readLong());

                    //System.out.println("Received server response: " + inFromServer);

                    // Write joke file received from server to cr directory

                    byte[] fileBytes = new byte[65000];
                    String fileName = "./cr/meme" + String.valueOf(memeNum) + ".jpg";
                    // FileOutputStream fileOut = new FileOutputStream(fileName, fileName.length());
                    // FileOutputStream fileOut = new FileOutputStream("./cr/meme" +
                    // inFromServer.substring(inFromServer.length() - 9 , inFromServer.length()));
                    FileOutputStream fileOut = new FileOutputStream(fileName);
                    BufferedOutputStream fileWriter = new BufferedOutputStream(fileOut);
                    int totalBytes = in.read(fileBytes, 0, fileBytes.length);
                    fileWriter.write(fileBytes, 0, totalBytes);
                    fileWriter.close();
                    fileOut.close();

                    Instant end = Instant.now(); // meme retrieval (1)
                    Duration timeElapsed = Duration.between(start, end); // meme retrieval (1)
                    long timeElapsedNanos = timeElapsed.toNanos(); // meme retrieval (1)
                    System.out.println(timeElapsedNanos + " x 10^(-6) milliseconds \n"); // meme retrieval (1)
                    retreiveTime1Array[retreiveTime1ArrayIndex] = timeElapsedNanos; // meme retrieval (1)
                    retreiveTime1ArrayIndex++; // meme retrieval (1)
                    totalCompletionTimes[completionIndex] += timeElapsedNanos; // total time for 10 meme retreival

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            out.writeUTF("bye");
            out.flush();

            /*
             * for (int i = 0; i < retreiveTime1Array.length; i++) {
             * System.out.println((i + 1) + " " + retreiveTime1Array[i]);
             * }
             */

            // Close connection
            System.out.println("Exiting");
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }
    }

    public int getRandomNum() {
        int min = 0;
        int max = 10;
        int num = min + (int) (Math.random() * (max - min) + 1);

        while (randArray[num - 1] == 1) {
            num = min + (int) (Math.random() * (max - min) + 1);
        }
        randArray[num - 1] = 1;

        receiveMemes++;
        return num;
    }

    public static void getTestStats() {
        LongSummaryStatistics memeStats = Arrays.stream(retreiveTime1Array).summaryStatistics();
        LongSummaryStatistics totalStats = Arrays.stream(totalCompletionTimes).summaryStatistics();
        LongSummaryStatistics tcpStats = Arrays.stream(setupTime2Array).summaryStatistics();
        double sdMemes = 0;
        double sdTotals = 0;
        double sdTCP = 0;

        for (long num : retreiveTime1Array) {
            sdMemes += Math.pow(num - memeStats.getAverage(), 2);
        }
        sdMemes = Math.sqrt(sdMemes / 100);

        for (long num : totalCompletionTimes) {
            sdTotals = Math.pow(num - totalStats.getAverage(), 2);
        }
        sdTotals = Math.sqrt(sdTotals / 10);

        for (long num : setupTime2Array) {
            sdTCP += Math.pow(num - tcpStats.getAverage(), 2);
        }
        sdTCP = Math.sqrt(sdTCP / 10);

        System.out.println("Summary statistics for resolving an image across 10 trials:");
        System.out.println("Min: " + memeStats.getMin() + " nanoseconds");
        System.out.println("Max: " + memeStats.getMax() + " nanoseconds");
        System.out.println("Average: " + memeStats.getAverage() + " nanoseconds");
        System.out.println("Standard Deviation: " + sdMemes + " nanoseconds");

        System.out.println("Summary statistics for total run times across 10 trials");
        System.out.println("Min: " + totalStats.getMin() + " nanoseconds");
        System.out.println("Max: " + totalStats.getMax() + " nanoseconds");
        System.out.println("Average: " + totalStats.getAverage() + " nanoseconds");
        System.out.println("Standard Deviation: " + sdTotals + " nanoseconds");

        System.out.println("Summary statistics for tcp setup times across 10 trials");
        System.out.println("Min: " + tcpStats.getMin() + " nanoseconds");
        System.out.println("Max: " + tcpStats.getMax() + " nanoseconds");
        System.out.println("Average: " + tcpStats.getAverage() + " nanoseconds");
        System.out.println("Standard Deviation: " + sdTCP + " nanoseconds");
    }

    public static void main(String[] args) {
        try {
            String hostname = args[0];
            int port = Integer.valueOf(args[1]);
            // Call client function with given port and hostname arguments to initialize
            // client/client socket
            for (int i = 0; i < 10; i++) {
                TimeUnit.SECONDS.sleep(1);
                TCPClient client = new TCPClient(hostname, port);
                completionIndex++;
            }
            getTestStats();

        } catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }
    }
}
