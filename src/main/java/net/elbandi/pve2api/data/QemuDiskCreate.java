package net.elbandi.pve2api.data;

/**
 * Bean that encapsulates all parameters needed for qemu disk creation.
 *
 * @author  Tobias Knell - knell@synyx.de
 */
public class QemuDiskCreate extends BlockDevice {

    private final String storage;
    private final String format;
    private final int sizeInGB;

    public QemuDiskCreate(String bus, int device, String storage, String format, int sizeInGB) {

        super(bus + device, bus, device, "disk");
        this.storage = storage;
        this.format = format;
        this.sizeInGB = sizeInGB;
    }

    @Override
    public String getStorage() {

        return storage;
    }


    public String getFormat() {

        return format;
    }


    public int getSizeInGB() {

        return sizeInGB;
    }


    @Override
    public String getCreateString() {

        StringBuilder builder = new StringBuilder();
        builder.append(storage).append(":").append(sizeInGB);
        builder.append(",format=").append(format);

        return builder.toString();
    }
}
