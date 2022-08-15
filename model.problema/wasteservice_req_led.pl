%====================================================================================
% wasteservice_req_led description   
%====================================================================================
context(ctxpro_led, "localhost",  "TCP", "8050").
 qactor( led_trolley, ctxpro_led, "it.unibo.led_trolley.Led_trolley").
  qactor( led_ledcontroller, ctxpro_led, "it.unibo.led_ledcontroller.Led_ledcontroller").
  qactor( led_blinkled, ctxpro_led, "it.unibo.led_blinkled.Led_blinkled").
