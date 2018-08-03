package org.academiadecodigo.bootcamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by codecadet on 22/06/2018.
 */
public class Client {
    public static void main(String[] args) {

        String hostName = "127.0.0.1";
        int portNumber = Integer.parseInt("8000");
        Scanner sc = new Scanner(System.in);
        ExecutorService fixedPool = Executors.newFixedThreadPool(1);
        String name;

        try {

            Socket clientSocket = new Socket(hostName, portNumber);

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("please enter your nickname");
            name = sc.nextLine();
            out.println(name);

            while (in.readLine().equals("false")){
                System.out.println("This one is taken, please choose another nickname.");
                name = sc.nextLine();
                out.println(name);
            }

            System.out.println("Welcome! you can start chatting now");


            fixedPool.submit(new Runnable() {
                @Override
                public void run() {

                    while (true){
                        try {
                            String reply = in.readLine();
                            System.out.println(reply);

                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                }
                }
            });

            while (true) {

                String message = sc.nextLine();

                out.println(message);

                if(message.equals("/quit")){
                    System.exit(0);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }


}
