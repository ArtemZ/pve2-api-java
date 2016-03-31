package net.elbandi.pve2api.data;

import net.elbandi.pve2api.Pve2Api;
import net.elbandi.pve2api.data.resource.Adapter;
import net.elbandi.pve2api.data.resource.Cdrom;
import net.elbandi.pve2api.data.resource.QemuDisk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;


public class VmQemu {

    private int vmid;

    private String name;
    private Status vmStatus;
    private Node node;

    /* boot order [acdn]{1,4} */
    private String boot = "cdn";

    /* e.g ide0 */
    private String bootdisk;
    private int cores;
    private int sockets;

    private String desc;

    /* in mbytes */
    private int memory;

    private boolean onboot;
    private String ostype;
    private boolean kvm = true;

    private List<BlockDevice> devices = new ArrayList<>();
    private List<Adapter> adapters = new ArrayList<>();

    public VmQemu(int vmid) {

        this.vmid = vmid;
    }


    public VmQemu(int vmid, String name) {

        this.vmid = vmid;
        this.name = name;
    }


    public VmQemu(Pve2Api api, Node node, int vmid, JSONObject config, JSONObject status) throws JSONException,
        LoginException, IOException {

        this.node = node;

        name = config.getString("name");
        cores = config.getInt("cores");
        sockets = config.getInt("sockets");
        desc = config.optString("description");
        bootdisk = config.optString("bootdisk");
        boot = config.optString("boot");
        memory = config.getInt("memory");
        onboot = config.optInt("onboot") == 1;
        ostype = config.getString("ostype");
        kvm = config.optInt("kvm") == 1;
        vmStatus = new Status(status);

        this.vmid = vmid;

        for (String k : JSONObject.getNames(config)) {
            if (k.startsWith("ide") || k.startsWith("scsi") || k.startsWith("virtio")) {
                String blockDeviceString = config.optString(k);
                String storage = BlockDevice.parseStorage(blockDeviceString);
                String url = BlockDevice.parseUrl(blockDeviceString);

                if (BlockDevice.parseMedia(blockDeviceString) != null
                        && BlockDevice.parseMedia(blockDeviceString).equals("cdrom")) {
                    Cdrom cdrom = new Cdrom(k.replaceAll("[0-9]+", ""), Integer.parseInt(k.substring(k.length() - 1)));

                    if (!storage.equals("none")) {
                        cdrom.setMedia(BlockDevice.parseMedia(blockDeviceString));
                        cdrom.setStorage(storage);

                        /*if(Pve2Api.getPve2Api().getVolumeById(node.getName(), storage, storage + ":" + url) == null){
                                throw new JSONException("getVolumeById returns null, parameters: " + node.getName() + "," + storage + "," + url);
                        }*/
                        cdrom.setVolume(api.getVolumeById(node.getName(), storage, storage + ":" + url));
                    }

                    devices.add(cdrom);
                } else {
                    QemuDisk qemuDisk = new QemuDisk(k.replaceAll("[0-9]+", ""),
                            Integer.parseInt(k.substring(k.length() - 1)));
                    qemuDisk.setStorage(storage);
                    qemuDisk.setMedia(BlockDevice.parseMedia(blockDeviceString));
                    qemuDisk.setVolume(api.getVolumeById(node.getName(), storage, storage + ":" + url));

                    devices.add(qemuDisk);
                }
            } else if (k.startsWith("net")) {
                String netDeviceString = config.getString(k);
                Adapter adapter = new Adapter("net", Integer.parseInt(k.substring(3)));
                adapter.setModel(Adapter.parseModel(netDeviceString));
                adapter.setMac(Adapter.parseMac(netDeviceString));
                adapter.setBridge(Adapter.parseBridge(netDeviceString));
                adapter.setRate(Adapter.parseRate(netDeviceString));
                adapter.setTag(Adapter.parseTag(netDeviceString));

                adapters.add(adapter);
            }
        }
    }

    public Status getVmStatus() {

        return vmStatus;
    }


    public Node getNode() {

        return node;
    }


