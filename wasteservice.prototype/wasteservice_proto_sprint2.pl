%====================================================================================
% wasteservice_proto_sprint2 description   
%====================================================================================
context(ctx_wasteservice_proto_ctx, "localhost",  "TCP", "8050").
 qactor( ledcontroller, ctx_wasteservice_proto_ctx, "it.unibo.ledcontroller.Ledcontroller").
  qactor( blinkled, ctx_wasteservice_proto_ctx, "it.unibo.blinkled.Blinkled").
  qactor( wasteservicestatusgui, ctx_wasteservice_proto_ctx, "it.unibo.wasteservicestatusgui.Wasteservicestatusgui").
  qactor( wasteservice, ctx_wasteservice_proto_ctx, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice_proto_ctx, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice_proto_ctx, "it.unibo.storagemanager.Storagemanager").
  qactor( wastetruck, ctx_wasteservice_proto_ctx, "it.unibo.wastetruck.Wastetruck").
