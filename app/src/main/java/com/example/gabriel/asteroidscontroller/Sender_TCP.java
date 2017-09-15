package com.example.gabriel.asteroidscontroller;

import android.content.Intent;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

public class Sender_TCP extends AsyncTask<String, Void, String> {

    protected String score;
    protected String lives;
    String id;
    SplashActivity splash = null;
    JoyStickActivity joyStick = null;
    // server details
    private String host = "localhost";
    private Socket socket;
    // i/o for the client
    private BufferedReader in;
    private PrintWriter out;

    public Sender_TCP(SplashActivity splash) {

        this.splash = splash;
    }

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

            socket = new Socket(host, 7777); //6666
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("gamepad||hello");

            ServerSocket serverSock = new ServerSocket(6666);
            Socket clientSock;
            String cliAddr;

            System.out.println("Waiting for a client...");
            clientSock = serverSock.accept();
            cliAddr = clientSock.getInetAddress().getHostAddress();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

            String line;
            boolean done = false;

            while (!done) {

                if ((line = in.readLine().trim()) == null)
                    done = true;

                else {

                    System.out.println("Client msg: " + line);

                    if (line.equals("bye")) {
                        done = true;

                    } else {
                        // Packet received
                        System.out.println(getClass().getName() + ">>>Common packet received from: "
                                + cliAddr);

                        System.out.println(getClass().getName() + ">>>Packet received; data: " + line);

                        analizeMessage(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        return "executed";
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long result) {
        //showDialog("Downloaded " + result + " bytes");

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

    protected void analizeMessage(String message) {

        String[] message_parts = message.split("\\|");
        String[] head = message_parts[0].split("_");
        final String[] body = message_parts[2].split("_");

        if (head[0].equals("server")) {

            if (body[0].equals("noGames")) {

                splash.runOnUI(new Runnable() {

                    public void run() {

                        splash.setStatus("There are no games available at the moment");
                    }
                });

            } else if (body[0].equals("name")) {

                id = body[1] + "_" + body[2];

                splash.startJoystick();
            }

        } else if (head[0].equals("game")) {

            joyStick.runOnUI(new Runnable() {
                public void run() {

                    if (body[0].equals("lifes")) {

                        //lives = body[1];

                        joyStick.setLivesText(body[1]);

                    } else if (body[0].equals("newScore")) {

                        joyStick.setScoreText(body[1]);
                        //score = body[1];

                    } else if (body[0].equals("gameOver")) {

                        //joyStick.setScoreText(body[1]);
                        //score = body[1];

                        //System.exit(0);

                        joyStick.killController();

                    }
                }
            });
        }
    }

    protected void assignJoystick(JoyStickActivity joyStick) {

        this.joyStick = joyStick;
    }

    protected void sendKey(String key) {

        //out.println(key);
        new GamepadThreadedHandler_TCP(socket, id + "||" + key).start();
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
                            e.printStackTrace();
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
                //c.receive(receivePacket);

                c.setSoTimeout(5000); // set the timeout in millisecounds.

                while (!answered) { // recieve data until timeout
                    try {
                        c.receive(receivePacket);

                        // We have a response
                        System.out.println(getClass().getName() + ">>> Broadcast response from server: "
                                + receivePacket.getAddress().getHostAddress());

                        // Check if the message is correct
                        String message = new String(receivePacket.getData()).trim();

                        if (message.equals("asteroid_server_response")) {

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
                ex.printStackTrace();
                // Logger.getLogger(LoginWindow.class.getName()).log(Level.SEVERE,
                // null, ex);
            }
        }
        return IP;
    }
}
