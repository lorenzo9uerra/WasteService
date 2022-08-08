%====================================================================================
% wasteservice_req_gui description   
%====================================================================================
context(ctxpro_gui, "localhost",  "TCP", "8050").
 qactor( gui_gui, ctxpro_gui, "it.unibo.gui_gui.Gui_gui").
  qactor( trolley_gui, ctxpro_gui, "it.unibo.trolley_gui.Trolley_gui").
  qactor( led_gui, ctxpro_gui, "it.unibo.led_gui.Led_gui").
  qactor( storage_gui, ctxpro_gui, "it.unibo.storage_gui.Storage_gui").
