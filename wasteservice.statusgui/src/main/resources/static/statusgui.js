//@ts-check
// ^^ attiva controllo di tipi TypeScript-like in VSCode
var ws;

var sentRequest = false;
var waitingForPickup = false;

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function connect() {
	ws = new WebSocket('ws://localhost:8080/statusgui');
	ws.onmessage = function(data) {
		if (data.data.startsWith("trolleyState") ) {
			$("#trolleyState").text(data.data.split(": ")[1])
		} else if (data.data.startsWith("depositedPlastic")) {
			$("#depositedPlastic").text(data.data.split(": ")[1])
		} else if (data.data.startsWith("depositedGlass")) {
			$("#depositedGlass").text(data.data.split(": ")[1])
		} else if (data.data.startsWith("trolleyPosition")) {
			$("#trolleyPosition").text(data.data.split(": ")[1])
		} else if (data.data.startsWith("ledState")) {
			$("#ledState").text(data.data.split(": ")[1])
		}
	}
	setConnected(true);
    console.log("WebSocket connected")
}

function disconnect() {
	if (ws != null) {
		ws.close();
	}
	setConnected(false);
	console.log("WebSocket disconnected");
}

$(function() {
    connect();
});