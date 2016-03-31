package net.elbandi.pve2api.data;

import java.text.DecimalFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class BlockDevice {

    private final String name;
    private String storage;

    /* media type, disk or cdrom */
    private String media;

    /* system interface. virtio, unused, scsi, sata or ide */
    private final String bus;
    private Volume volume;

    private int device;

    public BlockDevice(String name, String bus, int device, String media) {

        this.name = name;
        this.bus = bus;
        this.device = device;
        this.media = media;
    }

    public int getDevice() {

        return device;
    }


    public void setDevice(int device) {

        this.device = device;
    }


    public String getBus() {

        return bus;
    }


    public Volume getVolume() {

        return volume;
    }


    public void setVolume(Volume volume) {

        this.volume = volume;
    }


    public String getName() {

        return name;
    }


    public String getStorage() {

        return storage;
    }


    public String getMedia() {

        return media;
    }


    public void setMedia(String media) {

        this.media = media;
    }


    public void setStorage(String storage) {

        this.storage = storage;
    }


    public static String readableFileSize(long size) {

        if (size <= 0)
            return "0";

        final String[] units = new String[] { "B", "K", "M", "G", "T" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }


    public String getCreateString() throws VmQemu.MissingFieldException {

        StringBuilder stringBuilder = new StringBuilder();

        /*if(volume == null) throw new VmQemu.MissingFieldException("Field volume is not set");*/
        if (volume == null) {
            stringBuilder.append("none");
        } else {
            stringBuilder.append("volume=").append(volume.getVolid());
            stringBuilder.append(",size=").append(volume.getSize());
        }

        stringBuilder.append(",media=").append(media);

        return stringBuilder.toString();
    }


    public static String parseMedia(String blockDeviceData) {

        String mediaPattern = "media=(cdrom|disk)";
        Pattern r = Pattern.compile(mediaPattern);
        Matcher m = r.matcher(blockDeviceData);

        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }


    public static long parseSize(String blockDeviceData) {

        String sizePattern = "size=([0-9.KMGT]+)";
        Pattern r = Pattern.compile(sizePattern);
        Matcher m = r.matcher(blockDeviceData);
        long bytes = 0;

        if (m.find()) {
            if (m.group(1).endsWith("G")) {
                bytes = Long.parseLong(m.group(1).replace("G", "")) * 1024L * 1024L * 1024L;
            } else if (m.group(1).endsWith("T")) {
                bytes = Long.parseLong(m.group(1).replace("T", "")) * 1024L * 1024L * 1024L * 1024L;
            } else if (m.group(1).endsWith("M")) {
                bytes = Long.parseLong(m.group(1).replace("M", "")) * 1024L * 1024L;
            } else if (m.group(1).endsWith("K")) {
                bytes = Long.parseLong(m.group(1).replace("M", "")) * 1024L;
            } else {
                bytes = Long.parseLong(m.group(1));
            }
        }

        return bytes;
    }


    public static String parseStorage(String blockDeviceData) {

        String storagePattern = "^[a-z0-9_\\-.]+";
        Pattern r = Pattern.compile(storagePattern);
        Matcher m = r.matcher(blockDeviceData);

        if (m.find()) {
            return m.group(0);
        } else {
            return null;
        }
    }


    public static String parseUrl(String blockDeviceData) {

        String urlPattern = ":([a-z0-9_\\-/.]+),";
        Pattern r = Pattern.compile(urlPattern);
        Matcher m = r.matcher(blockDeviceData);

        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }
}
