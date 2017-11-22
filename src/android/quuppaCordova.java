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
import java.net.*;

public class quuppaCordova extends CordovaPlugin{
	Receiver receiver = new Receiver();

	public quuppaCordova(){}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException{
		JSONObject send = new JSONObject();
		if(action.equals("getData")){
			send.put("obj", receiver.getInfo());
			/*try{
				dsocket.receive(packet);
				obj = new String(buffer, 0, packet.getLength());
			}catch(Exception e){
            	System.err.println(e);
        	}*/
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
	    Thread tagInfo = new Thread(receiver);
	    tagInfo.start();
	}

	private class Receiver implements Runnable{
		private InetSocketAddress add = new InetSocketAddress(22104);
		private DatagramChannel channel;
	    private DatagramSocket dsocket;
	    private byte[] buffer = new byte[2048];
	    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	    private String obj;
	    
	    public Receiver(){}

		@Override
		public void run(){
			try{
		    	channel = DatagramChannel.open();
		    	dsocket = channel.socket();
		    	dsocket.bind(add);
		    	
		    	while(true){
					dsocket.receive(packet);
					obj = new String(buffer, 0, packet.getLength());
				}
		    }catch(Exception e){
	            System.err.println(e);
	        }
		}

		public String getInfo(){
			return obj;
		}
	}
}