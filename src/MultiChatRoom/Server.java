package MultiChatRoom;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcastMessage(String message) {
        for (ConnectionHandler connectionHandler : connections) {
            if (connectionHandler != null) {
                connectionHandler.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ConnectionHandler connectionHandler : connections) {
                connectionHandler.shutdown();
                connections.remove(connectionHandler);
            }
        } catch (IOException e) {

        }
    }

    class ConnectionHandler implements Runnable {
        private Socket client;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;
        private String username;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                bufferedWriter.write("Please enter your username: ");
                bufferedWriter.flush();
                username = bufferedReader.readLine();
                System.out.println(username + " connected to the room!");
                broadcastMessage(username + " joined the room!");
                String message;
                while ((message = bufferedReader.readLine()) != null) {
                    if (message.startsWith("/nick")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcastMessage(username + " renamed themselves to " + messageSplit[1]);
                            System.out.println(username + " renamed themselves to " + messageSplit[1]);
                            username = messageSplit[1];
                            bufferedWriter.write("Successfully changed username to: " + username);
                            bufferedWriter.flush();
                        } else {
                            bufferedWriter.write("No new username provided!");
                            bufferedWriter.flush();
                        }
                    } else if (message.startsWith("/quit")) {
                        shutdown();
                        broadcastMessage(username + " left the room!");
                    } else {
                        broadcastMessage(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void sendMessage(String message) {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.write(message);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void shutdown() {
            try {
                bufferedWriter.close();
                bufferedReader.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
