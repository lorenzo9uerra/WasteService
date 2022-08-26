%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
context(ctx_pathexecstop, "127.0.0.1",  "TCP", "8025").
 qactor( pathexecstop, ctx_pathexecstop, "external").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice, "it.unibo.storagemanager.Storagemanager").
  qactor( sonarinterrupter, ctx_wasteservice, "it.unibo.sonarinterrupter.Sonarinterrupter").
