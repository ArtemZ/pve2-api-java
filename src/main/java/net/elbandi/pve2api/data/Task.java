package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


public class Task {

    private final Date endtime;
    private final String id;
    private final String node;
    private final boolean saved;
    private final Date starttime;
    private final String status;
    private final String type;
    private final String upid;
    private final String user;

    public Task(JSONObject data) throws JSONException {

        endtime = new Date(data.getInt("endtime"));
        id = data.getString("id");
        node = data.getString("node");
        saved = data.getString("saved").equals("1");
        starttime = new Date(Integer.parseInt(data.getString("starttime")));
        status = data.getString("status");
        type = data.getString("type");
        upid = data.getString("upid");
        user = data.getString("user");
    }

    public Date getEndtime() {

        return endtime;
    }


    public String getId() {

        return id;
    }


    public String getNode() {

        return node;
    }


    public boolean isSaved() {

        return saved;
    }


    public Date getStarttime() {

        return starttime;
    }


    public String getStatus() {

        return status;
    }


    public String getType() {

        return type;
    }


    public String getUpid() {

        return upid;
    }


    public String getUser() {

        return user;
    }
}
