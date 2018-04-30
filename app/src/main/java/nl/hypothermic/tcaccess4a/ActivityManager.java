package nl.hypothermic.tcaccess4a;

public class ActivityManager {

    public static final ActivityManager INSTANCE = new ActivityManager();

    private static AuthActivity auth;

    public static void register(AuthActivity pass) {
        if (auth == null) {
            auth = pass;
        } else {
            throw new RuntimeException("Illegal attempt to replace ActivityManager");
        }
    }
}
