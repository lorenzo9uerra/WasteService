%====================================================================================
% wasteservice description   
%====================================================================================
context(ctx_trolley, "127.0.0.1",  "TCP", "8022").
context(ctx_wasteservice, "localhost",  "TCP", "8023").
 qactor( trolley, ctx_trolley, "external").
  qactor( wasteservice, ctx_wasteservice, "it.unibo.wasteservice.Wasteservice").
