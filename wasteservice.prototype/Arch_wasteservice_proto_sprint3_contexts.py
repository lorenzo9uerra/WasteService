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
with Diagram('wasteservice_proto_sprint3_contextsArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctx_wasteservice', graph_attr=nodeattr):
          wasteservice=Custom('wasteservice','./qakicons/symActorSmall.png')
     with Cluster('ctx_trolley', graph_attr=nodeattr):
          trolley=Custom('trolley','./qakicons/symActorSmall.png')
          sonarinterrupter=Custom('sonarinterrupter','./qakicons/symActorSmall.png')
     with Cluster('ctx_storagemanager', graph_attr=nodeattr):
          storagemanager=Custom('storagemanager','./qakicons/symActorSmall.png')
     with Cluster('ctx_raspberry', graph_attr=nodeattr):
          ledcontroller=Custom('ledcontroller','./qakicons/symActorSmall.png')
          blinkled=Custom('blinkled','./qakicons/symActorSmall.png')
          sonarshim=Custom('sonarshim','./qakicons/symActorSmall.png')
     with Cluster('ctx_statusgui', graph_attr=nodeattr):
          wasteservicestatusgui=Custom('wasteservicestatusgui','./qakicons/symActorSmall.png')
     with Cluster('ctx_driver', graph_attr=nodeattr):
          wastetruck=Custom('wastetruck','./qakicons/symActorSmall.png')
     with Cluster('ctx_pathexecstop', graph_attr=nodeattr):
          pathexecstop=Custom('pathexecstop','./qakicons/symActorSmall.png')
     with Cluster('ctx_basicrobot', graph_attr=nodeattr):
          basicrobot=Custom('basicrobot','./qakicons/symActorSmall.png')
     ledcontroller >> Edge(color='blue', style='solid', xlabel='ledSet') >> blinkled
     wasteservice >> Edge(color='magenta', style='solid', xlabel='storageAsk') >> storagemanager
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadrejected') >> sys 
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyMove') >> trolley
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadaccept') >> sys 
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyCollect') >> trolley
     wasteservice >> Edge(color='blue', style='solid', xlabel='pickedUp') >> wastetruck
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyDeposit') >> trolley
     wasteservice >> Edge(color='green', style='dashed', xlabel='loadrejected') >> sys 
     trolley >> Edge(color='magenta', style='solid', xlabel='dopath') >> pathexecstop
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     trolley >> Edge(color='blue', style='solid', xlabel='storageDeposit') >> storagemanager
     trolley >> Edge(color='green', style='dashed', xlabel='trolleyDone') >> sys 
     trolley >> Edge(color='blue', style='solid', xlabel='resumePath') >> pathexecstop
     trolley >> Edge(color='blue', style='solid', xlabel='stopPath') >> pathexecstop
     storagemanager >> Edge(color='green', style='dashed', xlabel='storageAt') >> sys 
     wastetruck >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> wasteservice
     pathexecstop >> Edge(color='magenta', style='solid', xlabel='step') >> basicrobot
     pathexecstop >> Edge(color='green', style='dashed', xlabel='dopathdone') >> sys 
     sonarshim >> Edge( xlabel='sonarDistance', **eventedgeattr) >> sys
     sys >> Edge(color='red', style='dashed', xlabel='sonarDistance') >> sonarinterrupter
     sonarinterrupter >> Edge(color='blue', style='solid', xlabel='trolleyStop') >> trolley
     sonarinterrupter >> Edge(color='blue', style='solid', xlabel='trolleyResume') >> trolley
diag
