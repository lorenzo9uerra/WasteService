%====================================================================================
% wasteservice_pro_sonar_stop description   
%====================================================================================
context(ctxpro_sonar_stop, "localhost",  "TCP", "8050").
 qactor( sonar_sonar, ctxpro_sonar_stop, "it.unibo.sonar_sonar.Sonar_sonar").
  qactor( sonar_controller, ctxpro_sonar_stop, "it.unibo.sonar_controller.Sonar_controller").
  qactor( trolley, ctxpro_sonar_stop, "it.unibo.trolley.Trolley").
