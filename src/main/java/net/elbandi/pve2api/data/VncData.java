package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;


public class VncData {

    private final String cert;
    private final int port;
    private final String ticket;
    private final String upid;
    private final String user;

    public VncData(JSONObject data) throws JSONException {

        cert = data.getString("cert");
        port = data.getInt("port");
        ticket = data.getString("ticket");
        upid = data.getString("upid");
        user = data.getString("user");
    }

    public String getCert() {

        return cert;
    }


    public int getPort() {

        return port;
    }


    public String getTicket() {

        return ticket;
    }


    public String getUpid() {

        return upid;
    }


    public String getUser() {

        return user;
    }
}
