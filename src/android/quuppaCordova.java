package com.quuppaCordova.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.channels.DatagramChannel;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.provider.Settings;
import java.net.*;
import java.util.Arrays;

public class quuppaCordova extends CordovaPlugin{
	private Info info = new Info();
	private Receiver receiver = new Receiver(info);
	private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private byte dataPacketCounter = 0;
    private Object dataPacketLock = new Object();
    private String tagId;

    InetSocketAddress add = new InetSocketAddress(22104);
    DatagramSocket dsocket;
    byte[] buffer = new byte[2048];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

	public quuppaCordova(){}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException{
		JSONObject send = new JSONObject();
		if(action.equals("getData")){
			send.put("obj", info.getData());
			
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, send));
			return true;
		}else if(action.equals("getId")){
			send.put("id", tagId);

			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, send));
			return true;
		}
		send.put("error", "Method not found");

		callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, send));
		return false;
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
	    super.initialize(cordova, webView);
	    receiver.start();
	    startAdvertising();
	    String tagId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	private class Receiver implements Runnable{
		private InetSocketAddress add = new InetSocketAddress(22104);
		private DatagramChannel channel;
	    private DatagramSocket dsocket;
	    private byte[] buffer = new byte[2048];
	    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	    private String obj;
	    private Info info;
	    
	    public Receiver(Info info){
	    	this.info = info;
	    }

	    public void start(){
	    	Thread tagInfo = new Thread(new Receiver(this.info));
	    	tagInfo.start();
	    }

		@Override
		public void run(){
			try{
		    	channel = DatagramChannel.open();
		    	dsocket = channel.socket();
		    	dsocket.bind(add);
		    	
		    	while(true){
					dsocket.receive(packet);
					obj = new String(buffer, 0, packet.getLength());
					this.info.setData(obj.replaceAll("\\r|\\n", ""));
				}
		    }catch(Exception e){
	            System.err.println(e);
	        }
		}

		public String getInfo(){
			return obj;
		}
	}

	private class Info{
		private String data = null;

		public void Info(){

		}

		public String getData(){
			return this.data;
		}

		public void setData(String val){
			this.data = val;
		}
	}

	private boolean startAdvertising(){
        int mode = 1;
        int txPower = 3;
        byte[] bytes = createBytes();
        try{
            BluetoothManager btManager = (BluetoothManager) getSystemService("bluetooth");
            BluetoothAdapter btAdapter = btManager.getAdapter();
            if (btAdapter.isEnabled()) {
                AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(mode)
                        .setTxPowerLevel(txPower)
                        .setConnectable(true)
                        .build();

                AdvertiseData advertisementData = new AdvertiseData.Builder()
                        .setIncludeTxPowerLevel(false)
                        .addManufacturerData(0x00C7, bytes)
                        .build();

                bluetoothLeAdvertiser = btAdapter.getBluetoothLeAdvertiser();
                bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertisementData, dataPacketAdvCallback);
            }
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    private byte[] createBytes(){
        byte header = (byte)(1 << 4);
        header |= (3 << 2);
        header |= 1;

        byte[] bytes = new byte[]{
            (byte)0x01, // Quuppa Packet ID
            (byte)0x21, // Device Type (0x21, android)
              	header, // Payload header
            (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, // Quuppa Address payload, will be replaced shortly...
            (byte)0xb4, // checksum, calculated later
            (byte)0x67,(byte)0xF7,(byte)0xDB,(byte)0x34,(byte)0xC4,(byte)0x03,(byte)0x8E,(byte)0x5C,(byte)0x0B,(byte)0xAA,(byte)0x97,(byte)0x30,(byte)0x56,(byte)0xE6 // DF field, 14 octets
        };

        // inject Quuppa Address into byte array
        byte[] qAddress = createQuuppaAddress(tagId);
        System.arraycopy(qAddress, 0, bytes, 3, 6);

        // calculate CRC and inject
        try {
            bytes[9] = CRC8.simpleCRC(Arrays.copyOfRange(bytes, 1, 9));
        }catch(Exception e){
            return null;
        }
        return bytes;
    }

    private byte[] createQuuppaAddress(String tagID) {
        byte[] bytes = new byte[6];
        bytes[0] = (byte)Integer.parseInt(tagID.substring(0,2), 16);
        bytes[1] = (byte)Integer.parseInt(tagID.substring(2,4), 16);
        bytes[2] = (byte)Integer.parseInt(tagID.substring(4,6), 16);
        bytes[3] = (byte)Integer.parseInt(tagID.substring(6,8), 16);
        bytes[4] = (byte)Integer.parseInt(tagID.substring(8,10), 16);
        bytes[5] = (byte)Integer.parseInt(tagID.substring(10,12), 16);
        return bytes;
    }

    private final AdvertiseCallback dataPacketAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {

        }

        @Override
        public void onStartFailure(int i) {
            final String message = "Start Status broadcast failed error code: " + i;
        }
    };

    private class CRC8 {
        public static final byte INITIAL_REGISTER_VALUE = (byte)0x00;

        public static byte simpleCRC(java.io.InputStream s, byte reg) throws java.io.IOException {
            byte bitMask = (byte)(1 << 7);

            // Process each message byte.
            int value = s.read();
            while (value != -1) {
                byte element = (byte)value;

                reg ^= element;
                for (int i = 0; i < 8; i++) {
                    if ((reg & bitMask) != 0) {
                        reg = (byte)((reg << 1) ^ 0x97);
                    }
                    else {
                        reg <<= 1;
                    }
                }
                value = s.read();
            }
            reg ^= 0x00;

            return reg;
        }
        public static byte simpleCRC(byte[] buffer, byte register) throws java.io.IOException {
            java.io.ByteArrayInputStream stream = new java.io.ByteArrayInputStream(buffer);
            return simpleCRC(stream, register);
        }
        public static byte simpleCRC(byte[] buffer) throws java.io.IOException {
            return simpleCRC(buffer, INITIAL_REGISTER_VALUE);
        }
    }
}