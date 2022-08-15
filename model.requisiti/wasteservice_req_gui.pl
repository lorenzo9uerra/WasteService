%====================================================================================
% wasteservice_req_gui description   
%====================================================================================
context(ctxreq_gui, "localhost",  "TCP", "8050").
 qactor( wasteservicestatusgui_gui, ctxreq_gui, "it.unibo.wasteservicestatusgui_gui.Wasteservicestatusgui_gui").
  qactor( trolley_gui, ctxreq_gui, "it.unibo.trolley_gui.Trolley_gui").
  qactor( blinkled_gui, ctxreq_gui, "it.unibo.blinkled_gui.Blinkled_gui").
