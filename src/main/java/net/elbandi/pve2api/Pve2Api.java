package net.elbandi.pve2api;

import net.elbandi.pve2api.RestClient.Response;
import net.elbandi.pve2api.data.AplInfo;
import net.elbandi.pve2api.data.BlockDevice;
import net.elbandi.pve2api.data.ClusterLog;
import net.elbandi.pve2api.data.Network;
import net.elbandi.pve2api.data.Node;
import net.elbandi.pve2api.data.QemuDiskUpdate;
import net.elbandi.pve2api.data.Resource;
import net.elbandi.pve2api.data.Service;
import net.elbandi.pve2api.data.Storage;
import net.elbandi.pve2api.data.Task;
import net.elbandi.pve2api.data.VmOpenvz;
import net.elbandi.pve2api.data.VmQemu;
import net.elbandi.pve2api.data.VmQemuUpdate;
import net.elbandi.pve2api.data.VncData;
import net.elbandi.pve2api.data.Volume;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;


public class Pve2Api {

    private static final RestClient REST_CLIENT = new RestClient();
    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    private final String pve_hostname;
    private final String pve_username;
    private final String pve_realm;
    private final String pve_password;

    private String pve_login_ticket;
    private String pve_login_token;
    private Date pve_login_ticket_timestamp;

    public Pve2Api(String pve_hostname, String pve_username, String pve_realm, String pve_password) {

        this.pve_hostname = pve_hostname;
        this.pve_username = pve_username;
        this.pve_password = pve_password;
        this.pve_realm = pve_realm;

        pve_login_ticket_timestamp = null;
    }

    public void login() throws JSONException, LoginException, IOException {

        String url = String.format("https://%s:8006/api2/json/access/ticket", pve_hostname);

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", pve_username));
        params.add(new BasicNameValuePair("password", pve_password));
        params.add(new BasicNameValuePair("realm", pve_realm));

        Response response = REST_CLIENT.execute(RestClient.RequestMethod.POST, url, null, params, null);

        switch (response.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:

                // Successfully connected
                JSONObject jObj = new JSONObject(response.getResponse());
                JSONObject data = jObj.getJSONObject("data");
                pve_login_ticket = data.getString("ticket");
                pve_login_token = data.getString("CSRFPreventionToken");
                pve_login_ticket_timestamp = new Date(); // decode from token

                break;

            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                throw new LoginException("Login failed. Please try again");

            default:
                throw new IOException(response.getErrorMessage());
                // error connecting to server, lets just return an error
        }
    }


    public void pve_check_login_ticket() throws LoginException, JSONException, IOException {

        if (pve_login_ticket_timestamp == null
                || pve_login_ticket_timestamp.getTime() >= (new Date()).getTime() - 3600) {
            login(); // shoud drop exception
        }
    }


    private JSONObject pve_action(String path, RestClient.RequestMethod method, Map<String, String> data)
        throws JSONException, LoginException, IOException {

        pve_check_login_ticket();

        if (!path.startsWith("/"))
            path = "/".concat(path);

        String url = String.format("https://%s:8006/api2/json%s", pve_hostname, path);

        List<NameValuePair> headers = new ArrayList<>();

        if (!method.equals(RestClient.RequestMethod.GET))
            headers.add(new BasicNameValuePair("CSRFPreventionToken", pve_login_token));

        headers.add(new BasicNameValuePair("Cookie", "PVEAuthCookie=" + pve_login_ticket));

        List<NameValuePair> params = new ArrayList<>();

        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        Response response = REST_CLIENT.execute(method, url, null, params, headers);

        switch (response.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:

                // Successfully connected
                return new JSONObject(response.getResponse());

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new LoginException(response.getErrorMessage());

            case HttpURLConnection.HTTP_BAD_REQUEST:

                // TODO: find a better exception
                LOG.warn("Response:" + response.getResponse());
                throw new IOException(response.getErrorMessage());

            default:
                throw new IOException(response.getErrorMessage());
                // error connecting to server, lets just return an error
        }
    }


    // TODO cluster adatok
    public List<ClusterLog> getClusterLog() throws JSONException, LoginException, IOException {

        List<ClusterLog> res = new ArrayList<>();
        JSONObject jObj = pve_action("/cluster/log", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new ClusterLog(data2.getJSONObject(i)));
        }

