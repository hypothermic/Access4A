package nl.hypothermic.tcaccess4a;

import android.app.Activity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static nl.hypothermic.tcaccess4a.Status.*;

public class Core extends Thread implements Serializable {

    private List<ICoreListener> listeners = new ArrayList<ICoreListener>();

    public void addListener(ICoreListener listener) {
        listeners.add(listener);
    }

    protected void updateStatus(Status status) {
        this.status = status;
        for (ICoreListener l : listeners) {
            l.onStatusUpdate(status);
        }
    }

    protected void updateVerbose(String msg) {
        for (ICoreListener l : listeners) {
            l.onVerbose(msg);
        }
    }

    private static final String PROTOCOL = "v1";

    private final String addr;
    private final int port;
    private final String uuid;
    private Status status = UNINITIALIZED;

    private SSLSocket c;
    private DataOutputStream dos;
    private DataInputStream dis;

    public Core(final String addr, final int port, final String uuid) {
        this.addr = addr;
        this.port = port;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        try {
            this.init();
            this.authenticate();
            this.mainloop();
            this.updateStatus(CLOSED);
        } catch (CoreException cx) {
            this.updateStatus(CRASHED);
            this.updateVerbose(cx.toString());
            this.stop();
        } catch (Exception x) {
            this.updateStatus(CRASHED);
            x.printStackTrace();
        }
    }

    private void init() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        this.updateStatus(INITIALIZING);
        TrustManager[] trustManagers = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustManagers, new java.security.SecureRandom());
        this.updateStatus(CONNECTING);
        c = (SSLSocket) sc.getSocketFactory().createSocket(addr, port);
        c.startHandshake();
        dos = new DataOutputStream(c.getOutputStream());
        dis = new DataInputStream(c.getInputStream());
    }

    private void authenticate() throws IOException, CoreException {
        this.updateStatus(AUTHENTICATING);
        c.startHandshake();
        while (true) {
            try {
                String input = dis.readUTF();
                System.out.println("CLIENT: RECEIVED " + input);
                if (input.startsWith("ANCE|")) {
                    if (input.endsWith(PROTOCOL)) {
                        dos.writeUTF("AUTH|" + uuid);
                    } else {
                        c.close();
                        throw new CoreException("Unsupported protocol, client=" + PROTOCOL + ", server=" + input);
                    }
                }
                if (input.startsWith("AUCP|")) {
                    if (input.endsWith("DONE")) {
                        //dos.writeUTF("CMD|time set 0"); // yay it works!
                        //dos.writeUTF("LS|PLAYERS|RQ"); // TODO: uncomment when playerlist works
                        this.updateStatus(AUTH_SUCCESS);
                        break;
                    } else if (input.endsWith("ERR")) {
                        throw new CoreException("Authentication error");
                    }
                }
                if (input.startsWith("FAIL|PARAM")) {
                    throw new CoreException("Server reported param error");
                }
            } catch (EOFException eofx) {
                // eof is fine
            }
        }
    }

    private void mainloop() throws IOException, CoreException {
        this.updateStatus(ACTIVE);
        while (true) {
            try {
                String input = dis.readUTF();
                if (input.startsWith("CMD|DONE")) {
                    this.updateStatus(COMMAND_SUCCESS);
                }
                if (input.startsWith("CMD|ERR")) {
                    this.updateVerbose("Server reported error in latest command");
                }
                if (input.startsWith("LS|PLAYERS|RS")) {
                    // TODO: make player list in activity and update this
                    this.updateVerbose("Server responded with player list");
                }
                if (input.startsWith("FAIL|PARAM")) {
                    this.updateVerbose("Server reported parameter error in latest command");
                }
                if (input.startsWith("FAIL|NOCMD")) {
                    this.updateVerbose("Server reported 'no such command' error in latest command");
                }
            } catch (EOFException eofx) {
                // eof is fine
            }
        }
    }

    public boolean sendCommand(String cmd) {
        if (!c.isConnected()) {
            return false;
        }
        try {
            dos.writeUTF("CMD|" + cmd);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // TODO
    /*public String listPlayers() {
        if (!c.isConnected()) {
            return false;
        }
        try {
            dos.writeUTF("LS|PLAYERS|RQ" + cmd);
            return true;
        } catch (IOException e) {
            return false;
        }
    }*/

    @Override
    public String toString() {
        return this.addr + ":" + port;
    }
}
