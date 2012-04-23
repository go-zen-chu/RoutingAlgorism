/**単方向リスト。Nodeクラスに格納されて使われる*/
public class UndirectoryLink {
	public int fromNodeID = 0;
	public int toNodeID = 0;
	public int weight = 0;
	
	public UndirectoryLink(int fromNodeID, int toNodeID, int weight) {
		this.fromNodeID = fromNodeID;
		this.toNodeID = toNodeID;
		this.weight = weight;
	}
}
