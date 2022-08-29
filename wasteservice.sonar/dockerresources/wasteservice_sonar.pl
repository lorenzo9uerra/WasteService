%====================================================================================
% wasteservice_sonar description   
%====================================================================================
context(ctx_raspberry_sonar, "localhost",  "TCP", "8031").
context(ctx_wasteservice, "wasteservice.core",  "TCP", "8023").
 qactor( sonar_shim, ctx_raspberry_sonar, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
  qactor( sonar_init, ctx_raspberry_sonar, "it.unibo.sonar_init.Sonar_init").
