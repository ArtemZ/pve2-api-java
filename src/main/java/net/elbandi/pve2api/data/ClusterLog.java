package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class ClusterLog {

    private final String msg;
    private final String node;
    private final int pid;
    private final int pri;
    private final String tag;
    private final Date time;
    private final int uid;
    private final String user;

    public ClusterLog(JSONObject data) throws JSONException {

        msg = data.getString("msg");
        node = data.getString("node");
        pid = data.getInt("pid");
        pri = data.getInt("pri");
        tag = data.getString("tag");
        time = new Date(data.getInt("time"));
        uid = data.getInt("uid");
        user = data.getString("user");
    }

    public String getMsg() {

        return msg;
    }


    public String getNode() {

        return node;
    }


    public int getPid() {

        return pid;
    }


    public int getPri() {

        return pri;
    }


    public String getTag() {

        return tag;
    }


    public Date getTime() {

        return time;
    }


    public int getUid() {

        return uid;
    }


    public String getUser() {

        return user;
    }
}
