package ros_java.ardrone_ui;

import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import std_msgs.Empty;

/**
 * Created by ziquan on 2/9/15.
 */
public class EmptyPublisher implements NodeMain {
    private GraphName name;
    private Publisher<Empty> publisher;

    EmptyPublisher(Publisher<Empty> publisher, String name) {
        this.publisher = publisher;
        this.name = GraphName.of(name);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return name;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        publisher = connectedNode.newPublisher(name, Empty._TYPE);
    }

    @Override
    public void onShutdown(Node node) {

    }

    @Override
    public void onShutdownComplete(Node node) {

    }

    @Override
    public void onError(Node node, Throwable throwable) {

    }
}
