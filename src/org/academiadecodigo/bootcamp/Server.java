package org.academiadecodigo.bootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codecadet on 22/06/2018.
 */
public class Server {

    private ServerSocket serverSocket;
    private ExecutorService cachedPool;
    private LinkedList<ServerWorker> list;


    public Server() {
        serverSocket = getServerSocket();
        cachedPool = Executors.newCachedThreadPool();
        list = new LinkedList();
    }


    public void start() {

        try {


            while (true) {

                System.out.println("waiting for requests");

                Socket clientSocket = serverSocket.accept();

                System.out.println("Connected to: " + clientSocket.getInetAddress().getHostName() + " on port: " + clientSocket.getPort());

                ServerWorker serverWorker = new ServerWorker(clientSocket);
                cachedPool.submit(serverWorker);
                list.add(serverWorker);

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


    }

    private ServerSocket getServerSocket() {

        try {
            serverSocket = new ServerSocket(8000);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return serverSocket;

    }

    public void sendAll(String message, ServerWorker sv) {

        if (message.equals("/quit")) {
            for (ServerWorker serverWorker : list) {
                if (!serverWorker.equals(sv)) {
                    serverWorker.send(sv.nickName + " has left the chat");
                }
            }
            list.remove(sv);
            return;
        }

        if (message.equals("/status online")) {
            for (ServerWorker serverWorker : list) {
                sv.send(serverWorker.nickName + " is online");
            }
        }

        for (ServerWorker serverWorker : list) {
            if (!serverWorker.equals(sv)) {
                serverWorker.send(sv.nickName + ": " + message);
            }
        }

    }

    public class ServerWorker implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickName;

        public ServerWorker(Socket socket) {
            this.socket = socket;
            getStreams();
        }


        @Override
        public void run() {

            try {

                nickName = in.readLine();

                while (true) {

                    String message = in.readLine();

                    sendAll(message, this);
                }

            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }

        public void send(String message) {
            out.println(message);
        }

        public void getStreams() {

            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }


        }
    }

}
