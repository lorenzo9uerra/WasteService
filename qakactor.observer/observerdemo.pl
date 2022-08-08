%====================================================================================
% observerdemo description   
%====================================================================================
context(ctx_observertest, "localhost",  "TCP", "9000").
 qactor( observable, ctx_observertest, "it.unibo.observable.Observable").
  qactor( observer, ctx_observertest, "it.unibo.observer.Observer").
