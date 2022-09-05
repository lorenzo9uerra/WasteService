%====================================================================================
% wasteservice_pro_sonar_stop description   
%====================================================================================
context(ctxpro_sonar_stop, "localhost",  "TCP", "8050").
 qactor( sonar_shim, ctxpro_sonar_stop, "it.unibo.sonar_shim.Sonar_shim").
  qactor( sonar_interrupter, ctxpro_sonar_stop, "it.unibo.sonar_interrupter.Sonar_interrupter").
  qactor( trolley, ctxpro_sonar_stop, "it.unibo.trolley.Trolley").
