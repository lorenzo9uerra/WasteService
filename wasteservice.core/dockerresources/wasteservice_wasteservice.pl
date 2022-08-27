%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
context(ctx_trolley, "wasteservice.trolley",  "TCP", "8070").
context(ctx_storagemanager, "wasteservice.storage",  "TCP", "8071").
context(ctx_pathexecstop, "unibo.pathexecstop",  "TCP", "8025").
 qactor( pathexecstop, ctx_pathexecstop, "external").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_trolley, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_storagemanager, "it.unibo.storagemanager.Storagemanager").
  qactor( sonarinterrupter, ctx_trolley, "it.unibo.sonarinterrupter.Sonarinterrupter").
