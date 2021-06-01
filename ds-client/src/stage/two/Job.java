package stage.two;

public class Job {
    public int jobID;

    public int submitTime;

    public int estRunTime;

    public int core;

    public int memory;

    public int disk;

    public Job(String text) {
        String[] msg = text.split(" ");
        submitTime = Integer.parseInt(msg[1]);
        jobID = Integer.parseInt(msg[2]);
        estRunTime = Integer.parseInt(msg[3]);
        core = Integer.parseInt(msg[4]);
        memory = Integer.parseInt(msg[5]);
        disk = Integer.parseInt(msg[6]);
    }
}
