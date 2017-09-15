package com.example.gabriel.asteroidscontroller;

import java.io.*;
import java.net.*;

/**
 * Created by Gabriel on 11/02/2016.
 */
public class GamepadThreadedHandler_UDP extends Thread {

    private DatagramSocket socket;
    private String ip;
    private int port;
    private String message;
    private String[] message_parts;

    public GamepadThreadedHandler_UDP(DatagramSocket socket, String ip, int port, String message) {

        this.message_parts = message.split("\\|");
        this.socket = socket;
        this.ip = ip;
        this.message = message;

        this.port = port;
    }

    public void run() {

        sendMessage();
    }

    protected void sendMessage(){

        byte[] sendData = message.getBytes();

        try {
            DatagramPacket sendPacket;

            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip), port);
            socket.send(sendPacket);
            System.out.println(getClass().getName() + ">>> "+message+" message sent to: " + ip);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
