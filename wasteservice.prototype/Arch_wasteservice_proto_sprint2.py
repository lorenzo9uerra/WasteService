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
with Diagram('wasteservice_proto_sprint2Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctx_wasteservice_proto_ctx', graph_attr=nodeattr):
          ledcontroller=Custom('ledcontroller','./qakicons/symActorSmall.png')
          blinkled=Custom('blinkled','./qakicons/symActorSmall.png')
          wasteservicestatusgui=Custom('wasteservicestatusgui','./qakicons/symActorSmall.png')
          wasteservice=Custom('wasteservice','./qakicons/symActorSmall.png')
          trolley=Custom('trolley','./qakicons/symActorSmall.png')
          storagemanager=Custom('storagemanager','./qakicons/symActorSmall.png')
          wastetruck=Custom('wastetruck','./qakicons/symActorSmall.png')
     ledcontroller >> Edge(color='blue', style='solid', xlabel='ledSet') >> blinkled
     wasteservice >> Edge(color='magenta', style='solid', xlabel='storageAsk') >> storagemanager
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadrejected') >> sys 
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyMove') >> trolley
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadaccept') >> sys 
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyCollect') >> trolley
     wasteservice >> Edge(color='blue', style='solid', xlabel='pickedUp') >> wastetruck
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyDeposit') >> trolley
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadrejected') >> sys 
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     trolley >> Edge(color='blue', style='solid', xlabel='storageDeposit') >> storagemanager
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     storagemanager >> Edge(color='green', style='dashed', xlabel='storageAt') >> sys 
     wastetruck >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> wasteservice
diag
