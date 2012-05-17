import java.util.ArrayList;

public class Route extends UndirectoryLink implements Cloneable {
	public ArrayList<UndirectoryLink> mLinkList = null;
	public boolean mIsFlowed = false;
	public boolean mIsNeeded = true;
	/**調べ終わったIDを保存する.最大最短路で用いる*/
	private ArrayList<Integer> mCheckedNodeIDs = null;
	
	/*-------------------コンストラクタ-------------------*/
	public Route(int fromNodeID, int toNodeID, int weight) {
		super(fromNodeID, toNodeID, weight); //ここで代入?
		mLinkList = new ArrayList<UndirectoryLink>();
		mCheckedNodeIDs = new ArrayList<Integer>();
		mCheckedNodeIDs.add(fromNodeID);
	}

	public Route(int fromNodeID, int toNodeID, int weight,
						ArrayList<UndirectoryLink> linkList) {
		super(fromNodeID, toNodeID, weight);
		mLinkList = linkList;
		mCheckedNodeIDs = new ArrayList<Integer>();
		mCheckedNodeIDs.add(fromNodeID);
	}
	
	public Route(int fromNodeID, int toNodeID, int weight, boolean isFlowed) {
		super(fromNodeID, toNodeID, weight);
		mIsFlowed = isFlowed;
		mCheckedNodeIDs = new ArrayList<Integer>();
		mCheckedNodeIDs.add(fromNodeID);
	}
	
	public Route(int fromNodeID, int toNodeID, int weight, 
							ArrayList<UndirectoryLink> linkList,
							ArrayList<Integer> checkedNodeIDs) {
		super(fromNodeID, toNodeID, weight);
		mLinkList = linkList;
		mCheckedNodeIDs = checkedNodeIDs;
	}

	/*-------------------メソッド-------------------*/
	/**そのノードに通ったかどうかを確認する*/
	public boolean isPassed(UndirectoryLink link) {
		int toNodeID = link.mToNodeID;
		for(Integer i: mCheckedNodeIDs){
			if(i == toNodeID) return true;
		}
		return false;
	}

	/**今までのルートにリンクを追加して、新しいルートを作る。
	 * 同時にリンクの先とコストを変化させる。さらに、通ったノードを登録する */
	public Route makeNewRoute(UndirectoryLink link) {
		// リンクのリストをクローンする
		@SuppressWarnings("unchecked")
		ArrayList<UndirectoryLink> linkList 
			= (ArrayList<UndirectoryLink>) mLinkList.clone();
		@SuppressWarnings("unchecked")
		ArrayList<Integer> checkedList 
			= (ArrayList<Integer>) mCheckedNodeIDs.clone();
		Route newRoute = new Route(mFromNodeID, mToNodeID, mWeight, linkList, checkedList);
		newRoute.forwardRoute(link);
		// 次のノードはリンク先に等しい
		// コストは前ルートのコストにリンクのコストを足したもの
		return newRoute;
	}
	
	/**値の追加*/
	public Route forwardRoute(UndirectoryLink link) {
		// 次のノードはリンク先に等しい
		mToNodeID = link.mToNodeID;
		// コストは前ルートのコストにリンクのコストを足したもの
		mWeight += link.mWeight;
		mLinkList.add(link);
		mCheckedNodeIDs.add(link.mToNodeID);
		return this;
	}
	
	/**値の削除*/
	public Route backRoute(UndirectoryLink link) {
		// リンク先に戻る
		mToNodeID = link.mFromNodeID;
		// コストは前ルートのコストにリンクのコストを引いたもの
		mWeight -= link.mWeight;
		int lastIndex = mLinkList.size() - 1;
		mLinkList.remove(lastIndex);
		lastIndex = mCheckedNodeIDs.size() - 1;
		mCheckedNodeIDs.remove(lastIndex);
		return this;
	}
	
	/**値の追加*/
	public Route forwardMaxRoute(UndirectoryLink link) {
		// 次のノードはリンク先に等しい
		mToNodeID = link.mToNodeID;
		if(mWeight > link.mWeight)
			mWeight = link.mWeight;
		mLinkList.add(link);
		mCheckedNodeIDs.add(link.mToNodeID);
		return this;
	}
	
	/**値の削除*/
	public Route backMaxRoute(UndirectoryLink link) {
		// リンク先に戻る
		this.mToNodeID = link.mFromNodeID;
		if(this.mWeight == link.mWeight)
			this.mWeight = Integer.MAX_VALUE;
		int lastIndex = this.mLinkList.size() - 1;
		this.mLinkList.remove(lastIndex);
		lastIndex = this.mCheckedNodeIDs.size() - 1;
		this.mCheckedNodeIDs.remove(lastIndex);
		return this;
	}

	/**リンクをそのままクローンする*/
	public Route clone() {
		// リンクのリストをクローンする
		@SuppressWarnings("unchecked")
		ArrayList<UndirectoryLink> linkList 
		 = (ArrayList<UndirectoryLink>) mLinkList.clone();
		@SuppressWarnings("unchecked")
		ArrayList<Integer> checkedNodeIDs
			= (ArrayList<Integer>)mCheckedNodeIDs.clone();
		return new Route(mFromNodeID, mToNodeID, mWeight, linkList, checkedNodeIDs);
	}
	
}
