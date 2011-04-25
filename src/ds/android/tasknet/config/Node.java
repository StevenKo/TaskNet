package ds.android.tasknet.config;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import ds.android.tasknet.task.Task;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Divya_PKV
 */
public class Node implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String nodeName;
    Integer nodeIndex;
    String nodeAddress;
    Integer nodePort;
    long memoryCapacity;
    float processorLoad;
    Integer batteryLevel;
    Map<String, Task> acceptedTasks = new HashMap<String, Task>();
    int promisedLoad;
    Date lastUpdated;

    public Node(String name, InetAddress address, Integer port) {
        nodeName = name;
        nodeIndex = -1;
        nodeAddress = address.getHostAddress();
        nodePort = port;
        memoryCapacity = 0;
        processorLoad = 0;
        batteryLevel = Preferences.TOTAL_LOAD_AT_NODE;
        promisedLoad = 0;
        lastUpdated = null;
    }

    public Integer getIndex() {
        return nodeIndex;
    }

    public String getName() {
        return nodeName;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public InetAddress getAdrress() throws UnknownHostException {
        return InetAddress.getByName(nodeAddress);
    }

    public void setNodeIndex(Integer nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public long getMemoryCapacity() {
        return memoryCapacity;
    }

    public float getProcessorLoad() {
        return processorLoad;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int value) {
        batteryLevel = value;
    }

    public void decrBatteryLevel(int value) {
        batteryLevel -= value;
    }

    public void update(long currentRAM, float CPUsage, int currentBatteryLevel) {
        memoryCapacity = currentRAM;
        processorLoad = CPUsage;
        batteryLevel = currentBatteryLevel;
        lastUpdated = Calendar.getInstance().getTime();
    }

    public void addToAcceptedTask(String taskAdvReplyId, Task task) {
        if (this.acceptedTasks.get(taskAdvReplyId) == null) {
            this.acceptedTasks.put(taskAdvReplyId, task);
        }
    }

    public void removeFromAcceptedTask(String taskId) {
        this.acceptedTasks.remove(taskId);
    }

    public Task getAcceptedTaskByTaskId(String taskId) {
        return this.acceptedTasks.get(taskId);
    }

    public int getPromisedLoad() {
        return promisedLoad;
    }

    public void setPromisedLoad(int promisedLoad) {
        this.promisedLoad = promisedLoad;
    }

    public void incrPromisedLoad(int promisedLoad) {
        this.promisedLoad += promisedLoad;
    }

    public void decrPromisedLoad(int promisedLoad) {
        this.promisedLoad -= promisedLoad;
    }

    public String toString() {
        String str = "";
        str += "Name: " + nodeName;
        str += "\nIndex: " + nodeIndex;
        str += "\nAdrress: " + nodeAddress;
        str += "\nPort: " + nodePort;
        str += "\nMemory Capacity: " + memoryCapacity;
        str += "\nProcessor Load: " + processorLoad;
        str += "\nBattery Level: " + batteryLevel;
        str += "\n";
        return str;
    }
}
