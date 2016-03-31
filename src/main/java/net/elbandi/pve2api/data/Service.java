package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;


public class Service {

    private final String desc;
    private final String name;
    private final String service;
    private final String state;

    public Service(JSONObject data) throws JSONException {

        desc = data.getString("desc");
        name = data.getString("name");
        service = data.getString("service");
        state = data.getString("state");
    }

    public String getDesc() {

        return desc;
    }


    public String getName() {

        return name;
    }


    public String getService() {

        return service;
    }


    public String getState() {

        return state;
    }
}
