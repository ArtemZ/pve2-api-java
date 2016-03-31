package net.elbandi.pve2api.data;

import java.util.HashMap;
import java.util.Map;


/**
 * Bean that encapsulates all parameters needed for qemu disk creation.
 *
 * @author  Tobias Knell - knell@synyx.de
 */
public class QemuDiskUpdate {

    private final String bus;
    private final int device;
    private final int sizeInGB;

    public QemuDiskUpdate(String bus, int device, int sizeInGB) {

        this.bus = bus;
        this.device = device;
        this.sizeInGB = sizeInGB;
    }

    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<>();

        map.put("disk", bus + device);
        map.put("size", sizeInGB + "G");

        return map;
    }
}
