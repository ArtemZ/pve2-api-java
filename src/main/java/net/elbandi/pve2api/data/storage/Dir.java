package net.elbandi.pve2api.data.storage;

import net.elbandi.pve2api.Pve2Api.PveParams;
import net.elbandi.pve2api.data.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;


public class Dir extends Storage {

    private String path;
    int maxfiles;

    public Dir(JSONObject data) throws JSONException {

        super(data);
        path = data.getString("path");
        maxfiles = data.getInt("maxfiles");
    }


    // for create
    public Dir(String storage, EnumSet<Content> content, String nodes, boolean shared, boolean disable, String path,
        int maxfiles) {

        super(storage, content, nodes, shared, disable);
        this.path = path;
        this.maxfiles = maxfiles;
    }


    // for update
    public Dir(String storage, String digest, EnumSet<Content> content, String nodes, boolean shared, boolean disable,
        int maxfiles) {

        super(storage, digest, content, nodes, shared, disable);
        this.maxfiles = maxfiles;
    }

    public String getPath() {

        return path;
    }


    public int getMaxfiles() {

        return maxfiles;
    }


    @Override
    public PveParams getCreateParams() {

        return super.getCreateParams().add("type", "dir").add("path", path).add("maxfiles", maxfiles);
    }


    @Override
    public PveParams getUpdateParams() {

        return super.getUpdateParams().add("maxfiles", maxfiles);
    }
}
