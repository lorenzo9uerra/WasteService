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
with Diagram('wasteservice_req_guiArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxreq_gui', graph_attr=nodeattr):
          wasteservicestatusgui_gui=Custom('wasteservicestatusgui_gui','./qakicons/symActorSmall.png')
          trolley_gui=Custom('trolley_gui','./qakicons/symActorSmall.png')
          blinkled_gui=Custom('blinkled_gui','./qakicons/symActorSmall.png')
     sys >> Edge(color='red', style='dashed', xlabel='ledStatus') >> wasteservicestatusgui_gui
     sys >> Edge(color='red', style='dashed', xlabel='trolleyStatus') >> wasteservicestatusgui_gui
     trolley_gui >> Edge( xlabel='trolleyStatus', **eventedgeattr) >> sys
     blinkled_gui >> Edge( xlabel='ledStatus', **eventedgeattr) >> sys
diag
