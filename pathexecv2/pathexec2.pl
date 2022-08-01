%====================================================================================
% pathexec2 description   
%====================================================================================
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
context(ctxpathexec2, "localhost",  "TCP", "8040").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( pathexec2, ctxpathexec2, "it.unibo.pathexec2.Pathexec2").
