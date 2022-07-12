%====================================================================================
% wasteservice_req_sonar_stop description   
%====================================================================================
context(ctxreq_sonar, "localhost",  "TCP", "8050").
 qactor( sonar_sonar, ctxreq_sonar, "it.unibo.sonar_sonar.Sonar_sonar").
  qactor( trolley_sonar, ctxreq_sonar, "it.unibo.trolley_sonar.Trolley_sonar").
