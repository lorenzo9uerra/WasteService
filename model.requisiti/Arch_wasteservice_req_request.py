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
          req_wasteservice=Custom('req_wasteservice','./qakicons/symActorSmall.png')
          req_wastetruck=Custom('req_wastetruck','./qakicons/symActorSmall.png')
     req_wastetruck >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> req_wasteservice
diag
