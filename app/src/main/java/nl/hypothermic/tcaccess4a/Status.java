package nl.hypothermic.tcaccess4a;

public enum Status {
    UNINITIALIZED,
    INITIALIZING,
    CONNECTING,
    AUTHENTICATING,
    ACTIVE,
    CLOSED,
    CRASHED,
    AUTH_SUCCESS,
    COMMAND_SUCCESS,
}
