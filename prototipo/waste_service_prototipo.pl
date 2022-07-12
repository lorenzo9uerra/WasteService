%====================================================================================
% waste_service_prototipo description   
%====================================================================================
context(ctxwasteservice, "localhost",  "TCP", "8050").
 qactor( camion, ctxwasteservice, "it.unibo.camion.Camion").
  qactor( wasteservice, ctxwasteservice, "it.unibo.wasteservice.Wasteservice").
  qactor( trolley, ctxwasteservice, "it.unibo.trolley.Trolley").
  qactor( storage_glass, ctxwasteservice, "it.unibo.storage_glass.Storage_glass").
  qactor( storage_paper, ctxwasteservice, "it.unibo.storage_paper.Storage_paper").
  qactor( gui, ctxwasteservice, "it.unibo.gui.Gui").
  qactor( led_actor, ctxwasteservice, "it.unibo.led_actor.Led_actor").
  qactor( sonar, ctxwasteservice, "it.unibo.sonar.Sonar").
