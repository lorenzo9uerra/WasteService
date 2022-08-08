%====================================================================================
% wasteservice_proto_sprint1 description   
%====================================================================================
context(ctx_wasteservice_proto_ctx, "localhost",  "TCP", "8050").
 qactor( ledcontroller, ctx_wasteservice_proto_ctx, "it.unibo.ledcontroller.Ledcontroller").
  qactor( led, ctx_wasteservice_proto_ctx, "it.unibo.led.Led").
  qactor( gui, ctx_wasteservice_proto_ctx, "it.unibo.gui.Gui").
  qactor( wasteservice, ctx_wasteservice_proto_ctx, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice_proto_ctx, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice_proto_ctx, "it.unibo.storagemanager.Storagemanager").
  qactor( wastetruck, ctx_wasteservice_proto_ctx, "it.unibo.wastetruck.Wastetruck").
