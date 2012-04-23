import java.util.ArrayList;

public class Route extends UndirectoryLink implements Cloneable {
	ArrayList<UndirectoryLink> linkList = null;
	
	public Route(int fromNodeID, int toNodeID, int weight) {
		super(fromNodeID, toNodeID, weight); //ここで代入?
		this.linkList = new ArrayList<UndirectoryLink>();
	}

	public Route(int fromNodeID, int toNodeID, int weight,
						ArrayList<UndirectoryLink> linkList) {
		super(fromNodeID, toNodeID, weight);
		this.linkList = linkList;
	}

	/**値の追加*/
	public Route updateRoute(UndirectoryLink link) {
		// 次のノードはリンク先に等しい
		this.toNodeID = link.toNodeID;
		// コストは前ルートのコストにリンクのコストを足したもの
		this.weight += link.weight;
		this.linkList.add(link);
		return this;
	}
	
	/**今までのルートにリンクを追加して、新しいルートを作る。同時にリンクの先とコストを変化させる */
	public Route makeNewRoute(UndirectoryLink link) {
		// リンクのリストをクローンする
		@SuppressWarnings("unchecked")
		ArrayList<UndirectoryLink> linkList 
			= (ArrayList<UndirectoryLink>) this.linkList.clone();
		linkList.add(link);
		// 次のノードはリンク先に等しい
		// コストは前ルートのコストにリンクのコストを足したもの
		return new Route(this.fromNodeID, link.toNodeID,
								this.weight + link.weight, linkList);
	}

	public Route clone() {
		// リンクのリストをクローンする
		@SuppressWarnings("unchecked")
		ArrayList<UndirectoryLink> linkList 
			= (ArrayList<UndirectoryLink>) this.linkList.clone();
		return new Route(this.fromNodeID, this.toNodeID, this.weight, linkList);
	}
	
}
