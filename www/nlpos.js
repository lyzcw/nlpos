/**
 * This class contains information about the current pos status.
 * @constructor
 */
var cordova = require('cordova'),
    exec = require('cordova/exec');

// var nlposPlugin = {
 
//   readcard: function(success, error) {
//     return exec(success, error, 'nlpos', 'readcard', []);
//   },
//   scan: function(success, error) {
//     return exec(success, error, 'nlpos', 'scan', []);
//   }
 
// };

// module.exports = nlposPlugin;

var NLPos = function() {
    //this._level = null;
    this._isRead = null;

    this.scan = function( success, error ) {
      return exec(success, error, 'nlpos', 'scan', []);
    };

    this.print = function( bill, success, error ) {
      return exec(success, error, 'nlpos', 'print', [bill]);
    };
    // Create new event handlers on the window (returns a channel instance)
    this.channels = {
      readcard:cordova.addWindowEventHandler("readcard")
    };
    for (var key in this.channels) {
        this.channels[key].onHasSubscribersChange= NLPos.onHasSubscribersChange;
    }
};

function handlers() {
    return nlpos.channels.readcard.numHandlers;
        // +battery.channels.batterylow.numHandlers+
        // battery.channels.batterycritical.numHandlers;
}

/**
 * Event handlers for when callbacks getregistered for the pos.
 * Keep track of how many handlers wehave so we can start and stop the native pos listener
 * appropriately (and hopefully save pos life!).
 */
NLPos.onHasSubscribersChange = function() {
  // If we just registered the firsthandler, make sure native listener is started.
  // if (this.numHandlers === 1 && handlers()=== 1) {
  //     exec(nlpos._status, nlpos._error,"nlpos", "openCardReader", []);
  // } else if (handlers() === 0) {
  //     exec(null, null, "nlpos","closeCardReader", []);
  // }
  exec(nlpos._status, nlpos._error,"nlpos", "openCardReader", []);
};

// NLPos.prototype.openCardReader = function() {
//   return exec(nlpos._status, nlpos._error, 'nlpos', 'openCardReader', []);
// };

// NLPos.prototype.closeCardReader = function() {
//   return exec(nlpos._status, nlpos._error,'nlpos', 'closeCardReader', []);
// };

NLPos.prototype.openCardReader = function(success, error) {
  return exec(success, error, 'nlpos', 'openCardReader', []);
};

NLPos.prototype.closeCardReader = function(success, error) {
  return exec(success, error,'nlpos', 'closeCardReader', []);
};

NLPos.prototype.getAsynMsg = function(success, error) {
  return exec(success, error,'nlpos', 'getAsynMsg', []);
};

/**
 * Callback for nlpos status
 *
 * @param {Object} info           keys: info
 */
NLPos.prototype._status = function (info) {

    //if (info) {
        // Something changed. Fire ReadCard event
        cordova.fireWindowEvent("readcard",info);
        
    //}
};

/**
 * Error callback for NLPos start
 */
NLPos.prototype._error = function(e) {
    console.log("Error initializing NLPos: " + e);
};

var nlpos = new NLPos(); // jshint ignore:line

module.exports = nlpos;
