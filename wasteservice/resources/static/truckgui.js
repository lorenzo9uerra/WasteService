var ws;
function setConnected(connected) {
	$("#connect").prop("disabled", connected);
	$("#disconnect").prop("disabled", !connected);
}

function connect() {
	ws = new WebSocket('ws://localhost:8080/truck');
	ws.onmessage = function(data) {
		helloWorld(data.data);
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
    var type = $("#wastetype").val();
    var amount = $("#wasteamount").val();
    var message = `loadDeposit(${type}, ${amount})`;
    ws.send(message);

	console.log(`Sent request for ${message}`)
}

function checkRequestButton() {
	var hasValue = $("#wasteamount").val !== ""
	$("#request").prop("disabled", !hasValue)
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