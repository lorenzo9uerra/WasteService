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
with Diagram('wasteserviceArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctx_wasteservice', graph_attr=nodeattr):
          wasteservice=Custom('wasteservice','./qakicons/symActorSmall.png')
          trolley=Custom('trolley','./qakicons/symActorSmall.png')
          storagemanager=Custom('storagemanager','./qakicons/symActorSmall.png')
          sonar_interrupter=Custom('sonar_interrupter','./qakicons/symActorSmall.png')
          sonar_shim=Custom('sonar_shim(coded)','./qakicons/codedQActor.png')
     with Cluster('ctx_basicrobot', graph_attr=nodeattr):
          pathexec=Custom('pathexec(ext)','./qakicons/externalQActor.png')
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyMove') >> trolley
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyCollect') >> trolley
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyDeposit') >> trolley
     wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyRotate') >> trolley
     trolley >> Edge(color='magenta', style='solid', xlabel='dopath') >> pathexec
     trolley >> Edge(color='blue', style='solid', xlabel='storageDeposit') >> storagemanager
     sys >> Edge(color='red', style='dashed', xlabel='sonarStop') >> sonar_interrupter
     sys >> Edge(color='red', style='dashed', xlabel='sonarResume') >> sonar_interrupter
     sonar_interrupter >> Edge(color='blue', style='solid', xlabel='trolleyStop') >> trolley
     sonar_interrupter >> Edge(color='blue', style='solid', xlabel='trolleyResume') >> trolley
diag
