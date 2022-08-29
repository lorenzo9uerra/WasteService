//@ts-check
// ^^ attiva controllo di tipi TypeScript-like in VSCode

function stylize() {
	$(".title").addClass("w3-indigo")

	const toMatchWidth = ["#trolley", "#storage"]
	toMatchWidth.forEach(function(v) {
		var greatestWidth = 0;
		$(v + " .info p:first-child").each(function() {
			var width = $(this).width();
	
			if( width > greatestWidth) {
				greatestWidth = width;
			}
		});
	
		$(v + " .info p:first-child").width(greatestWidth);
	});

	matchStorageContentWitdth();

	$(".ledbox").addClass("w3-circle")
}

var ws;

var sentRequest = false;
var waitingForPickup = false;

var storage = {}
var storageMax = {}

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
			handleStorage("plastic", dataContent)
		} else if (dataType.startsWith("depositedGlass")) {
			handleStorage("glass", dataContent)
		} else if (dataType.startsWith("trolleyPosition")) {
			$("#trolleyPosition").text(dataContent)
		} else if (dataType.startsWith("ledState")) {
			handleLedState(dataContent)
		} else if (dataType.startsWith("max")) {
			const wasteType = dataType.split("max")[1].toLowerCase()
			storageMax[wasteType] = parseFloat(dataContent)
			console.log("Set storage max for " + wasteType + " to " + parseFloat(dataContent))
		}
	}
	ws.onopen = function(ev) {
		setConnected(true);
		ws.send("get")
		ws.send("getstoragemax")
		console.log("WebSocket connected")	
	}
	ws.onclose = function(ev) {
		setConnected(false);
		console.log("WebSocket disconnected");	
	}
}

/** @param {string} state */
function handleLedState(state) {
	switch(state.toUpperCase()) {
		case "ON":
			$(".ledbox").removeClass("off")
						.removeClass("blink")
						.addClass("on")
			$(".ledbox p").text("Led on")
			break;
		case "BLINKING": 
			$(".ledbox").removeClass("off")
						.addClass("blink")
						.removeClass("on")
			$(".ledbox p").text("Led blinking")
			break;
		case "OFF":
			$(".ledbox").addClass("off")
						.removeClass("blink")
						.removeClass("on")
			$(".ledbox p").text("Led off")
			break;
		default:
			console.error(`Led state '${state}' unknown!`)
	}
}

/** @param {string} type */
function handleStorage(type, amountString) {
	const amount = parseFloat(amountString)
	storage[type] = amount
	updateStorage()
}

function updateStorage() {
	const types = ["glass", "plastic"]

	clearStorageContentWidth()
	types.forEach(function(type) {
		const amount = storage[type]
		const max = storageMax[type] || 50
		$(`#deposited${type.slice(0,1).toUpperCase()}${type.slice(1)}`).text(`${amount} / ${max}`)

		const fillPct = Math.max(1, amount * 100 / max)
		const bar = $(`#${type}-bar`)
		bar.width(`${fillPct}%`)

		if (fillPct < 0) {
			bar.addClass("bar-full")
				.removeClass("bar-half")
				.removeClass("bar-almostfull")
		} else if (fillPct < 50) {
			bar.removeClass("bar-half")
			   .removeClass("bar-almostfull")
			   .removeClass("bar-full")
		} else if (fillPct < 75) {
			if (!bar.hasClass("bar-half")) {
				bar.addClass("bar-half")
				   .removeClass("bar-almostfull")
				   .removeClass("bar-full")
			}
		} else if (fillPct < 99) {
			if (!bar.hasClass("bar-almostfull")) {
				bar.addClass("bar-almostfull")
				   .removeClass("bar-half")
				   .removeClass("bar-full")
			}
		} else {
			if (!bar.hasClass("bar-full")) {
				bar.addClass("bar-full")
				   .removeClass("bar-half")
				   .removeClass("bar-almostfull")
			}
		}
	})
	matchStorageContentWitdth()
}

function clearStorageContentWidth() {
	var toMatch = ["#depositedGlass", "#depositedPlastic"]
	const match = toMatch.join(",")
	// $(match).width("initial")
}

function matchStorageContentWitdth() {
	var greatestWidth = 0;
	var toMatch = ["#depositedGlass", "#depositedPlastic"]
	const match = toMatch.join(",")
	$(match).each(function() {
		var width = $(this).width();

		if( width > greatestWidth) {
			greatestWidth = width;
		}
	});
	// console.log(greatestWidth)

	// $(match).width(greatestWidth);
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

	stylize()
});