    public Map<String, String> toMap() throws DeviceException, MissingFieldException {

        Map<String, String> map = new HashMap<>();

        if (vmid != 0) {
            map.put("vmid", Integer.toString(vmid));
        }

        map.put("name", name);

        /*map.put("acpi", Boolean.toString(acpi));*/
        map.put("boot", boot);

        if (bootdisk != null && bootdisk.trim().length() != 0) {
            map.put("bootdisk", bootdisk);
        }

        if (cores > 0) {
            map.put("cores", Integer.toString(cores));
        }

        if (sockets > 0) {
            map.put("sockets", Integer.toString(sockets));
        }

        if (desc != null) {
            map.put("description", desc);
        }

        if (memory > 0) {
            map.put("memory", Integer.toString(memory));
        }

        if (onboot) {
            map.put("onboot", "1");
        } else {
            map.put("onboot", "0");
        }

        if (ostype != null) {
            map.put("ostype", ostype);
        }

        if (kvm) {
            map.put("kvm", "1");
        } else {
            map.put("kvm", "0");
        }

        for (BlockDevice device : devices) {
            map.put(device.getName(), device.getCreateString());
        }

        for (Adapter adapter : adapters) {
            map.put(adapter.getName(), adapter.getCreateString());
        }

        return map;
    }


    public String getBoot() {

        return boot;
    }


    public String getBootdisk() {

        return bootdisk;
    }


    public int getCores() {

        return cores;
    }


    public int getSockets() {

        return sockets;
    }


    public String getDesc() {

        return desc;
    }


    public int getMemory() {

        return memory;
    }


    public boolean isOnboot() {

        return onboot;
    }


    public String getOstype() {

        return ostype;
    }


    public boolean isKvm() {

        return kvm;
    }


    public String getName() {

        return name;
    }


    public int getVmid() {

        return vmid;
    }


    public List<BlockDevice> getDevices() {

        return devices;
    }


    public List<Adapter> getAdapters() {

        return adapters;
    }


    public void setName(String name) {

        this.name = name;
    }


    public void setVmStatus(Status vmStatus) {

        this.vmStatus = vmStatus;
    }


    public void setNode(Node node) {

        this.node = node;
    }


    public void setBoot(String boot) {

        this.boot = boot;
    }


    public void setBootdisk(String bootdisk) {

        this.bootdisk = bootdisk;
    }


    public void setCores(int cores) {

        this.cores = cores;
    }


    public void setSockets(int sockets) {

        this.sockets = sockets;
    }


    public void setDesc(String desc) {

        this.desc = desc;
    }


    public void setMemory(int memory) {

        this.memory = memory;
    }


    public void setOnboot(boolean onboot) {

        this.onboot = onboot;
    }


    public void setOstype(String ostype) {

        this.ostype = ostype;
    }


    public void setKvm(boolean kvm) {

        this.kvm = kvm;
    }


    public void setDevices(List<BlockDevice> devices) {

        this.devices = devices;
    }


    public void setAdapters(List<Adapter> adapters) {

        this.adapters = adapters;
    }

    public class DeviceException extends Exception {

        public DeviceException() {

            super();
        }


        public DeviceException(String message) {

            super(message);
        }


        public DeviceException(String message, Throwable cause) {

            super(message, cause);
        }


        public DeviceException(Throwable cause) {

            super(cause);
        }
    }

    public static class MissingFieldException extends Exception {

        public MissingFieldException() {

            super();
        }


        public MissingFieldException(String message) {

            super(message);
        }


        public MissingFieldException(String message, Throwable cause) {

            super(message, cause);
        }


        public MissingFieldException(Throwable cause) {

            super(cause);
        }
    }

    public static class Status {

        /* cpu - current cpu usage, %. 1 - 100 usage     */
        private final float cpu;

        /* amount of cpus */
        private final int cpus;

        /* unknown parameter */
        private final float disk;

        /* disk size, bytes */
        private final long maxdisk;

        /* amount of memory assigned to vm */
        private final long maxmem;

        /* used memory */
        private final long mem;

        /* current VM status. running, stopped */
        private final String status;
        private final int uptime;

        Status(JSONObject data) throws JSONException {

            this.cpu = (float) data.getDouble("cpu");
            this.cpus = data.getInt("cpus");
            this.disk = (float) data.getDouble("disk");

            this.maxdisk = data.getLong("maxdisk");
            this.maxmem = data.getLong("maxmem");
            this.mem = data.getLong("mem");

            this.status = data.getString("status");
            this.uptime = data.getInt("uptime");
        }

        public float getCpu() {

            return cpu;
        }


        public int getCpus() {

            return cpus;
        }


        public float getDisk() {

            return disk;
        }


        public long getMaxdisk() {

            return maxdisk;
        }


        public long getMaxmem() {

            return maxmem;
        }


        public long getMem() {

            return mem;
        }


        public String getStatus() {

            return status;
        }


        public int getUptime() {

            return uptime;
        }
    }
}
