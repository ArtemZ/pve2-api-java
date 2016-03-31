package net.elbandi.pve2api.data;

import net.elbandi.pve2api.data.resource.Adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Bean that encapsulates the parameters for an update of a Qemu VM.
 *
 * @author  Tobias Knell - knell@synyx.de
 */
public class VmQemuUpdate {

    /**
     * The boot order. Format: [acdn]{1,4} - Boot on floppy (a), hard disk (c), CD-ROM (d), or network (n).
     */
    private String boot;

    /**
     * The specific disk to boot from. Name of the disk.
     */
    private String bootdisk;

    /**
     * The number of cpu cores.
     */
    private Integer cores;

    /**
     * Description of the vm.
     */
    private String description;

    /**
     * Size of RAM in MB.
     */
    private Integer memory;

    private List<Adapter> networkAdapters;

    public String getBoot() {

        return boot;
    }


    public void setBoot(String boot) {

        this.boot = boot;
    }


    public String getBootdisk() {

        return bootdisk;
    }


    public void setBootdisk(String bootdisk) {

        this.bootdisk = bootdisk;
    }


    public Integer getCores() {

        return cores;
    }


    public void setCores(Integer cores) {

        this.cores = cores;
    }


    public String getDescription() {

        return description;
    }


    public void setDescription(String description) {

        this.description = description;
    }


    public Integer getMemory() {

        return memory;
    }


    public void setMemory(Integer memory) {

        this.memory = memory;
    }


    public List<Adapter> getNetworkAdapters() {

        return networkAdapters;
    }


    public void setNetworkAdapters(List<Adapter> networkAdapters) {

        this.networkAdapters = networkAdapters;
    }


    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<>();

        if (boot != null) {
            map.put("boot", boot);
        }

        if (bootdisk != null) {
            map.put("bootdisk", boot);
        }

        if (cores != null && cores > 0) {
            map.put("cores", cores.toString());
        }

        if (description != null) {
            map.put("description", description);
        }

        if (memory != null && memory > 0) {
            map.put("memory", memory.toString());
        }

        if (networkAdapters != null && !networkAdapters.isEmpty()) {
            for (Adapter adapter : networkAdapters) {
                map.put(adapter.getName(), adapter.getCreateString());
            }
        }

        return map;
    }
}
