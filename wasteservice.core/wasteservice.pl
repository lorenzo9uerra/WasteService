%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_wasteservice, "localhost",  "TCP", "8023").
context(ctx_basicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( pathexec, ctx_basicrobot, "external").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctx_wasteservice, "it.unibo.trolley.Trolley").
  qactor( storagemanager, ctx_wasteservice, "it.unibo.storagemanager.Storagemanager").
  qactor( sonar_shim, ctx_wasteservice, "it.unibo.sonar_shim.Sonar_shim").
  qactor( sonar_interrupter, ctx_wasteservice, "it.unibo.sonar_interrupter.Sonar_interrupter").
