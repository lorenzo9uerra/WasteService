%====================================================================================
% pathexecstop description   
%====================================================================================
context(ctx_pathexecstop, "localhost",  "TCP", "8025").
context(ctxbasicrobot, "robot",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( pathexecstop, ctx_pathexecstop, "it.unibo.pathexecstop.Pathexecstop").
  qactor( timer, ctx_pathexecstop, "it.unibo.timer.Timer").
