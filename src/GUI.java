import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**グラフの入力を司るクラス。コンストラクタでcsvファイル読み込む*/
public class GUI {
	
//	
//	/**グラフのデータをcsvから取得する*/
//	public static ArrayList<Node> chooseGraphData() {
//		ArrayList<Node> graph = new ArrayList<Node>();
//		// ファイルの選択ダイアログの起動
//		File csvFile = showFileChooser();
//		ArrayList<int[]> graphData = getGraphDataFromCsv(csvFile);
//		makeGraphFromGraphData(graph, graphData);
//		return graph;
//	}
//	
//	/**ファイル選択ダイアログを表示させる。キャンセルされた場合は、null*/
//	public static File showFileChooser() {
//		File file = null;
//		JFileChooser filechooser = new JFileChooser();
//		FileFilter filter = new FileNameExtensionFilter("csv file", "csv","CSV");
//		filechooser.addChoosableFileFilter(filter);
//		int selected = filechooser.showOpenDialog(this);
//		 if (selected == JFileChooser.APPROVE_OPTION){
//		    file = filechooser.getSelectedFile();
//		 }else if (selected == JFileChooser.CANCEL_OPTION){
//		    System.exit(0);
//		    return null;
//		 }
//		 return file;
//	}
	

	/**行列表示ダイアログ*/
	public static void showMatrixDialog(ArrayList<Node> graph) {
		JFrame selectDialog = new JFrame("実行の結果");
		selectDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectDialog.setSize(400, 400);
		selectDialog.setLayout(new GridLayout(1,1));
		
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
		selectDialog.add(nodeLabel);
		selectDialog.setVisible(true);
	}

	/**メソッド選択ダイアログ*/
	public static void selectMethod() {
		JFrame selectDialog = new JFrame("実行の選択");
		selectDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectDialog.setSize(400, 400);
		// 真ん中に表示
		selectDialog.setLocationRelativeTo(null);
		selectDialog.setLayout(new GridLayout(1, 3));
		// ボタン1
		JButton djikstraButton = new JButton("最短路を求める");
		djikstraButton.setActionCommand("Djikstra");
		djikstraButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String actionCommand = e.getActionCommand();
				if(actionCommand.equals("Djikstra")){
					// ダイクストラ法
					Route resultRoute = Main.djikstra();
					// 結果を表示
					showResultDialog(resultRoute);
				}
			}
		});
		// ボタン2
		JButton shortestMaxButton = new JButton("最短最大路を求める");
		shortestMaxButton.setActionCommand("ShortestMaxFlowPath");
		shortestMaxButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String actionCommand = e.getActionCommand();
				if (actionCommand.equals("ShortestMaxFlowPath")) {
					// 最短最大路を求める
					Route resultRoute = Main.shortestMaxFlowPath();
					showResultDialog(resultRoute);
				}
			}
		});
		selectDialog.add(djikstraButton);
		selectDialog.add(shortestMaxButton);
		selectDialog.add(djikstraButton, BorderLayout.NORTH);
		selectDialog.add(shortestMaxButton, BorderLayout.SOUTH);
		selectDialog.setVisible(true);
	}

	/**結果表示ダイアログ*/
	public static void showResultDialog(Route route) {
		JFrame selectDialog = new JFrame("実行の結果");
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
	
}

