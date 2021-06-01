package stage.two;

public class Server {
    public String serverName;

    public int serverId;

    public String state;

    public int currStartTime;

    public int cores;

    public int memory;

    public int disk;

    public int runningJobs;

    public int waitingJobs;

    public Server (String text) {
        String[] msg = text.split(" ");
        serverName = msg[0];
        serverId = Integer.parseInt(msg[1]);
        state = msg[2];
        currStartTime = Integer.parseInt(msg[3]);
        cores = Integer.parseInt(msg[4]);
        memory = Integer.parseInt(msg[5]);
        disk = Integer.parseInt(msg[6]);
        waitingJobs = Integer.parseInt(msg[7]);
        runningJobs = Integer.parseInt(msg[8]);
    }
}
