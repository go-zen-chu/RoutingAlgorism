import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Main {

	// コストグラフへの絶対パス
	private static final String COST_GRAPH 
		= "/Users/masudaakira/Desktop/report/topology3_cost.csv";
	// フローグラフへの絶対パス
	private static final String FLOW_GRAPH 
		= "/Users/masudaakira/Desktop/report/topology4_flow.csv";
	// 結果を保存する場所への絶対パス
	private static final String SAVE_PATH 
		= "/Users/masudaakira/Desktop/result.csv";
	/**改行コード*/
	public static final String NEWLINE = System.getProperty("line.separator");
	
	/**テストした回数*/
	private static final int TEST_TIME = 10000;
	/**通信に用いる回線数*/
	private static final int n = 100;
	
	private static ArrayList<Node> mCostGraph = null;
	private static ArrayList<Node> mFlowGraph = null;
	private static int mNodeNum = -1;
	// スタートとゴールになる二つのノード
	private static Node mStartNode = null, mGoalNode = null;
	/** (djikstra)次に検索する候補になるルートのスタック */
	private static ArrayList<Route> mRouteQueue = null;
	/** (djikstra)最短ルートを保存した捜索予定のスタック */
	private static ArrayList<Route> mCostLeastRoutes = null;
	/** (ShortestMaxFlowPath)最大路を記録したリスト（ただし、最小路かはわからない） */
	private static ArrayList<Route> mMaxFlowRoutes = null;
	
	public static void main(String[] args) {
		// グラフの入力と作成
		mCostGraph = getGraphData(COST_GRAPH);
		mFlowGraph = getGraphData(FLOW_GRAPH);
		
		// 入力したグラフの表示
//		GUI.showMatrixDialog(mCostGraph, "コストグラフ");
		GUI.showMatrixDialog(mFlowGraph, "フローグラフ");
		mNodeNum = mCostGraph.size();
		
		
		// ランダムなノードを作成
		long seed = System.currentTimeMillis();
		Random r = new Random(seed);
		int startNodeID = 0, goalNodeID = 0;
		while (startNodeID == goalNodeID) {
			startNodeID = r.nextInt(mNodeNum);
			goalNodeID = r.nextInt(mNodeNum);
		}
		
		// スタートとゴールノード
		mStartNode = mCostGraph.get(startNodeID);
		mGoalNode = mCostGraph.get(goalNodeID);
		// 実行動作確認
//		Route djikstraRoute = djikstra();
//		Route showtestMaxRoute = shortestMaxFlowPath();
//		GUI.showCostResultDialog(djikstraRoute);
//		GUI.showFlowResultDialog(showtestMaxRoute);
		
		String testResultString = "通信する回線数," + n + ","
										+ "テストが行われた回数," + TEST_TIME + NEWLINE;
		testResultString += testMinHopFixedPathDinamicNodes(r);
		testResultString += testMaxFlowFixedPathDinamicNodes(r);
		testResultString += testMinHopDinamicPathDinamicNodes(r);
		testResultString += testMaxFlowDinamicPathDinamicNodes(r);
		// スタートノードとゴールノードが固定のテスト
//		testResultString += testMinHopFixedPathFixedNodes();
//		testResultString += testMaxFlowFixedPathFixedNodes();
//		testResultString += testMinHopDinamicPathFixedNodes();
//		testResultString += testMaxFlowDinamicPathFixedNodes();
		
		exportAsCsvfile(SAVE_PATH, testResultString);
		System.exit(0);
	}

	/*-----------------------------------------------------*/
	/** ダイクストラ法 */
	public static Route djikstra() {
		mRouteQueue = new ArrayList<Route>();
		Route startRoute = new Route(mStartNode.mNodeID, mStartNode.mNodeID, 0);
		// あるノードに対して、コストが最小のルートのみを集めたリスト
		mCostLeastRoutes = new ArrayList<Route>();
		return searchRoute(startRoute, mGoalNode);
	}
	/**深さ優先探索*/
	public static Route searchRoute(Route route, Node goalNode) {
		// 検索するノード
		Node nextNode = mCostGraph.get(route.mToNodeID);
		// ノードから派生する新しいルートを保存
		ArrayList<Route> newRoutes = new ArrayList<Route>();
		for (UndirectoryLink l : nextNode.mLinkList) {
			// コストが0でない（つながっている）リンクを元に新しいルートを作成する
			// また、通ったノードへ戻らないようにする
			if (!(l.mWeight == 0) && !route.isPassed(l)) {
				newRoutes.add(route.makeNewRoute(l));
			}
		}
		// ルートを伸ばせるかどうか
		if (!newRoutes.isEmpty()) {
			for (Route newRoute: newRoutes) {
				// ルートスタックなどが空でないとき（空の時はプログラムの最初か最後）
				if (!mRouteQueue.isEmpty()) {
					for (Route stackedRoute: mRouteQueue) {
						if (stackedRoute.mToNodeID == newRoute.mToNodeID) {
							if (stackedRoute.mWeight < newRoute.mWeight) {
								// 新しいルートのうち、コストが既存のものよりも大きいものは抜き取っておく
								newRoute.mIsNeeded = false;
								break;
							}
						}
					}
				}
				// ルートスタックが空のとき、全部の新ルートを入れる
			}
		}
		// 新しいルートを追加する
		// TODO この方法の問題点は、既存のルートで効率の悪いものを消せないということ
		// ただし、最後の方に移すことは出来る
		for (Route newRoute : newRoutes) {
			if (newRoute.mIsNeeded) {	// すでに通っているものは、既存のルートよりもコストが掛かる
				mRouteQueue.add(newRoute);
			}
		}
		// 調査の結果、そこにいたるルートがないことが分かった
		if(mRouteQueue.isEmpty() && newRoutes.isEmpty()){
			// 繋げられない＝呼損のため、呼損したと伝える
			return new Route(-1, -1, -1, true);
		}
		// コスト順に並べる
		Collections.sort(mRouteQueue, new RouteWeightComparator());
		// ルートをポップする
		Route nextRoute = mRouteQueue.remove(0);
		// ポップされたルートはそのノードへの最小ルートを示している
		mCostLeastRoutes.add(nextRoute);
		if (nextRoute.mToNodeID == goalNode.mNodeID){
			// ゴールに到達した
			return nextRoute;
		}
		return searchRoute(nextRoute, goalNode);
	}

	/*-----------------------------------------------------*/
	/** 最短最大路を求める*/
	public static Route shortestMaxFlowPath() {
		// 最大路が入っているリスト
		mMaxFlowRoutes = new ArrayList<Route>();
		ArrayList<UndirectoryLink> flowOrderedLinks = getFlowOrderedLinks();
		int linkNum = flowOrderedLinks.size();
		// ArrayList を切り取る大きさ(sublistで用いる)
		int trimSize = 1;
		int startID = mStartNode.mNodeID;
		UndirectoryLink startLink = new UndirectoryLink(startID, startID, 0);
		while (true) {
			if(trimSize > linkNum){	// どのリンクを使ってもグラフを作成出来なかった
				return new Route(-1, -1, -1, true);
			}
			// リンクのコストが次のものと同等のとき、次のリンクも取り入れる
			if (flowOrderedLinks.get(trimSize - 1).mWeight
					== flowOrderedLinks.get(trimSize).mWeight) {
				trimSize++;	// 切り取るサイズを大きくする
			} else {
				// 最小ルートを求める
				makeMaxFlowGraph(
						new ArrayList<UndirectoryLink>(flowOrderedLinks.subList(0,trimSize)),
								startLink, new Route(startID, startID, Integer.MAX_VALUE));
				// 上で上手くグラフが出来なかったら新たにリンクを追加する
				if (mMaxFlowRoutes.isEmpty()) {
					trimSize++;
				} else {
					break; // グラフを作成出来た
				}
			}
			if (trimSize == linkNum) {	// 全てのリンクが含まれるとき
				// 最小ルートを求める
				makeMaxFlowGraph(
						new ArrayList<UndirectoryLink>(flowOrderedLinks.subList(0,trimSize)),
								startLink, new Route(startID, startID, Integer.MAX_VALUE));
				// 上で上手くグラフが出来なかったら新たにリンクを追加する
				if (mMaxFlowRoutes.isEmpty()) {
					trimSize++;
				} else {
					break;
				}
			}
		}
		// ルート内のリンク数を比較して、最小のものから並べる
		Collections.sort(mMaxFlowRoutes, new LinkNumberComparator());
		return mMaxFlowRoutes.get(0);
	}
	/** フロー容量の大きい順に並べたリストを受け取る */
	public static ArrayList<UndirectoryLink> getFlowOrderedLinks() {
		ArrayList<UndirectoryLink> flowOrderedLink = new ArrayList<UndirectoryLink>();
		// 単方向リストだが、全てのリンクを格納する
		for (Node n : mFlowGraph)
			for (UndirectoryLink l : n.mLinkList)
				if (l.mWeight != 0)
					flowOrderedLink.add(l);
		Collections.sort(flowOrderedLink, new LinkWeightComparator());
		return flowOrderedLink;
	}
	public static void makeMaxFlowGraph(
			ArrayList<UndirectoryLink> undirectoryLinks, UndirectoryLink link,
			Route route) {
		int startID = link.mToNodeID;
		int goalID = mGoalNode.mNodeID;
		if (goalID == startID) { // ゴールにたどり着けたので、ルートを保存
			mMaxFlowRoutes.add(route.clone());
		} else { // ゴールではないため、捜索を続ける
			for (UndirectoryLink l : undirectoryLinks) {
				if (link.mFromNodeID == l.mToNodeID) { // 一つ前の場所に戻っているので除外
				} else if (!route.isPassed(l) && startID == l.mFromNodeID) { // 先に進める場合
					// ルートに今のリンクを追加する
					route.forwardMaxRoute(l);
					makeMaxFlowGraph(undirectoryLinks, l, route);
					// 追加したルートを消す
					route.backMaxRoute(l);
				}
			}
		}
	}
	/*-----------------------------------------------------*/
	/** ルートのコストを比較するコンパレーター */
	private static class RouteWeightComparator implements Comparator<Route> {
		// 正の場合、o1がo2よりも大きいと判断される。つまり、昇順（小さいー＞大きい）
		@Override
		public int compare(Route o1, Route o2) {
			return o1.mWeight - o2.mWeight;
		}
	}
	/** リンクのコストを比較するコンパレーター */
	private static class LinkWeightComparator implements
			Comparator<UndirectoryLink> {
		// 降順（大きいー＞小さい）
		@Override
		public int compare(UndirectoryLink o1, UndirectoryLink o2) {
			return o2.mWeight - o1.mWeight;
		}
	}
	/** ルートのもつリンク数を比較するコンパレーター */
	private static class LinkNumberComparator implements Comparator<Route> {
		// 昇順（小さいー＞大きい）
		@Override
		public int compare(Route o1, Route o2) {
			return o1.mLinkList.size() - o2.mLinkList.size();
		}
	}
	
	/*--------------------ノード変動でテスト------------------------*/
	/**最小ホップ経路を用いた固定経路*/
	private static String testMinHopFixedPathDinamicNodes(Random r){
		String testResultString = "最小ホップ経路を用いた固定経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			int linkNum = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			for (int count = 0; count < TEST_TIME; count++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){ // 呼損していなかったら通信を終了、回線を回復
					// コストを変化させない
					increaseFlow(route);
				}
				Route nextRoute  = djikstra();
				decreaseFlow(nextRoute);
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}
				communicatingQueue.add(nextRoute);
				
				if(!nextRoute.mIsFlowed){
					linkNum += nextRoute.mLinkList.size();
				}
					
				// スタートとゴールはランダム
				int startNodeID = 0, goalNodeID = 0;
				while (startNodeID == goalNodeID) {
					startNodeID = r.nextInt(mNodeNum);
					goalNodeID = r.nextInt(mNodeNum);
				}
				mStartNode = mCostGraph.get(startNodeID);
				mGoalNode = mCostGraph.get(goalNodeID);
			}
			testResultString += j + "," + (((double)flowedTime)/TEST_TIME) + "," 
									+ ((double)linkNum)/(TEST_TIME-flowedTime) + NEWLINE;
			// コストグラフには変化を与えないので、初期値のまま
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最大路を用いた固定経路*/
	private static String testMaxFlowFixedPathDinamicNodes(Random r){
		String testResultString = "最大路を用いた固定経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			int linkNum = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i = 0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			for (int count = 0; count < TEST_TIME; count++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlow(route);
				}
				
				ArrayList<Node> tmpFlowGraph = mFlowGraph;
				mFlowGraph = getGraphData(FLOW_GRAPH);
				// 固定の場合、元々フローが最大だったものを選ぶ
				Route nextRoute = shortestMaxFlowPath();
				mFlowGraph = tmpFlowGraph;
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}else{
					decreaseFlow(nextRoute);
					if(nextRoute.mIsFlowed) flowedTime++;
				}
				communicatingQueue.add(nextRoute);
				
				if(!nextRoute.mIsFlowed)
					linkNum += nextRoute.mLinkList.size();
				
				// スタートとゴールノード
				int startNodeID = 0, goalNodeID = 0;
				while (startNodeID == goalNodeID) {
					startNodeID = r.nextInt(mNodeNum);
					goalNodeID = r.nextInt(mNodeNum);
				}
				mStartNode = mCostGraph.get(startNodeID);
				mGoalNode = mCostGraph.get(goalNodeID);
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + ","
								+ ((double)linkNum)/(TEST_TIME-flowedTime) + NEWLINE;
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最小ホップを用いた要求時経路*/
	private static String testMinHopDinamicPathDinamicNodes(Random r){
		String testResultString = "最小ホップを用いた要求時経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route nextRoute;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			int linkNum = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			// 最小ホップを用いた要求時経路
			for (int i = 0; i < TEST_TIME; i++) {
				int startNodeID = 0, goalNodeID = 0;
				while (startNodeID == goalNodeID) {
					startNodeID = r.nextInt(mNodeNum);
					goalNodeID = r.nextInt(mNodeNum);
				}
				// スタートとゴールノード
				mStartNode = mCostGraph.get(startNodeID);
				mGoalNode = mCostGraph.get(goalNodeID);
				
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlowCostChange(route);
				}
				nextRoute = djikstra();
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}else{
					decreaseFlowCostChange(nextRoute);
					if(nextRoute.mIsFlowed) flowedTime++;
				}
				communicatingQueue.add(nextRoute);
				
				if(!nextRoute.mIsFlowed)
					linkNum += nextRoute.mLinkList.size();
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + ","
									+ ((double)linkNum)/(TEST_TIME-flowedTime)+ NEWLINE;
			mCostGraph = getGraphData(COST_GRAPH);
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最大路を用いた要求時経路*/
	private static String testMaxFlowDinamicPathDinamicNodes(Random r){
		String testResultString = "最大路を用いた要求時経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route nextRoute;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			int linkNum = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			// 最小ホップを用いた要求時経路
			for (int i = 0; i < TEST_TIME; i++) {
				// スタートとゴールノードをランダムに
				int startNodeID = 0, goalNodeID = 0;
				while (startNodeID == goalNodeID) {
					startNodeID = r.nextInt(mNodeNum);
					goalNodeID = r.nextInt(mNodeNum);
				}
				mStartNode = mCostGraph.get(startNodeID);
				mGoalNode = mCostGraph.get(goalNodeID);
				
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlow(route);
				}
				nextRoute = shortestMaxFlowPath();
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}else{
					decreaseFlow(nextRoute);
					if(nextRoute.mIsFlowed) flowedTime++;
				}
				communicatingQueue.add(nextRoute);
				
				if(!nextRoute.mIsFlowed)
					linkNum += nextRoute.mLinkList.size();
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + ","
					+ ((double)linkNum)/(TEST_TIME-flowedTime) + NEWLINE;

			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	
	/*--------------------固定ノードでテスト------------------------*/
	/**最小ホップ経路を用いた固定経路*/
	private static String testMinHopFixedPathFixedNodes(){
		String testResultString = "最小ホップ経路を用いた固定経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route communicatingRoute = djikstra();
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			for (int count = 0; count < TEST_TIME; count++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){ // 呼損していなかったら通信を終了、回線を回復
					increaseFlowCostChange(route);
				}
				// 最初からルートを変えないので、ただそのルートのフローを引いていく
				Route nextRoute = communicatingRoute.clone();
				// また新しい経路を調べる必要がないので、nextRoute.mIsFlowedを調べる必要はない
				decreaseFlowCostChange(nextRoute);
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}
				communicatingQueue.add(nextRoute);
			}
			testResultString += j + "," + (((double)flowedTime)/TEST_TIME) + NEWLINE;

			mCostGraph = getGraphData(COST_GRAPH);
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最大路を用いた固定経路*/
	private static String testMaxFlowFixedPathFixedNodes(){
		String testResultString = "最大路を用いた固定経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route communicatingRoute = shortestMaxFlowPath();
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i = 0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			for (int count = 0; count < TEST_TIME; count++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlow(route);
				}
				// 最初からルートを変えないので、ただそのルートのフローを引いていく
				Route nextRoute = communicatingRoute.clone();
				decreaseFlow(nextRoute);
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}
				communicatingQueue.add(nextRoute);
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + NEWLINE;

			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最小ホップを用いた要求時経路*/
	private static String testMinHopDinamicPathFixedNodes(){
		String testResultString = "最小ホップを用いた要求時経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route nextRoute;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			// 最小ホップを用いた要求時経路
			for (int i = 0; i < TEST_TIME; i++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlowCostChange(route);
				}
				// 毎回ルートを確認し、
				nextRoute = djikstra();
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}else{
					decreaseFlowCostChange(nextRoute);
					if(nextRoute.mIsFlowed) flowedTime++;
				}
				communicatingQueue.add(nextRoute);
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + NEWLINE;
			mCostGraph = getGraphData(COST_GRAPH);
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	/**最大路を用いた要求時経路*/
	private static String testMaxFlowDinamicPathFixedNodes(){
		String testResultString = "最大路を用いた要求時経路" + NEWLINE
										+ "通信した量,呼損率" + NEWLINE;
		// 通信を始めようとするルート
		Route nextRoute;
		for(int j = 1; j < n; j++){
			int flowedTime = 0;
			// 通信の行うルート等を一時的に格納しておく
			ArrayList<Route> communicatingQueue = new ArrayList<Route>(j);
			// 最初は通信中のルートを全て呼損したことにする（通信終了処理を行わないため）
			for(int i=0; i < j; i++){
				communicatingQueue.add(new Route(-1, -1, -1, true));
			}
			// 最小ホップを用いた要求時経路
			for (int i = 0; i < TEST_TIME; i++) {
				Route route = communicatingQueue.remove(0);
				if(!route.mIsFlowed){
					// 通信を終了、回線を回復
					increaseFlow(route);
				}
				nextRoute = shortestMaxFlowPath();
				if(nextRoute.mIsFlowed){
					flowedTime++;
				}else{
					decreaseFlow(nextRoute);
					if(nextRoute.mIsFlowed) flowedTime++;
				}
				communicatingQueue.add(nextRoute);
			}
			testResultString += j + "," + ((double)flowedTime)/TEST_TIME + NEWLINE;
			mFlowGraph = getGraphData(FLOW_GRAPH);
		}
		return testResultString;
	}
	
	/*-----------------------------------------------------*/
	/**ルートの容量を1引いて、呼損したかどうかを判定する*/
	public static void decreaseFlowCostChange(Route route) {
		// 呼損が起こるかどうかを判断する
		for(UndirectoryLink l: route.mLinkList){
			// 容量のグラフのリンクの重みを見る
			for( UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
				if(link.mToNodeID == l.mToNodeID){
					if(link.mWeight == 0){
						// 容量が0の場所があったため、これは呼損する
						route.mIsFlowed = true;
						return;
					}
				}
			}
		}
		// 呼損していなかった時だけルートの容量を減らす
		if(!route.mIsFlowed){
			for(UndirectoryLink l: route.mLinkList){
				for( UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
					if(link.mToNodeID == l.mToNodeID){
						// 各リンクの容量を減らす
						link.mWeight--;
						// リンクの容量が0になったとき、そこは通れない
						if(link.mWeight == 0){
							mCostGraph.get(l.mFromNodeID).mLinkList.get(l.mToNodeID).mWeight = 0;
						}
					}
				}
			}
		}
	}
	/**ルートの容量を1足す*/
	public static void increaseFlowCostChange( Route route) {
		for(UndirectoryLink l: route.mLinkList){
			for(UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
				if(link.mToNodeID == l.mToNodeID){
					// 通れなかったリンクが復活する
					if(link.mWeight == 0){
						mCostGraph.get(l.mFromNodeID).mLinkList.get(l.mToNodeID).mWeight = 1;
					}
					link.mWeight++;
				}
			}
		}
	}
	/**ルートの容量を1引いて、呼損したかどうかを判定する*/
	public static void decreaseFlow(Route route) {
		// 呼損が起こるかどうかを判断する
		for(UndirectoryLink l: route.mLinkList){
			// 容量のグラフのリンクの重みを見る
			for( UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
				if(link.mToNodeID == l.mToNodeID){
					if(link.mWeight == 0){
						// 容量が0の場所があったため、これは呼損する
						route.mIsFlowed = true;
						return;
					}
				}
			}
		}
		// 呼損していなかった時だけルートの容量を減らす
		if(!route.mIsFlowed){
			for(UndirectoryLink l: route.mLinkList){
				for( UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
					if(link.mToNodeID == l.mToNodeID){
						// 各リンクの容量を減らす
						link.mWeight--;
					}
				}
			}
		}
	}
	/**ルートの容量を1足す*/
	public static void increaseFlow( Route route) {
		for(UndirectoryLink l: route.mLinkList){
			for(UndirectoryLink link: mFlowGraph.get(l.mFromNodeID).mLinkList){
				if(link.mToNodeID == l.mToNodeID){
					link.mWeight++;
				}
			}
		}
	}
	
	/*--------------------ファイルの入出--------------------------*/
	
	/**グラフのデータをcsvから取得する*/
	public static ArrayList<Node> getGraphData(String filePath) {
		ArrayList<Node> graph = new ArrayList<Node>();
		// ファイルの選択ダイアログの起動
		File csvFile = new File( filePath);
		ArrayList<int[]> graphData = getGraphDataFromCsv(csvFile);
		makeGraphFromGraphData(graph, graphData);
		return graph;
	}
	/**入力されたグラフが正しい場合、ArrayListが返され、正しくない場合はnullを返す*/
	public static ArrayList<int[]> getGraphDataFromCsv(File csvFile) {
		ArrayList<int[]> csvData = new ArrayList<int[]>();
		BufferedReader bufferedReader = null;
		String lineStr = null;
		int columnCount = 0;
		int oldRowNumber = 0;
		int newRowNumber = 0;
		int maxRowNumber = 0;
		try {
			bufferedReader = new BufferedReader(new FileReader(csvFile));
			while(null != (lineStr = bufferedReader.readLine())){
				 // 改行のみの行は行数としてカウントしない
	          if(!lineStr.equals("¥n")){
	         	 // 改行だけではない行はカウントする
	         	 columnCount++;
	         	 // 渡された行列がグラフ作成するにあたい、正しいかどうか確認する
	         	 String[] strArray = lineStr.split(","); // csvファイルの行を ,で分割する	  
	         	 newRowNumber = strArray.length;
	         	// 最初の列数が最大列数と同じになる筈
         		 if(csvData.isEmpty()) {
         			 maxRowNumber = newRowNumber;
         			 oldRowNumber = newRowNumber;
         		 }
	         	 // 行列の形が正しいかどうか
	         	 if(newRowNumber == oldRowNumber - 1){
	         		 // 入力方法簡素化ver （与えられたデータが実対称であると仮定して格納）
	         		 int[] intArray = new int[maxRowNumber];
	         		 for(int i = 0; i < maxRowNumber - newRowNumber; i++){
	         			 // いびつな形をした行列を正方行列に直す
	         			 intArray[i] = csvData.get(i)[maxRowNumber-newRowNumber];
	         		 }
	         		 for(int i = 0; i < strArray.length; i++){
	         			  intArray[i + maxRowNumber - newRowNumber] = Integer.parseInt(strArray[i]);
	         		 }
	         		 csvData.add(intArray);
	         	 }else if(newRowNumber == oldRowNumber){
	         		 // グラフの入力行列が正方行列だった場合
	         		 int[] intArray = new int[maxRowNumber];
	         		 for(int i = 0; i < maxRowNumber; i++){
	         			  intArray[i] = Integer.parseInt(strArray[i]);
	         		 }
	         		 csvData.add(intArray);
	         	 }else {
	         		 System.out.println("入力したファイルの" + columnCount 
		         				+ "行目：：グラフの入力に間違いがあります。");
	 	         	return null;
					}
	          }
	          oldRowNumber = newRowNumber;
	      }
			//System.out.println("行数:" + columnCount + " 列数:" + maxRowNumber);
			// 正方行列でないとおかしいので、行数=列数かどうかを調べる
			if(columnCount != maxRowNumber){
				System.out.println("入力したファイル行数か列数に問題があります");
	         return null;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(bufferedReader != null) bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return csvData;
	}
	/**ArrayListからグラフ（ノードやリンクなど）を作成する*/
	public static void makeGraphFromGraphData(ArrayList<Node> graph,
												ArrayList<int[]> graphData) {
		int columnNum = graphData.size();
		for(int i = 0; i < columnNum; i++){
			// リンクを格納するlist
			ArrayList<UndirectoryLink> linkList = new ArrayList<UndirectoryLink>();
			int[] columnArray = graphData.get(i);
			for(int j = 0; j < columnNum; j++){
				UndirectoryLink l = new UndirectoryLink(i, j, columnArray[j]);
				linkList.add(l);
			}
			Node n = new Node( i, linkList);
			graph.add(n);
		}
	}
	
	/**String型をcsvファイルとして指定の場所に保存する*/
	private static void exportAsCsvfile(String fileName, String data) {
		File file = new File(fileName);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			pw.print(data);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			pw.close();
		}
	}
}
