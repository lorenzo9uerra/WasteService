%====================================================================================
% wasteservice_pro_deposit description   
%====================================================================================
context(ctxpro_deposit, "localhost",  "TCP", "8050").
 qactor( pro_dep_wasteservice, ctxpro_deposit, "it.unibo.pro_dep_wasteservice.Pro_dep_wasteservice").
  qactor( pro_dep_trolley, ctxpro_deposit, "it.unibo.pro_dep_trolley.Pro_dep_trolley").
  qactor( pro_dep_storagemanager, ctxpro_deposit, "it.unibo.pro_dep_storagemanager.Pro_dep_storagemanager").
  qactor( dep_init, ctxpro_deposit, "it.unibo.dep_init.Dep_init").
