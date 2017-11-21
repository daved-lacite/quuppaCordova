package com.quuppaCordova.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class quuppaCordova extends CordovaPlugin{
	private Random rand = new Random();
	private int num = rand.nextInt(50);

	public quuppaCordova(){}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException{
		JSONObject send = new JSONObject();
		if(action.equals("getData")){
			send.put("num", num);
			send.put("x", 50);
			send.put("y", 0);
			send.put("z", 50);

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
	}
}