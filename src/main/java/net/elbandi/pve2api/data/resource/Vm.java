package net.elbandi.pve2api.data.resource;

import net.elbandi.pve2api.data.Resource;

import org.json.JSONException;
import org.json.JSONObject;


public class Vm extends Resource {

    private final String name;
    private final int vmid;
    private final String node;
    private final int maxcpu;
    private final long maxmem;
    private final long maxdisk;
    private final int uptime;
    private final long mem;
    private final long disk;
    private final float cpu;

    public Vm(JSONObject data) throws JSONException {

        super(data);
        name = data.getString("name");
        vmid = data.getInt("vmid");
        node = data.getString("node");
        maxcpu = data.getInt("maxcpu");
        maxmem = data.getLong("maxmem");
        maxdisk = data.getLong("maxdisk");
        uptime = data.getInt("uptime");
        cpu = (float) data.getDouble("cpu");
        mem = data.getLong("mem");
        disk = data.getLong("disk");
    }

    public String getName() {

        return name;
    }


    public int getVmid() {

        return vmid;
    }


    public String getNode() {

        return node;
    }


    public int getMaxcpu() {

        return maxcpu;
    }


    public long getMaxmem() {

        return maxmem;
    }


    public long getMaxdisk() {

        return maxdisk;
    }


    public int getUptime() {

        return uptime;
    }


    public long getMem() {

        return mem;
    }


    public long getDisk() {

        return disk;
    }


    public float getCpu() {

        return cpu;
    }
}
