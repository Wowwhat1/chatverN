package MultiChatRoom;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean done;

    @Override
    public void run() {
        try {
            System.out.println("Not connected");
            client = new Socket("localhost", 8888);
            System.out.println("Connected");
            printWriter = new PrintWriter(client.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inputMessage;
            while (!done && (inputMessage = bufferedReader.readLine()) != null) {
                System.out.println(inputMessage);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            bufferedReader.close();
            printWriter.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {

        }
    }

    class InputHandler implements Runnable {

        @Override
        public void run() {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = bufferedReader.readLine();
                    if (message.equals("/quit")) {
                        printWriter.println("/quit");
                        bufferedReader.close();
                        shutdown();
                    } else {
                        printWriter.write(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
