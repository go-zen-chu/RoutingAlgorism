import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class MainProcess {

	private static ArrayList<Node> mGraph = null;
	// スタートとゴールになる二つのノード
	private static Node mStartNode = null, mGoalNode = null;
	/** (djikstra)次に検索する候補になるルートのスタック */
	private static ArrayList<Route> mRouteStack = null;
	/** (djikstra)最短ルートを保存した捜索予定のスタック */
	private static ArrayList<Route> mCostLeastRoutes = null;
	/** (ShortestMaxFlowPath)最大路を記録したリスト（ただし、最小路かはわからない）*/
	private static ArrayList<Route> mMaxFlowRoutes = null;
	
	
	public static void main(String[] args) {
		GUI gui = new GUI();
		// グラフの入力と作成
		mGraph = gui.getGraphData();
		
		// ランダムなノードを作成
		long seed = System.currentTimeMillis();
		Random r = new Random(seed);
		int startNodeID = 0, goalNodeID = 0;
		while (startNodeID == goalNodeID) {
			startNodeID = r.nextInt(mGraph.size());
			goalNodeID = r.nextInt(mGraph.size());
		}
		// スタートとゴールノード
		mStartNode = mGraph.get(startNodeID);
		mGoalNode = mGraph.get(goalNodeID);
		// モードを尋ねるダイアログ
		gui.selectMethod();
	}

	/*-----------------------------------------------------*/	
	/**ダイクストラ法*/
	public static Route djikstra() {
		// 初期化がうまくいっていない場合の処理
		if(mGraph == null || mStartNode == null || mGoalNode == null)
			return null;
		mRouteStack = new ArrayList<Route>();
		Route startRoute = new Route(mStartNode.nodeID, mStartNode.nodeID, 0);
		// あるノードに対して、コストが最小のルートのみを集めたリスト
		mCostLeastRoutes = new ArrayList<Route>();
		return searchRoute(startRoute, mGoalNode);
	}
	
	public static Route searchRoute(Route route, Node goalNode) {
		// 検索するノード
		Node node = mGraph.get(route.toNodeID);
		// ノードから派生する新しいルートを保存
		ArrayList<Route> newRoutes = new ArrayList<Route>();
		for(UndirectoryLink l: node.linkList){
			// コストが0でない（つながっている）リンクを元に新しいルートを作成する
			// また、ルートのスタートノードへ戻らないようにする
			// TODO ただし、ルートの途中にあるループについては判断出来ない
			if(!(l.weight == 0) && route.fromNodeID != l.toNodeID){
				newRoutes.add(route.makeNewRoute(l));
			}
		}
		// ルートスタックなどが空でないとき（空の時はプログラムの最初か最後）
		if(!newRoutes.isEmpty()){
			for(int i = 0; i<newRoutes.size(); i++){
				if(!mRouteStack.isEmpty()){
					for(int j = 0; j<mRouteStack.size(); j++){
						if(mRouteStack.get(j).toNodeID == newRoutes.get(i).toNodeID){
							if(mRouteStack.get(j).weight < newRoutes.get(i).weight){
								// 新しいルートのうち、コストが既存のものよりも大きいものは抜き取っておく
								newRoutes.set(i, null);
								break;
							}
						}
					}
				}
			}
		}
		// 新しいルートを追加する
		// TODO この方法の問題点は、既存のルートで効率の悪いものを消せないということ
		for(Route r: newRoutes){
			if(r != null){
				mRouteStack.add(r);
			}
		}
		// コスト順に並べる
		Collections.sort( mRouteStack, new RouteWeightComparator());
		// ルートをポップする
		Route nextRoute = mRouteStack.remove(0);
		// ポップされたルートはそのノードへの最小ルートを示している
		mCostLeastRoutes.add(nextRoute);
		if(nextRoute.toNodeID == goalNode.nodeID) return nextRoute;
		return searchRoute(nextRoute, goalNode);
	}
	
	/*-----------------------------------------------------*/

	/**最短最大路を求める。ただし、双方向ノードのグラフを仮定している*/
	public static Route shortestMaxFlowPath() {
		if(mGraph == null || mStartNode == null || mGoalNode == null)
			return null;
		mMaxFlowRoutes = new ArrayList<Route>();
		ArrayList<UndirectoryLink> flowOrderedLink = getFlowOrderedLinks();
		int trimSize = 1;
		while (true) {
		// リンクのコストが次のものと同等のとき、次のリンクも取り入れる
			if(flowOrderedLink.get(trimSize).weight 
						== flowOrderedLink.get(trimSize+1).weight){
				trimSize++;
			}else{
				int startID = mStartNode.nodeID;
				UndirectoryLink startLink = new UndirectoryLink(startID, startID, 0);
				// caution :: cast
				// 最小ルートを求める
				makeMaxFlowGraph((ArrayList<UndirectoryLink>)flowOrderedLink.subList(0, trimSize),
						startLink, new Route(startID, startID, 0));
				// 上で上手くグラフが出来なかったら新たにリンクを追加する
				if(mMaxFlowRoutes.isEmpty()) {
					trimSize++;
				}else{
					break;
				}
			}
		}
		// ルート内のリンク数を比較して、最小のものから並べる
		Collections.sort(mMaxFlowRoutes, new LinkNumberComparator());
		return mMaxFlowRoutes.get(0);
	}

	/**フロー容量の大きい順に並べたリストを受け取る*/
	public static ArrayList<UndirectoryLink> getFlowOrderedLinks() {
		ArrayList<UndirectoryLink> flowOrderedLink = new ArrayList<UndirectoryLink>();
		// ノードの数
		int nodeNum = mGraph.size();
		for (int i = 0; i < nodeNum; i++) {
			Node n = mGraph.get(i);
			//  重複がないようにリンクを格納していく
			for(int j = 0; j < nodeNum - i; j++){
				flowOrderedLink.add(n.linkList.get(j));
			}
		}
		Collections.sort(flowOrderedLink, new LinkWeightComparator());
		return flowOrderedLink;
	}
	
	public static void makeMaxFlowGraph( ArrayList<UndirectoryLink> undirectoryLinks,
															UndirectoryLink link, Route route) {
		int startID = link.toNodeID;
		int goalID = mGoalNode.nodeID;
		if(goalID == startID){	// ゴールにたどり着けたので、ルートを保存
			mMaxFlowRoutes.add(route.clone());
		}else{	// ゴールではないため、捜索を続ける
			for(UndirectoryLink l: undirectoryLinks){
				if(link.fromNodeID == l.toNodeID){ // 元の場所に戻っているので除外
				}else	if(startID == l.toNodeID){ // 先に進める場合
					// ルートに今のリンクを追加する
					route.updateRoute(l);
					makeMaxFlowGraph(undirectoryLinks, l, route);
					//追加したルートを消す
					route.linkList.remove(route.linkList.size());
				}
			}
		}
	}
	
	/*-----------------------------------------------------*/
	/**ルートのコストを比較するコンパレーター*/
	private static class RouteWeightComparator implements Comparator<Route>{
		// 正の場合、o1がo2よりも大きいと判断される。つまり、昇順（小さいー＞大きい）
		@Override
		public int compare(Route o1, Route o2) {
			return o1.weight - o2.weight;
		}
	}
	
	/**リンクのコストを比較するコンパレーター*/
	private static class LinkWeightComparator implements Comparator<UndirectoryLink>{
		// 降順（大きいー＞小さい）
		@Override
		public int compare(UndirectoryLink o1, UndirectoryLink o2) {
			return o2.weight - o1.weight;
		}
	}
	
	/**ルートのもつリンク数を比較するコンパレーター*/
	private static class LinkNumberComparator implements Comparator<Route>{
		// 昇順（小さいー＞大きい）
		@Override
		public int compare(Route o1, Route o2) {
			return o1.linkList.size() - o2.linkList.size();
		}
	}
	
	/**グラフから任意のふたつのノードを取得する。ただし、値渡しのせいかうまくいかない*/
	/*
	public static void getRandomNodes(ArrayList<Node> graph, Node node1, Node node2) {
			// 任意のノードを選ぶため、IDをランダムに二つ選ぶ
			long seed = System.currentTimeMillis();
			Random r = new Random(seed);
			int node1ID = 0 ,node2ID = 0;
			// ノードのIDがお互いに違うことを確認する
			while(node1ID == node2ID){
				node1ID = r.nextInt(graph.size() - 1);
				node2ID = r.nextInt(graph.size() - 1);
			}  
			// ノードのIDはノード数-1までに収まる
			try {
				node1 = graph.get(node1ID).clone();
				node2 = graph.get(node2ID).clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
	}*/
	
	/*
	public static void sortAndAddRoutes(ArrayList<Route> routeList,
														ArrayList<Route> newRoutes) {
		if(routeList.isEmpty()){	// ルートリスト内が空のとき
			routeList.addAll(newRoutes);
		}else
		// routeListはソート済みだから、newRoutesを適切な場所に挿入する
		while (true) {
			Route[] oldRoutes = (Route[]) routeList.toArray();
			// 新しいルートの最初をポップする
			Route newRoute = newRoutes.remove(0);
			// 新しいルートがリストからなくなったら、break
			if(newRoute == null) break;
			// 新しいルートのコスト
			int newWeight = newRoutes.remove(0).weight;
			for (int i = 0; i < oldRoutes.length - 1; i++) {
				// 追加する場所を見つけた
				if(oldRoutes[i].weight < newWeight && oldRoutes[i+1].weight >= newWeight ){
					routeList.add(i+1, newRoute);
				}else if (i == oldRoutes.length-1){
					// ここに到達するのは、新しいルートがもっともコストが高いときのみ
					routeList.add(newRoute);
				}
			}
		}
		// コストに基づいて、並び替える
		Collections.sort(routeList, new RouteWeightComparator());
		// 新しいルートリストには、なにも含まれていない
		newRoutes = null;
	}
	*/
}