        return res;
    }


    public List<Resource> getResources() throws JSONException, LoginException, IOException {

        List<Resource> res = new ArrayList<>();
        JSONObject jObj = pve_action("/cluster/resources", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(Resource.createResource(data2.getJSONObject(i)));
        }

        return res;
    }


    /* Returns next free ID */
    public Integer nextId() throws JSONException, LoginException, IOException {

        JSONObject jsonObject = pve_action("/cluster/nextid", RestClient.RequestMethod.GET, null);

        return jsonObject.getInt("data");
    }


    public List<Task> getTasks() throws JSONException, LoginException, IOException {

        List<Task> res = new ArrayList<>();
        JSONObject jObj = pve_action("/cluster/tasks", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new Task(data2.getJSONObject(i)));
        }

        return res;
    }


    // TODO: getOptions
    // TODO: setOptions
    // TODO: getClusterStatus

    public List<Node> getNodeList() throws JSONException, LoginException, IOException {

        List<Node> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes", RestClient.RequestMethod.GET, null);
        JSONArray data2;
        data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(getNode(data2.getJSONObject(i).getString("node")));
        }

        return res;
    }


    public Node getNode(String name) throws JSONException, LoginException, IOException {

        JSONObject jObj = pve_action("/nodes/" + name + "/status", RestClient.RequestMethod.GET, null);
        JSONObject data2 = jObj.getJSONObject("data");

        return new Node(name, data2);
    }


    public List<Service> getNodeServices(String name) throws JSONException, LoginException, IOException {

        List<Service> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + name + "/services", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new Service(data2.getJSONObject(i)));
        }

        return res;
    }


    public List<AplInfo> getNodeAppliances(String node) throws JSONException, LoginException, IOException {

        List<AplInfo> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + node + "/aplinfo", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new AplInfo(data2.getJSONObject(i)));
        }

        return res;
    }


    public String downloadAppliances(String node, String storage, String template) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/aplinfo", RestClient.RequestMethod.POST,
                new PveParams("storage", storage).add("template", template));

        return jObj.getString("data");
    }


    public String startNodeVMs(String node) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/startall", RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public String stopNodeVMs(String node) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/stopall", RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public List<String> getNodeSyslog(String name) throws JSONException, LoginException, IOException {

        List<String> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + name + "/syslog", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(data2.getJSONObject(i).getString("t"));
        }

        return res;
    }


    public List<Network> getNodeNetwork(String name) throws JSONException, LoginException, IOException {

        List<Network> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + name + "/network", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new Network(data2.getJSONObject(i)));
        }

        return res;
    }


    public VncData shellNode(String node) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/vncshell", RestClient.RequestMethod.POST, null);

        return new VncData(jObj.getJSONObject("data"));
    }


    public List<Storage> getStorages() throws JSONException, LoginException, IOException {

        List<Storage> res = new ArrayList<>();
        JSONObject jObj = pve_action("/storage", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(Storage.createStorage(data2.getJSONObject(i)));
        }

        return res;
    }


    public List<Volume> getVolumes(String node, String storage) throws JSONException, LoginException, IOException {

        List<Volume> volumes = new ArrayList<>();
        JSONObject jsonObject = pve_action("/nodes/" + node + "/storage/" + storage + "/content",
                RestClient.RequestMethod.GET, null);
        JSONArray jsonArray = jsonObject.getJSONArray("data");

        for (int i = 0; i < jsonArray.length(); i++) {
            volumes.add(new Volume(jsonArray.getJSONObject(i)));
        }

        return volumes;
    }


    public Volume getVolumeById(String node, String storage, String id) throws JSONException, LoginException,
        IOException {

        List<Volume> volumes = getVolumes(node, storage);
        Volume volumeToReturn = null;

        for (Volume volume : volumes) {
            if (volume.getVolid().equals(id)) {
                volumeToReturn = volume;
            }
        }

        return volumeToReturn;
    }


    public void createStorage(Storage storage) throws LoginException, JSONException, IOException {

        Map<String, String> data = storage.getCreateParams();
        pve_action("/storage", RestClient.RequestMethod.POST, data);
    }


    public void updateStorage(Storage storage) throws LoginException, JSONException, IOException {

        Map<String, String> data = storage.getUpdateParams();
        pve_action("/storage/" + storage.getStorage(), RestClient.RequestMethod.PUT, data);
    }


    public void deleteStorage(String storage) throws LoginException, JSONException, IOException {

        pve_action("/storage/" + storage, RestClient.RequestMethod.DELETE, null);
    }


    public Volume createVolume(String node, String storage, String filename, String size, Integer vmid, String format)
        throws LoginException, JSONException, IOException, VmQemu.MissingFieldException {

        Map<String, String> data = new HashMap<>();
        data.put("filename", filename);
        data.put("size", size);
        data.put("vmid", Integer.toString(vmid));
        data.put("format", format);

        JSONObject jsonObject = pve_action("/nodes/" + node + "/storage/" + storage + "/content",
                RestClient.RequestMethod.POST, data);

        return getVolumeById(node, storage, jsonObject.getString("data"));
    }


    public void assignDiskToQemu(int vmid, String node, BlockDevice blockDevice) throws JSONException, LoginException,
        IOException, VmQemu.MissingFieldException {

        Map<String, String> data = new HashMap<>();
        data.put(blockDevice.getBus() + Integer.toString(blockDevice.getDevice()), blockDevice.getCreateString());

        pve_action("/nodes/" + node + "/qemu/" + Integer.toString(vmid) + "/config", RestClient.RequestMethod.PUT,
            data);
    }


    public void resizeQemuDisk(int vmid, String node, QemuDiskUpdate diskUpdate) throws JSONException, LoginException,
        IOException {

        Map<String, String> data = diskUpdate.toMap();
        pve_action("/nodes/" + node + "/qemu/" + Integer.toString(vmid) + "/resize", RestClient.RequestMethod.PUT,
            data);
    }


    public List<VmQemu> getQemuVMs(String node) throws JSONException, LoginException, IOException {

        List<VmQemu> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + node + "/qemu", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(getQemuVM(node, data2.getJSONObject(i).getInt("vmid")));
        }

        return res;
    }


    public VmQemu getQemuVM(String node, int vmid) throws JSONException, LoginException, IOException {

        JSONObject config = pve_action("/nodes/" + node + "/qemu/" + vmid + "/config", RestClient.RequestMethod.GET,
                null);
        JSONObject status = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/current",
                RestClient.RequestMethod.GET, null);

        return new VmQemu(this, getNode(node), vmid, config.getJSONObject("data"), status.getJSONObject("data"));
    }


    /*public void getQemuConfig(String node, int vmid, VmQemu vm) throws JSONException,
                    LoginException, IOException {
            JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/config",
                            RestClient.RequestMethod.GET, null);
            vm.SetConfig(jObj.getJSONObject("data"));
    }*/

    public String startQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/start",
                RestClient.RequestMethod.POST, null);

        // FIXME: already running?
        return jObj.getString("data");
    }


    protected String stopQemu(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/stop", RestClient.RequestMethod.POST,
                data);

        return jObj.getString("data");
    }


    public String stopQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        return stopQemu(node, vmid, null);
    }


    public String stopQemu(String node, int vmid, boolean keepActive) throws LoginException, JSONException,
        IOException {

        return stopQemu(node, vmid, new PveParams("keepActive", keepActive));
    }


    public String stopQemu(String node, int vmid, int timeout) throws LoginException, JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return stopQemu(node, vmid, new PveParams("timeout", timeout));
    }


    public String stopQemu(String node, int vmid, int timeout, boolean keepActive) throws LoginException, JSONException,
        IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return stopQemu(node, vmid, new PveParams("timeout", timeout).add("keepActive", keepActive));
    }


    public String resetQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/reset",
                RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public String createQemu(VmQemu vm) throws LoginException, JSONException, IOException, VmQemu.DeviceException,
        VmQemu.MissingFieldException {

        Map<String, String> params = vm.toMap(); // adding this to make it throw MissingFieldException right now in case node is not specified
        JSONObject jsonObject = pve_action("/nodes/" + vm.getNode().getName() + "/qemu", RestClient.RequestMethod.POST,
                params);

        return jsonObject.getString("data");
    }


    public void updateQemu(VmQemu vm) throws LoginException, JSONException, IOException, VmQemu.DeviceException,
        VmQemu.MissingFieldException {

        pve_action("/nodes/" + vm.getNode().getName() + "/qemu/" + vm.getVmid() + "/config",
            RestClient.RequestMethod.PUT, vm.toMap());
    }


    /**
     * @param  nodeName  the name of the node the VM runs on.
     * @param  vmId  the id of the vm to update
     * @param  update  the update data
     * @param  async  true if request should return immediately, false if it should wait for finished update.
     *
     * @throws  JSONException
     * @throws  LoginException
     * @throws  IOException
     */
    public void updateQemu(String nodeName, int vmId, VmQemuUpdate update, boolean async) throws JSONException,
        LoginException, IOException {

        RestClient.RequestMethod method = RestClient.RequestMethod.PUT;

        if (async) {
            method = RestClient.RequestMethod.POST;
        }

        pve_action(String.format("/nodes/%s/qemu/%d/config", nodeName, vmId), method, update.toMap());
    }


    public String deleteQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid, RestClient.RequestMethod.DELETE, null);

        return jObj.getString("data");
    }


    protected String unlinkQemu(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/unlink", RestClient.RequestMethod.PUT,
                data);

        return jObj.getString("data");
    }


    public String unlinkQemu(String node, int vmid, String idlist) throws LoginException, JSONException, IOException {

        return unlinkQemu(node, vmid, new PveParams("idlist", idlist));
    }


    public String unlinkQemu(String node, int vmid, String idlist, boolean force) throws LoginException, JSONException,
        IOException {

        return unlinkQemu(node, vmid, new PveParams("idlist", idlist).add("force", force));
    }


    protected String shutdownQemu(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/shutdown",
                RestClient.RequestMethod.POST, data);

        return jObj.getString("data");
    }


    public String shutdownQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        return shutdownQemu(node, vmid, null);
    }


    public String shutdownQemu(String node, int vmid, int timeout) throws LoginException, JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return shutdownQemu(node, vmid, new PveParams("timeout", timeout));
    }


    public String shutdownQemu(String node, int vmid, int timeout, boolean keepActive) throws LoginException,
        JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return shutdownQemu(node, vmid, new PveParams("timeout", timeout).add("keepActive", keepActive));
    }


    public String shutdownQemu(String node, int vmid, int timeout, boolean keepActive, boolean forceStop)
        throws LoginException, JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return shutdownQemu(node, vmid, new PveParams("timeout", timeout).add("keepActive", keepActive)
                .add("forceStop", forceStop));
    }


    public VncData consoleQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/vncproxy", RestClient.RequestMethod.POST,
                null);

        return new VncData(jObj.getJSONObject("data"));
    }


    public String suspendQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/suspend",
                RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public String resumeQemu(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/status/resume",
                RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public String sendkeyQemu(String node, int vmid, String key) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/sendkey", RestClient.RequestMethod.PUT,
                new PveParams("key", key));

        return jObj.getString("data");
    }


    protected String migrateQemu(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/migrate", RestClient.RequestMethod.POST,
                data);

        return jObj.getString("data");
    }


    public String migrateQemu(String node, int vmid, String target) throws LoginException, JSONException, IOException {

        return migrateQemu(node, vmid, new PveParams("target", target));
    }


    public String migrateQemu(String node, int vmid, String target, boolean online) throws LoginException,
        JSONException, IOException {

        return migrateQemu(node, vmid, new PveParams("target", target).add("online", online));
    }


    public String migrateQemu(String node, int vmid, String target, boolean online, boolean force)
        throws LoginException, JSONException, IOException {

        return migrateQemu(node, vmid, new PveParams("target", target).add("online", online).add("force", force));
    }


    public String monitorQemu(String node, int vmid, String command) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/monitor", RestClient.RequestMethod.POST,
                new PveParams("command", command));

        return jObj.getString("data");
    }


    public String rollbackQemu(String node, int vmid, String snapname) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/qemu/" + vmid + "/snapshot/" + snapname + "/rollback",
                RestClient.RequestMethod.POST, null);

        return jObj.getString("data");
    }


    public List<VmOpenvz> getOpenvzCTs(String node) throws JSONException, LoginException, IOException {

        List<VmOpenvz> res = new ArrayList<>();
        JSONObject jObj = pve_action("/nodes/" + node + "/openvz", RestClient.RequestMethod.GET, null);
        JSONArray data2 = jObj.getJSONArray("data");

        for (int i = 0; i < data2.length(); i++) {
            res.add(new VmOpenvz(data2.getJSONObject(i)));
        }

        return res;
    }


    public VmOpenvz getOpenvzCT(String node, int vmid) throws JSONException, LoginException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/current",
                RestClient.RequestMethod.GET, null);

        return new VmOpenvz(jObj.getJSONObject("data"));
    }


    public void getOpenvzConfig(String node, int vmid, VmOpenvz vm) throws JSONException, LoginException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/config", RestClient.RequestMethod.GET,
                null);
        vm.SetConfig(jObj.getJSONObject("data"));
    }


    public String createOpenvz(VmOpenvz vm) throws LoginException, JSONException, IOException {

        return createOpenvz(vm.getNode(), vm);
    }


    public String createOpenvz(String node, VmOpenvz vm) throws LoginException, JSONException, IOException {

        Map<String, String> parameterData = vm.getCreateParams();

        String path = "/nodes/" + node + "/openvz";
        JSONObject jsonObject = pve_action(path, RestClient.RequestMethod.POST, parameterData);

        return jsonObject.getString("data");
    }


    public String updateOpenvz(String node, VmOpenvz vm) throws LoginException, JSONException, IOException {

        return createOpenvz(node, vm);
    }


    protected Map<Integer, String> initlogOpenvz(String node, int vmid, Map<String, String> data) throws LoginException,
        JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/initlog", RestClient.RequestMethod.GET,
                data);
        JSONArray data2 = jObj.getJSONArray("data");
        Map<Integer, String> res = new HashMap<>();

        for (int i = 0; i < data2.length(); i++) {
            JSONObject o = data2.getJSONObject(i);
            res.put(o.getInt("n"), o.getString("t"));
        }

        return res;
    }


    public Map<Integer, String> initlogOpenvz(String node, int vmid, int start) throws LoginException, JSONException,
        IOException {

        if (start < 0)
            throw new IllegalArgumentException("Start paramter need to be positive");

        return initlogOpenvz(node, vmid, new PveParams("start", start));
    }


    public Map<Integer, String> initlogOpenvz(String node, int vmid, int start, int limit) throws LoginException,
        JSONException, IOException {

        if (start < 0)
            throw new IllegalArgumentException("Start paramter need to be positive");

        if (limit < 0)
            throw new IllegalArgumentException("Limit paramter need to be positive");

        return initlogOpenvz(node, vmid, new PveParams("start", start).add("limit", limit));
    }


    protected String deleteOpenvz(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid, RestClient.RequestMethod.DELETE, null);

        return jObj.getString("data");
    }


    public VncData consoleOpenvz(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/vncproxy", RestClient.RequestMethod.POST,
                null);

        return new VncData(jObj.getJSONObject("data"));
    }


    // TODO: status/ubc, ???

    public String startOpenvz(String node, int vmid) throws LoginException, JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/start",
                RestClient.RequestMethod.POST, null);

        // FIXME: already running?
        return jObj.getString("data");
    }


    protected String stopOpenvz(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/stop",
                RestClient.RequestMethod.POST, data);

        return jObj.getString("data");
    }


    protected String mountOpenvz(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/mount",
                RestClient.RequestMethod.POST, data);

        return jObj.getString("data");
    }


    protected String umountOpenvz(String node, int vmid, Map<String, String> data) throws LoginException, JSONException,
        IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/umount",
                RestClient.RequestMethod.POST, data);

        return jObj.getString("data");
    }


    protected String shutdownOpenvz(String node, int vmid, Map<String, String> data) throws LoginException,
        JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/status/shutdown",
                RestClient.RequestMethod.POST, data);

        return jObj.getString("data");
    }


    public String shutdownOpenvz(String node, int vmid) throws LoginException, JSONException, IOException {

        return shutdownOpenvz(node, vmid, null);
    }


    public String shutdownOpenvz(String node, int vmid, int timeout) throws LoginException, JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return shutdownOpenvz(node, vmid, new PveParams("timeout", timeout));
    }


    public String shutdownOpenvz(String node, int vmid, int timeout, boolean forceStop) throws LoginException,
        JSONException, IOException {

        if (timeout < 0)
            throw new IllegalArgumentException("Timeout paramter need to be positive");

        return shutdownOpenvz(node, vmid, new PveParams("timeout", timeout).add("forceStop", forceStop));
    }


    protected String migrateOpenvz(String node, int vmid, Map<String, String> data) throws LoginException,
        JSONException, IOException {

        JSONObject jObj = pve_action("/nodes/" + node + "/openvz/" + vmid + "/migrate", RestClient.RequestMethod.POST,
                data);

        return jObj.getString("data");
    }


    public String migrateOpenvz(String node, int vmid, String target) throws LoginException, JSONException,
        IOException {

        return migrateOpenvz(node, vmid, new PveParams("target", target));
    }


    public String migrateOpenvz(String node, int vmid, String target, boolean online) throws LoginException,
        JSONException, IOException {

        return migrateOpenvz(node, vmid, new PveParams("target", target).add("online", online));
    }

    // TODO: refactor to use BasicNameValuePair
    public static class PveParams extends HashMap<String, String> {

        private static final long serialVersionUID = 1L;

        public PveParams(String key, String value) {

            PveParams.this.add(key, value);
        }


        public PveParams(String key, int value) {

            PveParams.this.add(key, value);
        }


        public PveParams(String key, boolean value) {

            add(key, value);
        }

        public PveParams add(String key, String value) {

            if (value != null)
                put(key, value);

            return this;
        }


        public PveParams add(String key, int value) {

            put(key, "" + value);

            return this;
        }


        public PveParams add(String key, boolean value) {

            put(key, value ? "1" : "0");

            return this;
        }
    }
}
