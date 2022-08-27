//@ts-check
// ^^ attiva controllo di tipi TypeScript-like in VSCode
var ws;

var sentRequest = false;
var waitingForPickup = false;

var dlimit = -1

function connect() {
	ws = new WebSocket(`ws://${window.location.host}/ws`);
	ws.onmessage = function(data) {
		console.log("received", data.data)
		const ledRegex = /(?<=ledUpdate\()\w+(?=\))/
		const dlimitRegex = /(?<=dlimit\()\d+(?=\))/
		if (ledRegex.test(data.data)) {
			const isOn = ledRegex.exec(data.data)[0] === "true"
			if (isOn) {
				$(".ledbox.off").each(function(i, obj) {
					obj.classList.remove("off")
					obj.classList.add("on")
				});
				$(".ledbox p").text("Led On")
			} else {
				$(".ledbox.on").each(function(i, obj) {
					obj.classList.remove("on")
					obj.classList.add("off")
				});
				$(".ledbox p").text("Led Off")
			}
		} else if (dlimitRegex.test(data.data)) {
			dlimit = parseInt( dlimitRegex.exec(data.data)[0] )
			$("#sonarSlider").val(dlimit + 10)
							 .next().text(`${dlimit + 10} / ${dlimit}`)
			ws.send(`setSonar(${dlimit + 10})`)
		}
	}
	ws.onopen = function(ev) {
		ws.send("get()")
		console.log("WebSocket connected")	
	}
	ws.onclose = function(ev) {
		console.log("WebSocket disconnected");	
	}
}

function disconnect() {
	if (ws != null) {
		ws.close();
	}
	console.log("WebSocket disconnected from this side");
}

$(function() {
    connect();

	$("#sonarButton").on("mousedown", function(ev) {
		ws.send(`setSonar(${dlimit - 10})`)
		console.debug(`sent setSonar(${dlimit - 10})`)
	});
	$("#sonarButton").on("mouseup", function(ev) {
		ws.send(`setSonar(${dlimit + 10})`)
		console.debug(`sent setSonar(${dlimit + 10})`)
	});

	$("#sonarSlider").on("input", function(ev) {
		/** @type {HTMLInputElement} */
		// @ts-ignore
		const el = ev.target
		el.nextElementSibling.textContent = `${el.value} / ${dlimit}`

		ws.send(`setSonar(${el.value})`)
		console.debug(`sent setSonar(${el.value})`)
	})

	style()
});

function style() {
	$(".ledbox").addClass("w3-circle")
	$(".container").addClass("w3-container")
	$("#sonarButton").addClass("w3-xxlarge").addClass("w3-red").addClass("box")
}