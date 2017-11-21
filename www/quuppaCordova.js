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

window.quuppaTagLoc = new quuppaCordova();
window.plugins = window.plugins || {};
window.plugins.quuppaTagLoc = window.quuppaTagLoc;