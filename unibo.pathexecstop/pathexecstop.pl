%====================================================================================
% pathexecstop description   
%====================================================================================
context(ctx_pathexecstop, "localhost",  "TCP", "8025").
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( pathexecstop, ctx_pathexecstop, "it.unibo.pathexecstop.Pathexecstop").
