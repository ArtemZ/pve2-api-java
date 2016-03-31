package net.elbandi.pve2api.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Resource {

    enum Type {

        Pool,
        VmQemu,
        Node,
        Storage,
        Unknown,
        Volume
    }

    private final String id;
    private final Type type;

    public Resource(JSONObject data) throws JSONException {

        id = data.getString("id");
        type = convertType(data.getString("type"));
    }

    public String getId() {

        return id;
    }


    public Type getType() {

        return type;
    }


    public static Resource createResource(JSONObject data) throws JSONException {

        switch (convertType(data.getString("type"))) {
            case Pool:
                return new net.elbandi.pve2api.data.resource.Pool(data);

            case VmQemu:
                return new net.elbandi.pve2api.data.resource.Vm(data);

            case Node:
                return new net.elbandi.pve2api.data.resource.Node(data);

            case Storage:
                return new net.elbandi.pve2api.data.resource.Storage(data);

            default:
                return new Resource(data);
        }
    }


    public static List<Resource> getResourcesByType(List<Resource> src, Type type) {

        List<Resource> res = new ArrayList<>();

        for (Resource r : src) {
            if (r.type == type) {
                res.add(r);
            }
        }

        return res;
    }


    private static Type convertType(String name) {

        switch (name) {
            case "pool":
                return Type.Pool;

            case "qemu":
                return Type.VmQemu;

            case "node":
                return Type.Node;

            case "storage":
                return Type.Storage;

            case "volume":
                return Type.Volume;

            default:
                return Type.Unknown;
        }
    }
}
