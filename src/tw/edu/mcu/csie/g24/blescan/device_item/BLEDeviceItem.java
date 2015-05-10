package tw.edu.mcu.csie.g24.blescan.device_item;

public class BLEDeviceItem {
	private String address = "";
	private String rssi = "";
	private String name = "";
	public BLEDeviceItem() {
	}

	public BLEDeviceItem(String name, String address, String rssi) {
		this.address = address;
		this.rssi = rssi;
		this.name = name;
	}


	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}
	public String getName() {
		return address;
	}

	public void setName(String name) {
		this.name = name;
	}
	public String toString() {
		String str = "";
		
		str += "{";
		str += "name:" + name +",";
		str += "address:" + address + ", ";
		str += "rssi:" + rssi + ", ";
		str += "}";
		
		return str;
	}
}