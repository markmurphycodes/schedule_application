package Model;

public class Client {

    private int userId;
    private String userName;

    public Client(int id, String name) {
        this.userId = id;
        this.userName = name;
    }

    public int getUserId() { return this.userId; }

    public String getUserName() { return this.userName; }

}
