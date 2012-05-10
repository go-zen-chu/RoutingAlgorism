import java.util.ArrayList;

public class Node {

	public int mNodeID = 0;
	public ArrayList<UndirectoryLink> mLinkList = null;
	
	public Node(int nodeID, ArrayList<UndirectoryLink> linkList) {
		this.mNodeID = nodeID;
		this.mLinkList = linkList;
	}
}
