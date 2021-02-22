package hdfs;

import java.io.Serializable;



public class Node implements Serializable {
    private String hostName;
    private int port;

    public Node(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;

    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPort() {
        return this.port;
    }

  @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        if(this.getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return (port == node.port)&&(hostName.equals(node.hostName));

    }
  @Override
    public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime *result + (int) (port ^ (port >>> 32));
    result = prime *result + ((hostName== null) ? 0 : hostName.hashCode());
    return result;
}

}
