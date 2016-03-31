package net.elbandi.pve2api.data.resource;

import net.elbandi.pve2api.data.Resource;

import org.json.JSONException;
import org.json.JSONObject;


public class Storage extends Resource {

    private final String storage;
    private final String node;
    private final long maxdisk;
    private final long disk;

    public Storage(JSONObject data) throws JSONException {

        super(data);
        storage = data.getString("storage");
        node = data.getString("node");
        disk = data.getLong("disk");
        maxdisk = data.getLong("maxdisk");
    }

    public String getStorage() {

        return storage;
    }


    public String getNode() {

        return node;
    }


    public long getMaxdisk() {

        return maxdisk;
    }


    public long getDisk() {

        return disk;
    }
}
