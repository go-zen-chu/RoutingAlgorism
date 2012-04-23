import java.util.ArrayList;


public class Node implements Cloneable{

	public int nodeID = 0;
	public ArrayList<UndirectoryLink> linkList = null;
	
	public Node(int nodeID, ArrayList<UndirectoryLink> linkList) {
		this.nodeID = nodeID;
		this.linkList = linkList;
	}
	
	/**クローンを生成する*/
	@Override
	protected Node clone() throws CloneNotSupportedException {
		return new Node(this.nodeID, this.linkList);
	}
	
	public void copyNode(Node n) {
		this.nodeID = n.nodeID;
		this.linkList = n.linkList;
	}
}
