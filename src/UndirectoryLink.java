/**単方向リスト。Nodeクラスに格納されて使われる*/
public class UndirectoryLink {
	public int mFromNodeID = -1;
	public int mToNodeID = -1;
	public int mWeight = -1;
	
	public UndirectoryLink(int fromNodeID, int toNodeID, int weight) {
		this.mFromNodeID = fromNodeID;
		this.mToNodeID = toNodeID;
		this.mWeight = weight;
	}
}
