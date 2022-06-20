%====================================================================================
% waste_service_prototipo description   
%====================================================================================
context(ctxwasteservice, "localhost",  "TCP", "8050").
 qactor( camion, ctxwasteservice, "it.unibo.camion.Camion").
  qactor( requesthandler, ctxwasteservice, "it.unibo.requesthandler.Requesthandler").
  qactor( storage_glass, ctxwasteservice, "it.unibo.storage_glass.Storage_glass").
  qactor( storage_paper, ctxwasteservice, "it.unibo.storage_paper.Storage_paper").
  qactor( trolley, ctxwasteservice, "it.unibo.trolley.Trolley").
