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
	ws = new WebSocket(`ws://${window.location.host}/statusgui`);
	ws.onmessage = function(data) {
		console.log("received", data.data)
		const dataType = data.data.split(": ")[0]
		const dataContent = data.data.split(": ")[1]
		if (dataType.startsWith("trolleyState") ) {
			$("#trolleyState").text(dataContent)
		} else if (dataType.startsWith("trolleyActivity")) {
			$("#trolleyActivity").text(dataContent)
		} else if (dataType.startsWith("depositedPlastic")) {
			$("#depositedPlastic").text(dataContent)
		} else if (dataType.startsWith("depositedGlass")) {
			$("#depositedGlass").text(dataContent)
		} else if (dataType.startsWith("trolleyPosition")) {
			$("#trolleyPosition").text(dataContent)
		} else if (dataType.startsWith("ledState")) {
			$("#ledState").text(dataContent)
		}
	}
	ws.onopen = function(ev) {
		setConnected(true);
		ws.send("get")
		console.log("WebSocket connected")	
	}
	ws.onclose = function(ev) {
		setConnected(false);
		console.log("WebSocket disconnected");	
	}
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