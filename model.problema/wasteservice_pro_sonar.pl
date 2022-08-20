%====================================================================================
% wasteservice_pro_sonar description   
%====================================================================================
context(ctxpro_sonar, "localhost",  "TCP", "8050").
 qactor( sonar_sonar, ctxpro_sonar, "it.unibo.sonar_sonar.Sonar_sonar").
  qactor( sonar_controller, ctxpro_sonar, "it.unibo.sonar_controller.Sonar_controller").
  qactor( trolley, ctxpro_sonar, "it.unibo.trolley.Trolley").
