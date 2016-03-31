package net.elbandi.pve2api.data.storage;

import net.elbandi.pve2api.Pve2Api.PveParams;
import net.elbandi.pve2api.data.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;


public class Lvm extends Storage {

    private String vgname;
    private boolean saferemove;

    public Lvm(JSONObject data) throws JSONException {

        super(data);
        vgname = data.getString("vgname");
        saferemove = data.optInt("saferemove") == 1;
    }


    // for create
    public Lvm(String storage, EnumSet<Content> content, String nodes, boolean shared, boolean disable, String vgname) {

        super(storage, content, nodes, shared, disable);
        this.vgname = vgname;
    }


    // for update
    public Lvm(String storage, String digest, EnumSet<Content> content, String nodes, boolean shared, boolean disable) {

        super(storage, digest, content, nodes, shared, disable);
    }

    public String getVgname() {

        return vgname;
    }


    public boolean isSaferemove() {

        return saferemove;
    }


    @Override
    public PveParams getCreateParams() {

        return super.getCreateParams().add("type", "lvm").add("vgname", vgname);
    }
}
