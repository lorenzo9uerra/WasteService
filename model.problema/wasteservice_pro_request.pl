%====================================================================================
% wasteservice_pro_request description   
%====================================================================================
context(ctxpro_request, "localhost",  "TCP", "8050").
 qactor( pro_req_wasteservice, ctxpro_request, "it.unibo.pro_req_wasteservice.Pro_req_wasteservice").
  qactor( pro_req_storagemanager, ctxpro_request, "it.unibo.pro_req_storagemanager.Pro_req_storagemanager").
  qactor( pro_req_wastetruck, ctxpro_request, "it.unibo.pro_req_wastetruck.Pro_req_wastetruck").
