package Model;


public class User {

    private static int userId;

    public static void setUserId(int userId)
    {
        User.userId = userId;
    }

    public static int getUserId() { return User.userId; }

    private static User instance;

    private User () {}

    static
    {
        instance = new User();
    }


}
