package stage.two;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private Socket socket;

    private char[] buf;

    private List<Server> serverList;
    private List<Integer> weights;
    private Job nextJob;
    private int totalWeight;

    public Client(String host, int port) {
        // initialize socket connection
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        buf = new char[1024*1024];
    }

    public void authorize() {
        // basic authorization for client
        sendString("HELO");
        readBuf();
        sendString("AUTH aaa");
        readBuf();
        sendString("REDY");

        // get first job from ds-sim
        nextJob = new Job(readString());
    }

    public void initServers() {
        // read and initialize information about servers
        serverList = new ArrayList<>();
        weights = new ArrayList<>();
        sendString("GETS All");
        readBuf();
        sendString("OK");
        String info = readString();
        String[] serversInfo = info.split("\n");

        for (String s : serversInfo) {
            Server isv = new Server(s);
            serverList.add(isv);
            if (weights.isEmpty()) {
                weights.add(isv.cores);
            } else {
                weights.add(isv.cores + weights.get(weights.size()-1));
            }
        }
        totalWeight = weights.get(weights.size()-1);
        sendString("OK");
        readBuf();
    }

    public void schedule() {
        while (nextJob != null) {
            Server nextServer = serverList.get(getServerIdxByWeight());
            while (nextJob.core > nextServer.cores ||
                    nextJob.memory > nextServer.memory ||
                    nextJob.disk > nextServer.disk) {
                nextServer = serverList.get(getServerIdxByWeight());
            }

            sendString("SCHD " + nextJob.jobID + " " +
                    nextServer.serverName + " " + nextServer.serverId);
            readBuf();

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
                } else {
                    //sendString("OK");
                }
            }
        }
    }

    private int getServerIdxByWeight() {
        // generate a random number in [0, totalWeight]
        int randNum = (int) (Math.random() * totalWeight);

        // determine server index
        int serverIdx = totalWeight-1;
        for (int i = 0; i < weights.size(); i++) {
            if (randNum < weights.get(i)) {
                serverIdx = i;
                break;
            }
        }

        return serverIdx;
    }

    private String readString() {
        int offset = readBuf();
        return new String(buf, 0, offset);
    }

    private int readBuf() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int cnt = reader.read(buf);
            return cnt;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private void sendString(String msg) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            writer.write(msg);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean close() {
        try {
            if (socket != null && !socket.isClosed()) {
                sendString("QUIT");
                readBuf();
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
