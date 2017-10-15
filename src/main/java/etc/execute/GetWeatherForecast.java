package etc.execute;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GetWeatherForecast {
	private static String api_key = "CWB-9A71E53D-CEEF-4FE0-A170-85B3ADAE4482";
	private static String url_front = "http://opendata.cwb.gov.tw/opendataapi";

	public static void main(String[] args) throws Exception {
		GetWeatherForecast getWeatherForecast = new GetWeatherForecast();
		getWeatherForecast.get7DaysForcast();
		// getWeatherForecast.get7DaysForcastD4j();
	}

	/** 7天的天氣預測 */
	public void get7DaysForcast() {
		ReadFile rf = new ReadFile();
		ArrayList<String> dataidList = rf.readList("data/seven_days_forecast");
		HashMap<String, Forecast> forecastMap = new HashMap<String, Forecast>();
		BufferedWriter bw = null;
		FileWriter fw = null;

		for (String dataid : dataidList) {
			String sevenDaysForecast = getRestKey(dataid); // 首先取得XML檔內容
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(new InputSource(new StringReader(sevenDaysForecast)));
				NodeList locationList = document.getElementsByTagName("location");
				String city = document.getElementsByTagName("locationsName").item(0).getTextContent()
						.replaceAll("\\s*|\t|\r|\n", "");
				for (int i = 0; i < locationList.getLength(); i++) {
					Element locationElm = (Element) locationList.item(i);
					String area = locationElm.getElementsByTagName("locationName").item(0).getTextContent();
					String latitude = locationElm.getElementsByTagName("lat").item(0).getTextContent();
					String longitude = locationElm.getElementsByTagName("lon").item(0).getTextContent();
					NodeList weatherElmList = document.getElementsByTagName("weatherElement");// 每個地區的氣候

					/** 擷取溫度的 **/
					Element tElm = (Element) weatherElmList.item(0);
					String type = tElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					NodeList timeElmList = tElm.getElementsByTagName("time");
					for (int z = 0; z < timeElmList.getLength(); z++) {
						Element timeElm = (Element) timeElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList elmValueList = timeElm.getElementsByTagName("elementValue");
						for (int q = 0; q < elmValueList.getLength(); q++) {
							Element valueElm = (Element) elmValueList.item(q);
							String value = valueElm.getElementsByTagName("value").item(0).getTextContent();// 溫度
							String measures = valueElm.getElementsByTagName("measures").item(0).getTextContent();// 度量標準
							String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
							if (forecastMap.containsKey(key)) {

								forecastMap.get(key).setTemperature(getCleanName(value));
								forecastMap.get(key).setTemperatureMeasure(getCleanName(measures));
							} else {

								Forecast forecast = new Forecast();
								forecast.setStartTime(getCleanName(startTime));
								forecast.setEndTime(getCleanName(endTime));
								forecast.setCity(getCleanName(city));
								forecast.setArea(getCleanName(area));
								forecast.setLatitude(getCleanName(latitude));
								forecast.setLongitude(getCleanName(longitude));
								forecast.setTemperature(getCleanName(value));
								forecast.setTemperatureMeasure(getCleanName(measures));
								forecastMap.put(key, forecast);
							}
						}
					}
					/** 擷取溫度2的 **/
					Element tdElm = (Element) weatherElmList.item(1);
					type = tdElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					NodeList tdElmList = tdElm.getElementsByTagName("time");
					for (int z = 0; z < tdElmList.getLength(); z++) {
						Element timeElm = (Element) tdElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList elmValueList = timeElm.getElementsByTagName("elementValue");
						for (int q = 0; q < elmValueList.getLength(); q++) {
							Element valueElm = (Element) elmValueList.item(q);
							String value = valueElm.getElementsByTagName("value").item(0).getTextContent();// 溫度
							String measures = valueElm.getElementsByTagName("measures").item(0).getTextContent();// 度量標準
							String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
							if (forecastMap.containsKey(key)) {
								forecastMap.get(key).setTemperature_d(getCleanName(value));
								forecastMap.get(key).setTemperatureMeasure_d(getCleanName(measures));
							} else {
								Forecast forecast = new Forecast();
								forecast.setStartTime(getCleanName(startTime));
								forecast.setEndTime(getCleanName(endTime));
								forecast.setCity(getCleanName(city));
								forecast.setArea(getCleanName(area));
								forecast.setTemperature_d(getCleanName(value));
								forecast.setTemperatureMeasure_d(getCleanName(measures));
								forecastMap.put(key, forecast);
							}
						}
					}
					// 擷取濕度的
					Element rhElm = (Element) weatherElmList.item(2);
					type = rhElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					NodeList rhElmList = rhElm.getElementsByTagName("time");
					for (int z = 0; z < rhElmList.getLength(); z++) {
						Element timeElm = (Element) rhElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList elmValueList = timeElm.getElementsByTagName("elementValue");
						for (int q = 0; q < elmValueList.getLength(); q++) {
							Element valueElm = (Element) elmValueList.item(q);
							String value = valueElm.getElementsByTagName("value").item(0).getTextContent();// 溫度
							String measures = valueElm.getElementsByTagName("measures").item(0).getTextContent();// 度量標準
							String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
							if (forecastMap.containsKey(key)) {
								forecastMap.get(key).setRh(getCleanName(value));
								forecastMap.get(key).setRhMeasure(getCleanName(measures));
							} else {
								Forecast forecast = new Forecast();
								forecast.setStartTime(getCleanName(startTime));
								forecast.setEndTime(getCleanName(endTime));
								forecast.setCity(getCleanName(city));
								forecast.setArea(getCleanName(area));
								forecast.setRh(getCleanName(value));
								forecast.setRhMeasure(getCleanName(measures));
								forecastMap.put(key, forecast);
							}
						}
					}
					// 擷取風力的
					Element windElm = (Element) weatherElmList.item(3);
					type = windElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					NodeList windElmList = windElm.getElementsByTagName("time");
					for (int z = 0; z < windElmList.getLength(); z++) {
						Element timeElm = (Element) windElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList parameterList = timeElm.getElementsByTagName("parameter");
						String wind_direction = "";
						String wind_direction_unit = "";
						String wind_describe = "";
						String wind_speed = "";
						String wind_speed_unit = "";
						String wind_speed_lv = "";
						String wind_speed_lv_unit = "";
						for (int q = 0; q < parameterList.getLength(); q++) {
							// System.out.println(q);
							Element parameterElm = (Element) parameterList.item(q);
							String parameterName = parameterElm.getElementsByTagName("parameterName").item(0)
									.getTextContent();// 風力參數分類
							if (getCleanName(parameterName).equals("風向縮寫")) {
								wind_direction = parameterElm.getElementsByTagName("parameterValue").item(0)
										.getTextContent();// 風力數值
								wind_direction_unit = parameterElm.getElementsByTagName("parameterUnit").item(0)
										.getTextContent();// 風力參數單位
							} else if (getCleanName(parameterName).equals("風向描述")) {
								wind_describe = parameterElm.getElementsByTagName("parameterValue").item(0)
										.getTextContent();// 風力數值

							} else if (getCleanName(parameterName).equals("風速")) {
								wind_speed = parameterElm.getElementsByTagName("parameterValue").item(0)
										.getTextContent();// 風力數值
								wind_speed_unit = parameterElm.getElementsByTagName("parameterUnit").item(0)
										.getTextContent();// 風力參數單位
							} else if (getCleanName(parameterName).equals("風級")) {
								wind_speed_lv = parameterElm.getElementsByTagName("parameterValue").item(0)
										.getTextContent();// 風力數值
								wind_speed_lv_unit = parameterElm.getElementsByTagName("parameterUnit").item(0)
										.getTextContent();// 風力參數單位
							}
						}
						String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
						if (forecastMap.containsKey(key)) {
							forecastMap.get(key).setWind_describe(getCleanName(wind_describe));
							forecastMap.get(key).setWind_direction(getCleanName(wind_direction));
							forecastMap.get(key).setWind_direction_unit(getCleanName(wind_direction_unit));
							forecastMap.get(key).setWind_speed(getCleanName(wind_speed_lv));
							forecastMap.get(key).setWind_speed_unit(getCleanName(wind_speed_unit));
							forecastMap.get(key).setWind_speed_lv(getCleanName(wind_speed_lv));
							forecastMap.get(key).setWind_speed_lv_unit(getCleanName(wind_speed_lv_unit));
							// forecastMap.get(key).setWindUnit(getCleanName(windUnit));
						} else {
							Forecast forecast = new Forecast();
							forecast.setStartTime(getCleanName(startTime));
							forecast.setEndTime(getCleanName(endTime));
							forecast.setCity(getCleanName(city));
							forecast.setArea(getCleanName(area));
							forecast.setWind_describe(getCleanName(wind_describe));
							forecast.setWind_direction(getCleanName(wind_direction));
							forecast.setWind_speed(getCleanName(wind_speed));
							forecast.setWind_speed_lv(getCleanName(wind_speed_lv));
							forecastMap.put(key, forecast);
						}
					}
					/** 擷取降雨機率的 **/
					Element popElm = (Element) weatherElmList.item(5);
					type = popElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					// System.out.println(type);
					NodeList popElmList = popElm.getElementsByTagName("time");
					for (int z = 0; z < popElmList.getLength(); z++) {
						Element timeElm = (Element) popElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList elmValueList = timeElm.getElementsByTagName("elementValue");
						for (int q = 0; q < elmValueList.getLength(); q++) {
							Element valueElm = (Element) elmValueList.item(q);
							String value = valueElm.getElementsByTagName("value").item(0).getTextContent();// 降雨機率
							String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
							if (forecastMap.containsKey(key)) {
								forecastMap.get(key).setPop(getCleanName(value));
							} else {
								Forecast forecast = new Forecast();
								forecast.setStartTime(getCleanName(startTime));
								forecast.setEndTime(getCleanName(endTime));
								forecast.setCity(getCleanName(city));
								forecast.setArea(getCleanName(area));
								forecast.setPop(getCleanName(value));
								forecastMap.put(key, forecast);
							}
						}
					}

					/** 擷取紫外線的 **/
					Element uviElm = (Element) weatherElmList.item(12);
					type = uviElm.getElementsByTagName("elementName").item(0).getTextContent();// 指標名稱
					NodeList uviElmList = uviElm.getElementsByTagName("time");
					for (int z = 0; z < uviElmList.getLength(); z++) {
						Element timeElm = (Element) uviElmList.item(z);
						String startTime = timeElm.getElementsByTagName("startTime").item(0).getTextContent();// 開始時間
						String endTime = timeElm.getElementsByTagName("endTime").item(0).getTextContent();// 結束時間
						NodeList parameterList = timeElm.getElementsByTagName("parameter");
						String uvi = "";
						for (int q = 0; q < parameterList.getLength(); q++) {
							Element parameterElm = (Element) parameterList.item(q);
							String parameterName = parameterElm.getElementsByTagName("parameterName").item(0)
									.getTextContent();// 風力參數分類
							// System.out.println(parameterName);
							if (getCleanName(parameterName).equals("紫外線指數")) {
								uvi = parameterElm.getElementsByTagName("parameterValue").item(0).getTextContent();// 風力數值
							}
						}
						String key = getCleanName(city + "|" + area + "|" + startTime + "|" + endTime);
						if (forecastMap.containsKey(key)) {
							forecastMap.get(key).setUvi(getCleanName(uvi));
						} else {
							Forecast forecast = new Forecast();
							forecast.setStartTime(getCleanName(startTime));
							forecast.setEndTime(getCleanName(endTime));
							forecast.setCity(getCleanName(city));
							forecast.setArea(getCleanName(area));
							forecast.setUvi(getCleanName(uvi));
							forecastMap.put(key, forecast);
						}
					}

				}
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Iterator iter = forecastMap.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		sb.append(
				"city,area,latitude,longitude,start_time,end_time,temperature,temperature_measure,temperature_d,temperature_measure_d,");
		sb.append(
				"rh,rh_measure,wind_describe,wind_direction,wind_direction_unit,wind_speed,wind_speed_unit,wind_speed_lv,wind_speed_lv_unit,pop,uvi\n");
		sb.append("縣市,地區,緯度,經度,開始時間,結束時間,t溫度,t溫度度量,td溫度,td溫度度量,");
		sb.append("濕度,濕度度量,風向描述,風向縮寫,風向縮寫_單位,風速,風速_單位,風速等級,風速等級_單位,降雨機率,紫外線指數\n");
		System.out.print(sb.toString());
		try {
//			fw = new FileWriter("C:\\Users\\skipandsnow\\Google 雲端硬碟\\外部競賽\\交通部ETC競賽\\weather_forecast_7days.csv");
			fw = new FileWriter("G:\\google雲端硬碟\\外部競賽\\交通部ETC競賽\\weather_forecast_7days.csv");
			bw = new BufferedWriter(fw);
//			bw.write(sb.toString());
			while (iter.hasNext()) {
				Object key = iter.next();
				Object val = forecastMap.get(key);
				Forecast forecast = (Forecast) val;
				
				for (int i = 0; i < 12; i++) {
					sb.setLength(0);
					sb.append(forecast.getCity() + ",");
					sb.append(forecast.getArea() + ",");
					sb.append(forecast.getLatitude() + ",");
					sb.append(forecast.getLongitude() + ",");
					sb.append(forecast.getStartTime() + ",");
					sb.append(forecast.getEndTime() + ",");
					if (getCleanName(forecast.getStartTime()).substring(11, 13).equals("06")) {
						sb.append(getCleanName(forecast.getStartTime()).substring(0, 10) + ",");// 日期
						if (i+6 < 10) {
							sb.append("0"+ (i+6) + ",");// 日期
						}else{
							sb.append((i+6) + ",");//小時
						}
					}else{
						if (i+18 < 24) {
							sb.append(getCleanName(forecast.getStartTime()).substring(0, 10) + ",");// 日期
							sb.append(i+18 + ",");// 日期
						}else{
							sb.append(getCleanName(forecast.getEndTime()).substring(0, 10) + ",");// 日期
							sb.append("0"+ (i-6) + ",");//小時
						}
					}
					sb.append(forecast.getTemperature() + ",");
					sb.append(forecast.getTemperatureMeasure() + ",");
					sb.append(forecast.getTemperature_d() + ",");
					sb.append(forecast.getTemperatureMeasure_d() + ",");
					sb.append(forecast.getRh() + ",");
					sb.append(forecast.getRhMeasure() + ",");
					sb.append(forecast.getWind_describe() + ",");
					sb.append(forecast.getWind_direction() + ",");
					sb.append(forecast.getWind_direction_unit() + ",");
					sb.append(forecast.getWind_speed() + ",");
					sb.append(forecast.getWind_speed_unit() + ",");
					sb.append(forecast.getWind_speed_lv() + ",");
					sb.append(forecast.getWind_speed_lv_unit() + ",");
					sb.append(forecast.getPop() + ",");
					sb.append(forecast.getUvi() + "\n");
					bw.write(sb.toString());
					System.out.print(sb.toString());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private String getCleanName(String name) {
		return name.replaceAll("\\s*|\t|\r|\n", "");
	}

	private String getRestKey(String dataId) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url_front + "?dataid=" + dataId + "&authorizationkey=" + api_key);
		// System.out.println(url_front + "?dataid=" + dataId +
		// "&authorizationkey=" + api_key);
		HttpEntity entity = null;
		String responseContent = null;
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpGet);
			entity = response.getEntity();
			// responseContent = EntityUtils.toString(entity, "UTF-8");
			if (entity != null) {
				responseContent = EntityUtils.toString(entity, "UTF-8");
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				httpclient.close();
				response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return responseContent;
	}
}
