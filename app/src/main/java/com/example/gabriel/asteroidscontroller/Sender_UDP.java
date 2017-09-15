package com.example.gabriel.asteroidscontroller;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import java.io.*;
import java.net.*;
import java.util.*;

public class Sender_UDP extends AsyncTask<String, Void, String> {

    // server details
    private String host = "localhost";
    private DatagramSocket socket;

    String id;

    protected String score;
    protected String lives;

    SplashActivity splash = null;
    JoyStickActivity joyStick = null;

    // i/o for the client
    /*
    private BufferedReader in;
    private PrintWriter out;
    */

    public String getScore() {
        return score;
    }

    public String getLives() {
        return lives;
    }

    protected String doInBackground(String... urls) {

        this.host = discover_serverIP();
        System.out.println("Found IP: " + host);

        try {

            this.socket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));

            new GamepadThreadedHandler_UDP(socket, host, 7777, "gamepad||hello").start();

            while (true) {
                System.out.println("Waiting for a server message...");

                // Receive a packet
                byte[] recvBuf = new byte[15000];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);

                // Packet received
                System.out.println(getClass().getName() + ">>>Common packet received from: "
                        + packet.getAddress().getHostAddress());
                System.out.println(getClass().getName() + ">>>Packet received; data: " + new String(packet.getData()).trim());

                // See if the packet holds the right command (message)
                String message = new String(packet.getData()).trim();

                analizeMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return "executed";

        }

        protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
        //showDialog("Downloaded " + result + " bytes");

        }


    public Sender_UDP(SplashActivity splash) {

        this.splash = splash;

        //this.host = discover_serverIP();
       // System.out.println("Found IP: " + host);
    }

    private void closeLink() {
        try {
            // tell server that client is disconnecting
            //out.println("bye");
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        System.exit(0);
    }

    /*
    private void makeContact() {
        try {
            sock = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true); // autoflush
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    */

    protected void analizeMessage(String message){

        String [] message_parts = message.split("\\|");

        String[] head = message_parts[0].split("_");

        final String [] body = message_parts[2].split("_");

        if (head[0].equals("server")){

            if (body[0].equals("noGames")){

                splash.setStatus("There are no games available at the moment");

            }else if (body[0].equals("name")){

                id = body[1] + "_" + body[2];

                splash.startJoystick();
            }

        }else if (head[0].equals("game")){

            joyStick.runOnUI(new Runnable() {
                public void run() {

                    if (body[0].equals("lifes")){

                        //lives = body[1];

                        joyStick.setLivesText(body[1]);

                    }else if (body[0].equals("newScore")){

                        joyStick.setScoreText(body[1]);
                        //score = body[1];

                    }else if (body[0].equals("gameOver")) {

                        joyStick.setScoreText(body[1]);
                        //score = body[1];
                    }

                }
            });


        }
    }

    public static Handler UIHandler;

        static
        {
            UIHandler = new Handler(Looper.getMainLooper());
        }
        public static void runOnUI(Runnable runnable) {
            UIHandler.post(runnable);
        }



    protected void assignJoystick(JoyStickActivity joyStick){

        this.joyStick = joyStick;
    }

    protected void sendKey(String key){

        //out.println(key);
        new GamepadThreadedHandler_UDP(socket, host, 7777, id + "||" + key).start();
        System.out.println("Sent " + key);

    }

    public String discover_serverIP() {

        String IP = "";

        Boolean answered = false;

        // Find the server using UDP broadcast
        while (!answered) {

            try {

                DatagramSocket c;

                // Open a random port to send the package
                c = new DatagramSocket();
                c.setBroadcast(true);

                byte[] sendData = "discover_asteroid_server".getBytes();

                // Try the 255.255.255.255 first
				/*
				 * try { DatagramPacket sendPacket = new
				 * DatagramPacket(sendData, sendData.length,
				 * InetAddress.getByName("255.255.255.255"), 8888);
				 * c.send(sendPacket); System.out.println(getClass().getName() +
				 * ">>> Request packet sent to: 255.255.255.255 (DEFAULT)"); }
				 * catch (Exception e) { }
				 */

                // Broadcast the message over all the network interfaces
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback
                        // interface
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }

                        // Send the broadcast package!
                        try {
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
                            c.send(sendPacket);

                        } catch (Exception e) {
                        }

                        System.out.println(getClass().getName() + ">>> Request packet sent to: "
                                + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                    }
                }

                System.out.println(getClass().getName()
                        + ">>> Done looping over all network interfaces. Now waiting for a reply!");

                // Wait for a response
                byte[] recvBuf = new byte[15000];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                // c.receive(receivePacket);

                c.setSoTimeout(5000); // set the timeout in millisecounds.

                while (true) { // recieve data until timeout
                    try {
                        c.receive(receivePacket);

                        // We have a response
                        System.out.println(getClass().getName() + ">>> Broadcast response from server: "
                                + receivePacket.getAddress().getHostAddress());

                        // Check if the message is correct
                        String message = new String(receivePacket.getData()).trim();

                        if (message.equals("asteroid_server_response")) {
                            // DO SOMETHING WITH THE SERVER'S IP (for example,
                            // store it in
                            // your controller)
                            // Controller_Base.setServerIp(receivePacket.getAddress());

                            // new
                            // ScoreClient(receivePacket.getAddress().getHostAddress());

                            IP = receivePacket.getAddress().getHostAddress();
                        }

                        answered = true;

                        // Close the port!
                        c.close();
                    } catch (SocketTimeoutException e) {
                        // timeout exception.
                        System.out.println("Timeout reached!!! " + e);
                        c.close();
                    }
                }

            } catch (IOException ex) {
                // Logger.getLogger(LoginWindow.class.getName()).log(Level.SEVERE,
                // null, ex);
            }

        }
        return IP;
    }

}
