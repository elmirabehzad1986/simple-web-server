package elmira;

import javax.sound.sampled.Port;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class JavaHttpServer implements Runnable {

    static final File WEB_ROOT = new File(".");
    static  final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    //port to listen connection
    static final int PORT = 8080;

    //verbose mode
    static final boolean verbose = true;

    // client connection via Socket Class
    private Socket socket;

    public JavaHttpServer(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("connection started to port: "+ PORT);

        while(true){
            JavaHttpServer javaHttpServer = new JavaHttpServer(serverSocket.accept());

            if (verbose) {
                System.out.println("connection opened at :" + new Date());
            }

            Thread thread = new Thread(javaHttpServer);
            thread.start();
        }
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter writer = null;
        BufferedOutputStream outputData = null;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(),true);
            outputData = new BufferedOutputStream(socket.getOutputStream());

            //get first line of the request
            String input = in.readLine();
            //parse the request with a string tokenizer
            StringTokenizer parsedRequest = new StringTokenizer(input);
            //get the http method
            String method = parsedRequest.nextToken().toUpperCase();
            String fileRequested = parsedRequest.nextToken().toLowerCase();

            if(!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("HEAD")){
                if(verbose){
                    System.out.println("501 not implemented method : " + method);
                }
                //return not supported file to client
                File file = new File(WEB_ROOT,METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String mimeType = "text/html";
                byte[] fileData = readFileContent(file,fileLength);

                //sending http header with data to client
                writer.println("Http/1.1 501 Not Implemented");
                writer.println("Date" + new Date());
                writer.println("Content-type :" + mimeType);
                writer.println("Content-length :" + fileLength);
                writer.println();
                writer.flush();
                outputData.write(fileData,0,fileLength);
                outputData.flush();
                return;
            }
            else {

            }

        } catch (IOException e) {
            System.err.println("Server error" + e);
        }

    }

    private byte[] readFileContent(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        try(FileInputStream fileIn = new FileInputStream(file)) {
            fileIn.read(fileData);
        }
        return fileData;
    }
}
