import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**グラフの入力を司るクラス。コンストラクタでcsvファイル読み込む*/
public class GUI extends JFrame implements ActionListener{
	
	private static final long serialVersionUID = 1L;

	/**default construstor*/
	public GUI(){
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	/*-----------------------------------------------------*/
	/**グラフのデータをcsvから取得する*/
	public ArrayList<Node> getGraphData(String fileName) {
		ArrayList<Node> graph = new ArrayList<Node>();
		// ファイルの選択ダイアログの起動
		File csvFile = new File("/Users/masudaakira/Documents/" + fileName);
		ArrayList<int[]> graphData = getGraphDataFromCsv(csvFile);
		makeGraphFromGraphData(graph, graphData);
		return graph;
	}
	
	
	/**グラフのデータをcsvから取得する*/
	public ArrayList<Node> chooseGraphData() {
		ArrayList<Node> graph = new ArrayList<Node>();
		// ファイルの選択ダイアログの起動
		File csvFile = showFileChooser();
		ArrayList<int[]> graphData = getGraphDataFromCsv(csvFile);
		makeGraphFromGraphData(graph, graphData);
		return graph;
	}
	
	/**ファイル選択ダイアログを表示させる。キャンセルされた場合は、null*/
	public File showFileChooser() {
		File file = null;
		JFileChooser filechooser = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("csv file", "csv","CSV");
		filechooser.addChoosableFileFilter(filter);
		int selected = filechooser.showOpenDialog(this);
		 if (selected == JFileChooser.APPROVE_OPTION){
		    file = filechooser.getSelectedFile();
		 }else if (selected == JFileChooser.CANCEL_OPTION){
		    System.exit(0);
		    return null;
		 }
		 return file;
	}
	
	/**入力されたグラフが正しい場合、ArrayListが返され、正しくない場合はnullを返す*/
	public ArrayList<int[]> getGraphDataFromCsv(File csvFile) {
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
	         		Exception e = new Exception(
	         				"入力したファイルの" + columnCount 
	         				+ "行目：：グラフの入力に間違いがあります。");
	 	         	showErrorDialog(e);
	 	         	return null;
					}
	          }
	          oldRowNumber = newRowNumber;
	      }
			//System.out.println("行数:" + columnCount + " 列数:" + maxRowNumber);
			// 正方行列でないとおかしいので、行数=列数かどうかを調べる
			if(columnCount != maxRowNumber){
				Exception e = new Exception("入力したファイル行数か列数に問題があります");
	         showErrorDialog(e);
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
	public void makeGraphFromGraphData(ArrayList<Node> graph,
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

	/*-----------------------------------------------------*/
	/**行列表示ダイアログ*/
	public void showMatrixDialog(ArrayList<Node> graph) {
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
	/*-----------------------------------------------------*/	
	/**メソッド選択ダイアログ*/
	public void selectMethod() {
		JFrame selectDialog = new JFrame("実行の選択");
		selectDialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selectDialog.setSize(400, 400);
		// 真ん中に表示
		selectDialog.setLocationRelativeTo(null);
		selectDialog.setLayout(new GridLayout(1, 3));
		// ボタン
		JButton djikstraButton = new JButton("最短路を求める");
		djikstraButton.setActionCommand("Djikstra");
		djikstraButton.addActionListener(this);
		// ボタン2
		JButton shortestMaxButton = new JButton("最短最大路を求める");
		shortestMaxButton.setActionCommand("ShortestMaxFlowPath");
		shortestMaxButton.addActionListener(this);
		selectDialog.add(djikstraButton);
		selectDialog.add(shortestMaxButton);
		selectDialog.add(djikstraButton, BorderLayout.NORTH);
		selectDialog.add(shortestMaxButton, BorderLayout.SOUTH);
		selectDialog.setVisible(true);
	}
	/*-----------------------------------------------------*/
	/**結果表示ダイアログ*/
	public void showResultDialog(Route route) {
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
	/*-----------------------------------------------------*/	
	// ボタンを押したとき
	@Override
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if(actionCommand.equals("Djikstra")){
			// ダイクストラ法
			Route resultRoute = Main.djikstra();
			// 結果を表示
			showResultDialog(resultRoute);
		}else if (actionCommand.equals("ShortestMaxFlowPath")) {
			// 最短最大路を求める
			Route resultRoute = Main.shortestMaxFlowPath();
			showResultDialog(resultRoute);
		}
	}
	
	public void showErrorDialog(Exception e) {
		// dialogを表示させる
		e.printStackTrace();
		System.exit(1);
	}
}

