%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
context(ctx_basicrobot, "robot",  "TCP", "8020").
 qactor( pathexec, ctx_basicrobot, "external").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice, "it.unibo.storagemanager.Storagemanager").
