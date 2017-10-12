package etc.execute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GetShortestRoute {

	static String JDBCDriver = "com.cloudera.impala.jdbc4.Driver";
	Connection con = null;
	HashMap<String, ArrayList<String>> gantryMap = new HashMap<String, ArrayList<String>>();
	BufferedWriter bw = null;
	FileWriter fw = null;
	int shortestPathLength=0;
	
	public GetShortestRoute() {
		try {
			Class.forName(JDBCDriver);
			con = DriverManager.getConnection(CONNECTION_URL);
			fw = new FileWriter("G:\\google雲端硬碟\\外部競賽\\交通部ETC競賽\\ShortestRoute.csv");
			bw = new BufferedWriter(fw);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Define a string as the connection URL
	private static final String CONNECTION_URL = "jdbc:impala://140.128.101.178:21050";

	public static void main(String[] args) {
		GetShortestRoute gsr = new GetShortestRoute();
		gsr.run("01F2089N", "03F3101N");
		gsr.closeConns();
	}

	/** 載入GantryMap */
	public void loadGantryMap() {
		Statement stmt = null;
		ResultSet rs = null;
		String query = "select gantryfrom, gantryto from etc_data.distinct_gantries  order by gantryfrom";

		try {
			String tmpGantry = "";
			ArrayList<String> gantryList = new ArrayList<String>();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			rs.next();
			tmpGantry = rs.getString("gantryfrom");
			gantryList.add(rs.getString("gantryto"));

			while (rs.next()) {
				if (!tmpGantry.equals(rs.getString("gantryfrom"))) {
					gantryMap.put(tmpGantry, gantryList);
					gantryList = new ArrayList<String>();
					tmpGantry = rs.getString("gantryfrom");
				}
				gantryList.add(rs.getString("gantryto"));
			}

			gantryMap.put(tmpGantry, gantryList);
			shortestPathLength = gantryMap.size();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void getTopRoutes(int shortestLength){
		
	} 
	public void getRoute(ArrayList<String> route, String endGantry) {
//		System.out.println("Size:" + route.size());
//		System.out.println("E:" + route.get(route.size() - 1));
		if (gantryMap.containsKey(route.get(route.size() - 1))) {
			for (String tmpGantry : gantryMap.get(route.get(route.size() - 1))) {
				if (!tmpGantry.equals(endGantry)) {
//					System.out.println("New:" + tmpGantry);
					HashSet<String> tmpHighway = new HashSet<String>();  
					for (int i = 0; i < route.size(); i++) {
//						System.out.println(route.get(i).substring(0,3)+route.get(i).substring(route.get(i).length()-1,route.get(i).length()));
						tmpHighway.add(route.get(i).substring(0,3)+route.get(i).substring(route.get(i).length()-1,route.get(i).length()));
					}
					tmpHighway.remove(route.get(route.size()-1).substring(0,3)+route.get(route.size()-1).substring(route.get(route.size()-1).length()-1,route.get(route.size()-1).length()));
//					String replicate = route.get(route.size()-1).substring(0,3)+route.get(route.size()-1).substring(route.get(route.size()-1).length()-1,route.get(route.size()-1).length());
//					System.out.println(replicate);	
					if(tmpHighway.size()>1 && tmpHighway.contains(tmpGantry.substring(0,3)+tmpGantry.substring(tmpGantry.length()-1,tmpGantry.length()))){
//						int count = 0;
//						ArrayList<String> tmpList = new ArrayList<String>();
//						for (int i = 0; i < route.size(); i++) {
//							tmpList.add(route.get(i));
//						}
//						tmpList.add(tmpGantry);
//						StringBuffer sb = new StringBuffer();
//						for (String gantry : tmpList) {
//							if (count != 0) {
//								sb.append("-->");
//							}
//							sb.append(gantry);
//							count++;
//						}
//						System.out.println("重複了:"+sb.toString());						
						continue; //如果有重複上道路就停止
					} 
					/** 複製List */
					ArrayList<String> tmpList = new ArrayList<String>();
					for (int i = 0; i < route.size(); i++) {
						tmpList.add(route.get(i));
					}
					tmpList.add(tmpGantry);
					/** 如果搜尋的超出限制就停止 */
					if (tmpList.size() < gantryMap.size()) {
						if(tmpList.size() < shortestPathLength-1){
							getRoute(tmpList, endGantry);	
						}
					}
				} else {
					int count = 0;
					ArrayList<String> tmpList = new ArrayList<String>();
					for (int i = 0; i < route.size(); i++) {
						tmpList.add(route.get(i));
					}
					tmpList.add(tmpGantry);
					shortestPathLength = tmpList.size();
					StringBuffer sb = new StringBuffer();
					for (String gantry : tmpList) {
						if (count != 0) {
							System.out.print("-->");
							sb.append("-->");
						}
						System.out.print(gantry);
						sb.append(gantry);
						count++;
					}
					try {
						bw.write(sb.toString()+"\n");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
	}

	public void printNext(ArrayList<String> route) {
		// System.out.println(route.get(route.size()-1));
		for (String tmpGantry : gantryMap.get(route.get(route.size() - 1))) {
			System.out.println(tmpGantry);
		}
	}

	public void run(String startGantry, String endGantry) {
		loadGantryMap();// 載入天氣偵測臺清單
		System.out.println(gantryMap.size());
		System.out.println("Load Gantry Finished.");
		ArrayList<String> routeArr = new ArrayList<String>();
		routeArr.add(startGantry);
		getRoute(routeArr, endGantry);
		closeConns();
		
		// printNext(routeArr);
	}

	public void closeConns() {
		try {
			con.close();
			bw.close();
			fw.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
