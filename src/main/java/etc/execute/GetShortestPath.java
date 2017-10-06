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

import etc.model.Weather;

public class GetShortestPath {

	static String JDBCDriver = "com.cloudera.impala.jdbc4.Driver";
	private ArrayList<Weather> weatherList = new ArrayList<Weather>();
	Connection con = null;

	public GetShortestPath(){
		try {
			Class.forName(JDBCDriver);
			con = DriverManager.getConnection(CONNECTION_URL);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Define a string as the connection URL
	private static final String CONNECTION_URL = "jdbc:impala://140.128.101.178:21050";

	public static void main(String[] args) {
		GetShortestPath gsp = new GetShortestPath();
		gsp.run();
		gsp.closeConns();
	}

	public void loadWeatherStationList() {
		Statement weather_stmt = null;
		ResultSet weather_rs = null;
		String weather_query = "select station_ser, station,lat,lng from etc_data.weather_station";
		try {
			weather_stmt = con.createStatement();
			weather_rs = weather_stmt.executeQuery(weather_query);
			while (weather_rs.next()) {
				Weather weather = new Weather();
				weather.setStation_ser(weather_rs.getString("station_ser"));
				weather.setStation(weather_rs.getString("station"));
				weather.setLat(weather_rs.getFloat("lat"));
				weather.setLng(weather_rs.getFloat("lng"));
				weatherList.add(weather);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				weather_rs.close();
				weather_stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	// 取得最近的天氣站點
	public Weather getNearestWeatherStation(float lat, float lng) {
		
		Weather nearestWeather = weatherList.get(0);
		double distance = Math.sqrt(Math.pow(lat - nearestWeather.getLat(), 2) + Math.pow(lng - nearestWeather.getLng(), 2));
		
		for (Weather weather : weatherList) {
			double tmpDistance = Math.sqrt(Math.pow(lat - weather.getLat(), 2) + Math.pow(lng - weather.getLng(), 2));
			if (tmpDistance <= distance) {
				nearestWeather = weather;
				distance = tmpDistance;
			}
		}
		System.out.println(lat+"|"+lng);	
		System.out.println(distance);
		return nearestWeather;
	}

	public void run() {
		loadWeatherStationList();// 載入天氣偵測臺清單
		System.out.println("Load Station Finished.");
		Statement etc_stmt = null;
		ResultSet etc_rs = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		String etc_query = "select stationid, lat,lng from etc_data.dim_highway_p2";

		try {
			fw = new FileWriter("C:\\Users\\skipandsnow\\Google 雲端硬碟\\外部競賽\\交通部ETC競賽\\station_nearest.csv");
			bw = new BufferedWriter(fw);
			etc_stmt = con.createStatement();

			etc_rs = etc_stmt.executeQuery(etc_query);

			while (etc_rs.next()) {
				float lat = etc_rs.getFloat("lat");
				float lng = etc_rs.getFloat("lng");
				
				Weather weather = getNearestWeatherStation(lat, lng);
				bw.write(etc_rs.getString("stationid") + "," + weather.getStation_ser()+"\n");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	public void closeConns(){
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
