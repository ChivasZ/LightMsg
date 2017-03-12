package org.jivesoftware.smack.packet;

public class HeartBeatMessage extends Packet {

	private double lon;
	private double lat;
	private String child;

	public HeartBeatMessage() {
		this.lon = 99.9;
		this.lat = 99.9;
	}

	public HeartBeatMessage(String child) {
		this.child = child;
	}

	public HeartBeatMessage(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}

	public double getLon() { return lon; }
	public double getLat() { return lat; }
	public void setLon(double lon) { this.lon = lon; }
	public void setLat(double lat) { this.lat = lat; }

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		//return "TEST_HEART_BEAT";
		StringBuilder buf = new StringBuilder();
		buf.append("<hb>");
		if (child != null && !child.isEmpty()) {
			buf.append(child);
		} else {
			buf.append("<location>");
			buf.append("<lon>");
			buf.append(String.valueOf(lon));
			buf.append("</lon>");
			buf.append("<lat>");
			buf.append(String.valueOf(lat));
			buf.append("</lat>");
			buf.append("</location>");
		}
		buf.append("</hb>");
		return buf.toString();
	}

}
