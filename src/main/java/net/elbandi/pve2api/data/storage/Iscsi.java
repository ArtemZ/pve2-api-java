package net.elbandi.pve2api.data.storage;

import net.elbandi.pve2api.Pve2Api.PveParams;
import net.elbandi.pve2api.data.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumSet;


public class Iscsi extends Storage {

    private String portal;
    private String target;

    public Iscsi(JSONObject data) throws JSONException {

        super(data);
        portal = data.getString("portal");
        target = data.getString("target");
    }


    // for create
    public Iscsi(String storage, String nodes, boolean disable, String portal, String target) {

        super(storage, EnumSet.of(Content.images), nodes, true, disable);
        this.portal = portal;
        this.target = target;
    }


    // for update
    public Iscsi(String storage, String digest, String nodes, boolean disable) {

        super(storage, digest, EnumSet.of(Content.images), nodes, true, disable);
    }

    public String getPortal() {

        return portal;
    }


    public String getTarget() {

        return target;
    }


    @Override
    public PveParams getCreateParams() {

        PveParams res = super.getCreateParams().add("type", "iscsi").add("portal", portal).add("target", target);
        res.remove("shared"); // always shared

        return res;
    }


    @Override
    public PveParams getUpdateParams() {

        PveParams res = super.getUpdateParams();
        res.remove("shared"); // always shared

        return res;
    }
}
