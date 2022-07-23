%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_storagemanager, "localhost",  "TCP", "8021").
context(ctx_trolley, "localhost",  "TCP", "8022").
 qactor( storagemanager, ctx_storagemanager, "it.unibo.storagemanager.Storagemanager").
  qactor( trolley, ctx_trolley, "it.unibo.trolley.Trolley").
