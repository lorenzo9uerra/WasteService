from diagrams import Cluster, Diagram, Edge
from diagrams.custom import Custom
import os
os.environ['PATH'] += os.pathsep + 'C:/Program Files/Graphviz/bin/'

graphattr = {     #https://www.graphviz.org/doc/info/attrs.html
    'fontsize': '22',
}

nodeattr = {   
    'fontsize': '22',
    'bgcolor': 'lightyellow'
}

eventedgeattr = {
    'color': 'red',
    'style': 'dotted'
}
with Diagram('wasteservice_pro_requestArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxpro_request', graph_attr=nodeattr):
          pro_req_wasteservice=Custom('pro_req_wasteservice','./qakicons/symActorSmall.png')
          pro_req_storagemanager=Custom('pro_req_storagemanager','./qakicons/symActorSmall.png')
          pro_req_wastetruck=Custom('pro_req_wastetruck','./qakicons/symActorSmall.png')
     pro_req_wasteservice >> Edge(color='magenta', style='solid', xlabel='storageAsk') >> pro_req_storagemanager
     pro_req_wasteservice >> Edge(color='green', style='dashed', xlabel='loadrejected') >> sys 
     pro_req_wasteservice >> Edge(color='green', style='dashed', xlabel='loadaccept') >> sys 
     pro_req_wasteservice >> Edge(color='blue', style='solid', xlabel='pickedUp') >> pro_req_wastetruck
     pro_req_storagemanager >> Edge(color='green', style='dashed', xlabel='storageAt') >> sys 
     pro_req_wastetruck >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> pro_req_wasteservice
diag
