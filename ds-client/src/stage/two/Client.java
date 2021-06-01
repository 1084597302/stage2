package stage.two;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class Client {
    private Socket socket;

    private BufferedReader br;

    private DataOutputStream dos;

    private byte[] buf;

    private List<Server> serverList;
    private Job nextJob;

    public Client(String host, int port) {
        // initialize socket connection
        try {
            socket = new Socket(host, port);
            //socket.setTcpNoDelay(true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void authorize() {
        // basic authorization for client
        sendString("HELO");
        readString();
        sendString("AUTH aaa");
        readString();
        sendString("REDY");

        // get first job from ds-sim
        nextJob = new Job(readString());
    }

    public void initServers() {
        // read and initialize information about servers
        System.out.println("------INIT SERVER------");
        serverList = new ArrayList<>();
        sendString("GETS Capable " + nextJob.core + " " + nextJob.memory + " " + nextJob.disk);
        String msg = readString();
        if (msg.equals(".")) msg = readString();
        int num = Integer.parseInt(msg.split(" ")[1]);
        System.out.println(msg);
        sendString("OK");
        String info = readString();
        String[] serversInfo = info.split("\n");

        for (String s : serversInfo) {
            System.out.println(s);
            serverList.add(new Server(s));
        }

        sendString("OK");
        readString();
        //assert ".".equals(msg);
    }

    public void schedule() {
        while (nextJob != null) {
            initServers();
            Server nextServer = serverList.get(getServerIdx());

            sendString("SCHD " + nextJob.jobID + " " +
                    nextServer.serverName + " " + nextServer.serverId);
            readString();

            while (true) {
                sendString("REDY");
                String msg = readString();
                String type = msg.substring(0, 4);
                if ("JOBN".equals(type)) {
                    nextJob = new Job(msg);
                    break;
                } else if ("NONE".equals(type)) {
                    nextJob = null;
                    break;
                }
            }
        }
    }

    private int getServerIdx() {
        initServers();
        int serverIdx = -1;
        int maxJobs = Integer.MAX_VALUE;
        while (serverIdx < 0) {
            initServers();
            for (int i = 0; i < serverList.size(); i++) {
                Server s = serverList.get(i);
                if (s.waitingJobs + s.runningJobs < maxJobs) {
                    maxJobs = s.waitingJobs + s.runningJobs;
                    serverIdx = i;
                }
            }
        }

        return serverIdx;
    }

    private String readString() {
        try{
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 1) {
                while (br.ready()) {
                    sb.append((char) br.read());
                }
            }
            return sb.toString();
        }
        catch (Exception e){

        }
        return "error found";
    }

    private void sendString(String msg) {
        try {
            dos.write(msg.getBytes(StandardCharsets.UTF_8));
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        try {
            if (socket != null && !socket.isClosed()) {
                sendString("QUIT");
                readString();
                //assert "QUIT".equals(new String(buf, 0, 4));
                socket.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
