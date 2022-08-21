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
with Diagram('wasteservice_req_ledArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxpro_led', graph_attr=nodeattr):
          led_trolley=Custom('led_trolley','./qakicons/symActorSmall.png')
          led_ledcontroller=Custom('led_ledcontroller','./qakicons/symActorSmall.png')
          led_blinkled=Custom('led_blinkled','./qakicons/symActorSmall.png')
     led_ledcontroller >> Edge(color='blue', style='solid', xlabel='ledSet') >> led_blinkled
diag
