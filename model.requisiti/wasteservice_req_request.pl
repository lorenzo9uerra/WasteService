%====================================================================================
% wasteservice_req_request description   
%====================================================================================
context(ctxreq_request, "localhost",  "TCP", "8050").
 qactor( wastetruck_req, ctxreq_request, "it.unibo.wastetruck_req.Wastetruck_req").
  qactor( wasteservice_req, ctxreq_request, "it.unibo.wasteservice_req.Wasteservice_req").
