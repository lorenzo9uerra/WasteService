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
          dep_trolley=Custom('dep_trolley','./qakicons/symActorSmall.png')
          dep_waste_boxes=Custom('dep_waste_boxes','./qakicons/symActorSmall.png')
          dep_init=Custom('dep_init','./qakicons/symActorSmall.png')
     dep_trolley >> Edge(color='blue', style='solid', xlabel='depositWaste') >> dep_waste_boxes
     dep_init >> Edge(color='blue', style='solid', xlabel='deposit') >> dep_trolley
diag
