%====================================================================================
% wasteservice_sonar description   
%====================================================================================
context(ctx_raspberry_sonar, "localhost",  "TCP", "8031").
context(ctx_trolley, "host.sonarinterrupter",  "TCP", "8023").
 qactor( sonarshim, ctx_raspberry_sonar, "it.unibo.lenziguerra.wasteservice.sonar.SonarShim").
  qactor( sonarinit, ctx_raspberry_sonar, "it.unibo.sonarinit.Sonarinit").
