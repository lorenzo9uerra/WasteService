%====================================================================================
% wasteservice_req_led description   
%====================================================================================
context(ctxreq_led, "localhost",  "TCP", "8050").
 qactor( led_trolley, ctxreq_led, "it.unibo.led_trolley.Led_trolley").
  qactor( led_blinkled, ctxreq_led, "it.unibo.led_blinkled.Led_blinkled").
