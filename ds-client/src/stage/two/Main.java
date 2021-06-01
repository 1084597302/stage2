package stage.two;


public class Main {

    public static void main(String[] args) {
        Client c = new Client("127.0.0.1", 50000);

        c.authorize();
        c.initServers();
        c.schedule();

        c.close();
    }
}
