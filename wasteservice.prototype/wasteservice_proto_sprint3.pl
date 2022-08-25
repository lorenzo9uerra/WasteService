%====================================================================================
% wasteservice_proto_sprint3 description   
%====================================================================================
context(ctx_wasteservice_proto_ctx, "localhost",  "TCP", "8050").
 qactor( ledcontroller, ctx_wasteservice_proto_ctx, "it.unibo.ledcontroller.Ledcontroller").
  qactor( blinkled, ctx_wasteservice_proto_ctx, "it.unibo.blinkled.Blinkled").
  qactor( wasteservicestatusgui, ctx_wasteservice_proto_ctx, "it.unibo.wasteservicestatusgui.Wasteservicestatusgui").
  qactor( wasteservice, ctx_wasteservice_proto_ctx, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice_proto_ctx, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice_proto_ctx, "it.unibo.storagemanager.Storagemanager").
  qactor( wastetruck, ctx_wasteservice_proto_ctx, "it.unibo.wastetruck.Wastetruck").
  qactor( pathexec, ctx_wasteservice_proto_ctx, "it.unibo.pathexec.Pathexec").
  qactor( timer, ctx_wasteservice_proto_ctx, "it.unibo.timer.Timer").
  qactor( sonarshim, ctx_wasteservice_proto_ctx, "it.unibo.sonarshim.Sonarshim").
  qactor( sonarinterrupter, ctx_wasteservice_proto_ctx, "it.unibo.sonarinterrupter.Sonarinterrupter").
