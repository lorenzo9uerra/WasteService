%====================================================================================
% pathexecstop description   
%====================================================================================
mqttBroker("broker.hivemq.com", "1883", "unibo/basicrobot").
context(ctx_pathexecstop, "localhost",  "TCP", "8025").
context(ctxbasicrobot, "127.0.0.1",  "TCP", "8020").
 qactor( basicrobot, ctxbasicrobot, "external").
  qactor( pathexecstop, ctx_pathexecstop, "it.unibo.pathexecstop.Pathexecstop").
  qactor( timer, ctx_pathexecstop, "it.unibo.timer.Timer").
