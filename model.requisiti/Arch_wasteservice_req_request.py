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
with Diagram('wasteservice_req_requestArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxreq_request', graph_attr=nodeattr):
          wastetruck_req=Custom('wastetruck_req','./qakicons/symActorSmall.png')
          wasteservice_req=Custom('wasteservice_req','./qakicons/symActorSmall.png')
     wastetruck_req >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> wasteservice_req
diag
