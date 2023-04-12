package MultiChatRoom;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 9999);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
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
            bufferedWriter.close();
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
                        bufferedWriter.write("/quit");
                        bufferedReader.close();
                        shutdown();
                    } else {
                        bufferedWriter.write(message);
                        bufferedWriter.flush();
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
