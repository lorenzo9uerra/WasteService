//@ts-check
// ^^ attiva controllo di tipi TypeScript-like in VSCode

// @ts-ignore
const MSG_LOADACCEPT = "loadaccept";
// @ts-ignore
const MSG_LOADREJECTED = "loadrejected";
// @ts-ignore
const MSG_PICKUP = "pickedUp"

var ws;

var sentRequest = false;
var waitingForPickup = false;

function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function connect() {
	ws = new WebSocket('ws://localhost:8080/truck');
	ws.onmessage = function(data) {
		if (data.data === MSG_LOADACCEPT) {
			$("#loadreply").text(`Carico accettato, in attesa del Trolley...`)
			waitingForPickup = true;
		} else if (data.data === MSG_LOADREJECTED) {
			$("#loadreply").text(`Carico rifiutato, i cassonetti sono pieni. Torna più tardi.`)
		} else if (data.data === MSG_PICKUP && waitingForPickup) {
			$("#loadreply").text(`Carico accettato e caricato dal Trolley!`)
			waitingForPickup = false;
		} else {
			$("#loadreply").text(`Risposta sconosciuta o inaspettata! È: ${data.data}`)
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

// function sendData() {
// 	var data = JSON.stringify({
// 		'user' : $("#user").val()
// 	})
// 	ws.send(data);
// }

function request() {
	if (!sentRequest) {
		var type = $("#wastetype").val();
		var amount = $("#wasteamount").val();
		var message = `loadDeposit(${type}, ${amount})`;
		ws.send(message);
	
		console.log(`Sent request for ${message}`)
		$("#loadreply").text(`Richiesta inviata, attendo risposta da WasteService...`)
		sentRequest = true;
	} else {
		console.error("Cannot send request again")
	}
}

function checkRequestButton() {
	var hasValue = $("#wasteamount").val() !== ""
	$("#request").prop("disabled", !hasValue && !sentRequest)
}

$(function() {
    connect();

	$("form").on('submit', function(e) {
		e.preventDefault();
	});
	// $("#connect").click(function() {
	// 	connect();
	// });
	// $("#disconnect").click(function() {
	// 	disconnect();
	// });
	$("#wasteamount").on("change", function() {
		checkRequestButton()
	})
	checkRequestButton()
	$("#request").on("click", function() {
		request();
	});
});