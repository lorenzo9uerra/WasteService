%====================================================================================
% wasteservice_req_deposit description   
%====================================================================================
context(ctxreq_deposit, "localhost",  "TCP", "8050").
 qactor( dep_trolley, ctxreq_deposit, "it.unibo.dep_trolley.Dep_trolley").
  qactor( dep_waste_boxes, ctxreq_deposit, "it.unibo.dep_waste_boxes.Dep_waste_boxes").
  qactor( dep_init, ctxreq_deposit, "it.unibo.dep_init.Dep_init").
