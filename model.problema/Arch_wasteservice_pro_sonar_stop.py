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
with Diagram('wasteservice_pro_sonar_stopArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxpro_sonar_stop', graph_attr=nodeattr):
          sonar_shim=Custom('sonar_shim','./qakicons/symActorSmall.png')
          sonar_interrupter=Custom('sonar_interrupter','./qakicons/symActorSmall.png')
          trolley=Custom('trolley','./qakicons/symActorSmall.png')
     sonar_shim >> Edge( xlabel='sonarDistance', **eventedgeattr) >> sys
     sys >> Edge(color='red', style='dashed', xlabel='sonarDistance') >> sonar_interrupter
     sonar_interrupter >> Edge(color='blue', style='solid', xlabel='trolleyStop') >> trolley
     sonar_interrupter >> Edge(color='blue', style='solid', xlabel='trolleyResume') >> trolley
diag
