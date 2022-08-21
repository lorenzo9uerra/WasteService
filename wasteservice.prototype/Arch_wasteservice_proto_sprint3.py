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
with Diagram('wasteservice_proto_sprint3Arch', show=False, outformat='png', graph_attr=graphattr) as diag:
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
          sonar_interaction=Custom('sonar_interaction','./qakicons/symActorSmall.png')
          sonar_interrupt=Custom('sonar_interrupt','./qakicons/symActorSmall.png')
     ledcontroller >> Edge(color='blue', style='solid', xlabel='ledSet') >> blinkled
     wasteservice >> Edge(color='magenta', style='solid', xlabel='storageAsk') >> storagemanager
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyMove') >> trolley
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyCollect') >> trolley
     wasteservice >> Edge(color='blue', style='solid', xlabel='pickedUp') >> wastetruck
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyDeposit') >> trolley
     trolley >> Edge(color='blue', style='solid', xlabel='storageDeposit') >> storagemanager
     wastetruck >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> wasteservice
     sonar_interaction >> Edge( xlabel='sonarStop', **eventedgeattr) >> sys
     sonar_interaction >> Edge( xlabel='sonarResume', **eventedgeattr) >> sys
     sys >> Edge(color='red', style='dashed', xlabel='sonarStop') >> sonar_interrupt
     sys >> Edge(color='red', style='dashed', xlabel='sonarResume') >> sonar_interrupt
     sonar_interrupt >> Edge(color='blue', style='solid', xlabel='trolleyStop') >> trolley
     sonar_interrupt >> Edge(color='blue', style='solid', xlabel='trolleyResume') >> trolley
diag
