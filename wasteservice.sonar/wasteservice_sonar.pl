%====================================================================================
% wasteservice_sonar description   
%====================================================================================
context(ctx_wasteservice_sonar, "localhost",  "TCP", "8031").
context(ctx_wasteservice, "host.trolley",  "TCP", "8023").
 qactor( sonar_shim, ctx_wasteservice_sonar, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
  qactor( sonar_init, ctx_wasteservice_sonar, "it.unibo.sonar_init.Sonar_init").