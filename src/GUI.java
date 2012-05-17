import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**グラフの入力を司るクラス。コンストラクタでcsvファイル読み込む*/
public class GUI {

	/**行列表示ダイアログ*/
	public static void showMatrixDialog(ArrayList<Node> graph, String graphName) {
		JFrame showMatrixDialog = new JFrame("入力された隣接行列の表示(" + graphName + ")");
		showMatrixDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		showMatrixDialog.setSize(400, 400);
		showMatrixDialog.setLayout(new GridLayout(1,1));
		
		int nodeNum = graph.size();
		String viewStr = "<html>Nodes :";
		for(int i = 0; i < nodeNum; i++){
			viewStr += "  " + String.valueOf(i) + "|";
		}
		viewStr += "<br>";
		for (int i = 0; i < nodeNum; i++) {
			viewStr += "Node " + String.valueOf(i) + ":";
			for (int j = 0; j < nodeNum; j++) {
				viewStr += "  " + String.valueOf(graph.get(i).mLinkList.get(j).mWeight) + "|";
			}
			viewStr += "<br>";
		}
		JLabel nodeLabel = new JLabel(viewStr);
		showMatrixDialog.add(nodeLabel);
		showMatrixDialog.setVisible(true);
	}


	/**結果表示ダイアログ*/
	public static void showCostResultDialog(Route route) {
		JFrame selectDialog = new JFrame("実行の結果(ダイクストラ法)");
		selectDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectDialog.setSize(400, 400);
		selectDialog.setLocationRelativeTo(null);
		selectDialog.setLayout(new GridLayout(8,1));
		
		JLabel nodeLabel = new JLabel("<html>Start Node ID : " + route.mFromNodeID + "<br>"
											+ "Goal Node ID : " + route.mToNodeID + "<br>");
		String resultStr = String.valueOf("    " + route.mFromNodeID);
		for(UndirectoryLink l: route.mLinkList){
			resultStr += " ->(" + String.valueOf(l.mWeight) + ")-> " + String.valueOf(l.mToNodeID);
		}
		JLabel resultLabel = new JLabel("<html>" + resultStr + "<br>" 
												+ " Cost : " + route.mWeight);
		selectDialog.add(nodeLabel);
		selectDialog.add(resultLabel);
		selectDialog.setVisible(true);
	}
	
	/**結果表示ダイアログ*/
	public static void showFlowResultDialog(Route route) {
		JFrame selectDialog = new JFrame("実行の結果(最短最大路)");
		selectDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectDialog.setSize(400, 400);
		selectDialog.setLocationRelativeTo(null);
		selectDialog.setLayout(new GridLayout(8,1));
		
		JLabel nodeLabel = new JLabel("<html>Start Node ID : " + route.mFromNodeID + "<br>"
											+ "Goal Node ID : " + route.mToNodeID + "<br>");
		String resultStr = String.valueOf("    " + route.mFromNodeID);
		for(UndirectoryLink l: route.mLinkList){
			resultStr += " ->(" + String.valueOf(l.mWeight) + ")-> " + String.valueOf(l.mToNodeID);
		}
		JLabel resultLabel = new JLabel("<html>" + resultStr + "<br>" 
												+ "Max Flow : " + route.mWeight);
		selectDialog.add(nodeLabel);
		selectDialog.add(resultLabel);
		selectDialog.setVisible(true);
	}
}

