package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created with IntelliJ IDEA. User: artemz Date: 8/28/13 Time: 1:19 AM To change this template use File | Settings |
 * File Templates.
 */
public class Volume {

    private String filename; // required only for volume creation
    private String volid;
    private String format; // e.g qcow2
    private String content; // e.g images
    private int vmid;
    private String parent;
    private long used;
    private long size;

    public Volume(JSONObject jsonObject) throws JSONException {

        this.volid = jsonObject.getString("volid");
        this.format = jsonObject.optString("format");
        this.content = jsonObject.getString("content");
        this.used = jsonObject.optLong("used", 0);
        this.size = jsonObject.optLong("size", 0);
        this.vmid = jsonObject.optInt("vmid");
        this.parent = jsonObject.optString("parent");
    }


    public Volume(String format, String content) {

        this.format = format;
        this.content = content;
    }

    public String getFormat() {

        return format;
    }


    public String getVolid() {

        return volid;
    }


    public void setVolid(String volid) {

        this.volid = volid;
    }


    public void setFormat(String format) {

        this.format = format;
    }


    public String getContent() {

        return content;
    }


    public void setContent(String content) {

        this.content = content;
    }


    public int getVmid() {

        return vmid;
    }


    public void setVmid(int vmid) {

        this.vmid = vmid;
    }


    public String getParent() {

        return parent;
    }


    public void setParent(String parent) {

        this.parent = parent;
    }


    public long getUsed() {

        return used;
    }


    public void setUsed(long used) {

        this.used = used;
    }


    public long getSize() {

        return size;
    }


    public void setSize(long size) {

        this.size = size;
    }
}
