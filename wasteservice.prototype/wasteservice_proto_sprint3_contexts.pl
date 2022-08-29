%====================================================================================
% wasteservice_proto_sprint3_contexts description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
context(ctx_trolley, "localhost",  "TCP", "8070").
context(ctx_storagemanager, "localhost",  "TCP", "8071").
context(ctx_raspberry, "localhost",  "TCP", "8031").
context(ctx_statusgui, "localhost",  "TCP", "8095").
context(ctx_driver, "localhost",  "TCP", "9090").
context(ctx_pathexecstop, "localhost",  "TCP", "8025").
context(ctx_basicrobot, "localhost",  "TCP", "8020").
 qactor( ledcontroller, ctx_raspberry, "it.unibo.ledcontroller.Ledcontroller").
  qactor( blinkled, ctx_raspberry, "it.unibo.blinkled.Blinkled").
  qactor( wasteservicestatusgui, ctx_statusgui, "it.unibo.wasteservicestatusgui.Wasteservicestatusgui").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_trolley, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_storagemanager, "it.unibo.storagemanager.Storagemanager").
  qactor( wastetruck, ctx_driver, "it.unibo.wastetruck.Wastetruck").
  qactor( pathexecstop, ctx_pathexecstop, "it.unibo.pathexecstop.Pathexecstop").
  qactor( basicrobot, ctx_basicrobot, "it.unibo.basicrobot.Basicrobot").
  qactor( sonarshim, ctx_raspberry, "it.unibo.sonarshim.Sonarshim").
  qactor( sonarinterrupter, ctx_trolley, "it.unibo.sonarinterrupter.Sonarinterrupter").
