package org.academiadecodigo.bootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codecadet on 22/06/2018.
 */
public class Server {

    private ServerSocket serverSocket;
    private ExecutorService cachedPool;
    private List<ServerWorker> list;


    public Server() {
        serverSocket = getServerSocket();
        cachedPool = Executors.newCachedThreadPool();
        list = Collections.synchronizedList(new ArrayList<>());
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

    private synchronized void sendAll(String message, ServerWorker sv) {

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

        if (message.contains("/private")) {

            //TODO StringBuilder
            String[] builderMessage = message.split(" ");
            String privateMessage = "";

            for (int i = 2; i < builderMessage.length; i++) {
                privateMessage += builderMessage[i] + " ";
            }

            for (ServerWorker serverWorker : list) {
                if (serverWorker.getNickName().equals(builderMessage[1])) {
                    serverWorker.send("PRIVATE/ " + sv.getNickName() + ": " + privateMessage);
                }
            }
            return;
        }

        for (ServerWorker serverWorker : list) {
            if (!serverWorker.equals(sv)) {
                serverWorker.send(sv.nickName + ": " + message);
            }
        }

    }

    public synchronized boolean checkNickname(String nickName, ServerWorker sv) {
        for (ServerWorker serverWorker : list) {
            if (serverWorker.getNickName().equals(nickName) && !serverWorker.equals(sv)) {
                return false;
            }
        }
        return true;
    }


    public class ServerWorker implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String nickName;

        public ServerWorker(Socket socket) {
            this.socket = socket;
            this.nickName = "";
            getStreams();
        }

        public String getNickName() {
            return nickName;
        }

        @Override
        public void run() {

            try {

                nickName = in.readLine();

                while (!checkNickname(nickName, this)){
                out.println("false");
                nickName = in.readLine();
                }

                out.println("true");

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
