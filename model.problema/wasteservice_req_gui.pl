%====================================================================================
% wasteservice_req_gui description   
%====================================================================================
context(ctxpro_gui, "localhost",  "TCP", "8050").
 qactor( gui_wasteservicestatusgui, ctxpro_gui, "it.unibo.gui_wasteservicestatusgui.Gui_wasteservicestatusgui").
  qactor( trolley_gui, ctxpro_gui, "it.unibo.trolley_gui.Trolley_gui").
  qactor( blinkled_gui, ctxpro_gui, "it.unibo.blinkled_gui.Blinkled_gui").
  qactor( storage_gui, ctxpro_gui, "it.unibo.storage_gui.Storage_gui").
