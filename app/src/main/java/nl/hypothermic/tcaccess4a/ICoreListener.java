package nl.hypothermic.tcaccess4a;

public interface ICoreListener {

    void onStatusUpdate(Status status);

    void onVerbose(String msg);
}
