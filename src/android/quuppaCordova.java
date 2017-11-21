package com.quuppaCordova.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class quuppaCordova extends CordovaPlugin{
	public quuppaCordova(){}

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException{
		JSONObject send = new JSONObject();
		send.put("start", "Sender initialized");
		if(action.equals("getData")){
			
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