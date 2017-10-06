package etc.model;

public class Weather {
	String station_ser,station,station_date,obstime;
	float lat,lng;

	public String getStation_ser() {
		return station_ser;
	}

	public void setStation_ser(String station_ser) {
		this.station_ser = station_ser;
	}

	public String getStation_date() {
		return station_date;
	}

	public void setStation_date(String station_date) {
		this.station_date = station_date;
	}

	public String getObstime() {
		return obstime;
	}

	public void setObstime(String obstime) {
		this.obstime = obstime;
	}


	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLng() {
		return lng;
	}

	public void setLng(float lng) {
		this.lng = lng;
	}
}
