%====================================================================================
% wasteservice_req_gui description   
%====================================================================================
context(ctxreq_gui, "localhost",  "TCP", "8050").
 qactor( gui_gui, ctxreq_gui, "it.unibo.gui_gui.Gui_gui").
  qactor( trolley_gui, ctxreq_gui, "it.unibo.trolley_gui.Trolley_gui").
  qactor( led_gui, ctxreq_gui, "it.unibo.led_gui.Led_gui").
