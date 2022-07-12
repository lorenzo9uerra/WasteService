%====================================================================================
% wasteservice_req_led description   
%====================================================================================
context(ctxreq_led, "localhost",  "TCP", "8050").
 qactor( trolley, ctxreq_led, "it.unibo.trolley.Trolley").
  qactor( led, ctxreq_led, "it.unibo.led.Led").
