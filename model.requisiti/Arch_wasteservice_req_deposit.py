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
with Diagram('wasteservice_req_depositArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxreq_deposit', graph_attr=nodeattr):
          depositinit=Custom('depositinit','./qakicons/symActorSmall.png')
          trolley_dep=Custom('trolley_dep','./qakicons/symActorSmall.png')
          waste_boxes=Custom('waste_boxes','./qakicons/symActorSmall.png')
     depositinit >> Edge(color='blue', style='solid', xlabel='testDeposit') >> trolley_dep
     trolley_dep >> Edge(color='blue', style='solid', xlabel='depositWaste') >> waste_boxes
diag
