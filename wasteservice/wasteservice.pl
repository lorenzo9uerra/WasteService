%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
 qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice, "it.unibo.storagemanager.Storagemanager").
