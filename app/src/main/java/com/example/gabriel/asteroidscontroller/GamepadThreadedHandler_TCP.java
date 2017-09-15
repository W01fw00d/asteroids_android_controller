package com.example.gabriel.asteroidscontroller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Gabriel on 11/02/2016.
 */
public class GamepadThreadedHandler_TCP extends Thread {

    private Socket socket;
    private String message;
    private String[] message_parts;

    public GamepadThreadedHandler_TCP(Socket socket, String message) {

        this.message_parts = message.split("\\|");
        this.socket = socket;
        this.message = message;
    }

    public void run() {

        sendMessage();
    }

    protected void sendMessage(){

        try {
            PrintWriter out = new PrintWriter( socket.getOutputStream( ), true);

            out.println(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
