%====================================================================================
% wasteservice_req_request description   
%====================================================================================
context(ctxreq_request, "localhost",  "TCP", "8050").
 qactor( req_wasteservice, ctxreq_request, "it.unibo.req_wasteservice.Req_wasteservice").
  qactor( req_wastetruck, ctxreq_request, "it.unibo.req_wastetruck.Req_wastetruck").
