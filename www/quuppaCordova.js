var argscheck = require('cordova/argscheck'),
    channel = require('cordova/channel'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec'),
    cordova = require('cordova');

function quuppaCordova(){
	var me = this;
}

quuppaCordova.prototype.getData = function(successCallback, failureCallback){
	argscheck.checkArgs('ff', 'quuppaCordova.getData', arguments);
	exec(successCallback, failureCallback, "quuppaCordova", "getData", []);
}

quuppaCordova.prototype.getId = function(successCallback, failureCallback){
	argscheck.checkArgs('ff', 'quuppaCordova.getId', arguments);
	exec(successCallback, failureCallback, "quuppaCordova", "getId", []);
}

window.quuppaTagLoc = new quuppaCordova();
window.plugins = window.plugins || {};
window.plugins.quuppaTagLoc = window.quuppaTagLoc;