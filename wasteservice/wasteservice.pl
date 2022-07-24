%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_storagemanager, "localhost",  "TCP", "8021").
context(ctx_trolley, "localhost",  "TCP", "8022").
context(ctx_wasteservice, "localhost",  "TCP", "8023").
 qactor( storagemanager, ctx_storagemanager, "it.unibo.storagemanager.Storagemanager").
  qactor( trolley, ctx_trolley, "it.unibo.trolley.Trolley").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
