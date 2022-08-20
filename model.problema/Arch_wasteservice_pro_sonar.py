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
with Diagram('wasteservice_pro_sonarArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxpro_sonar', graph_attr=nodeattr):
          sonar_sonar=Custom('sonar_sonar','./qakicons/symActorSmall.png')
          sonar_controller=Custom('sonar_controller','./qakicons/symActorSmall.png')
          trolley=Custom('trolley','./qakicons/symActorSmall.png')
     sonar_sonar >> Edge( xlabel='sonarStop', **eventedgeattr) >> sys
     sonar_sonar >> Edge( xlabel='sonarResume', **eventedgeattr) >> sys
     sys >> Edge(color='red', style='dashed', xlabel='sonarStop') >> sonar_controller
     sys >> Edge(color='red', style='dashed', xlabel='sonarResume') >> sonar_controller
     sonar_controller >> Edge(color='blue', style='solid', xlabel='trolleyStop') >> trolley
     sonar_controller >> Edge(color='blue', style='solid', xlabel='trolleyResume') >> trolley
diag
