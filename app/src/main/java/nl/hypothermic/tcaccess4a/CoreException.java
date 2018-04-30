package nl.hypothermic.tcaccess4a;

public class CoreException extends Exception {

    private String cause;

    public CoreException(String cause) {
        super();
        this.cause = cause;
    }

    @Override
    public String toString() {
        return this.cause;
    }
}
