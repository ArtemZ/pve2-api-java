package net.elbandi.pve2api.data.resource;

import net.elbandi.pve2api.data.BlockDevice;
import net.elbandi.pve2api.data.VmQemu;


public class QemuDisk extends BlockDevice {

    public QemuDisk(String bus, int device) {

        super(bus + device, bus, device, "disk");
    }

    @Override
    public String getCreateString() throws VmQemu.MissingFieldException {

        StringBuilder stringBuilder = new StringBuilder();

        if (getVolume() == null)
            throw new VmQemu.MissingFieldException("Field volume is not set");

        stringBuilder.append("volume=").append(getVolume().getVolid());
        stringBuilder.append(",media=disk");

        stringBuilder.append(",size=").append(getVolume().getSize());

        return stringBuilder.toString();
    }
}
