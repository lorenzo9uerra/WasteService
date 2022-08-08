%====================================================================================
% wasteservice_req_deposit description   
%====================================================================================
context(ctxreq_deposit, "localhost",  "TCP", "8050").
 qactor( depositinit, ctxreq_deposit, "it.unibo.depositinit.Depositinit").
  qactor( trolley_dep, ctxreq_deposit, "it.unibo.trolley_dep.Trolley_dep").
  qactor( waste_boxes, ctxreq_deposit, "it.unibo.waste_boxes.Waste_boxes").
