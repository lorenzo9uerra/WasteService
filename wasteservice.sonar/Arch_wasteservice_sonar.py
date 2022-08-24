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
with Diagram('wasteservice_sonarArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctx_wasteservice_sonar', graph_attr=nodeattr):
          sonar_init=Custom('sonar_init','./qakicons/symActorSmall.png')
          sonar_shim=Custom('sonar_shim(coded)','./qakicons/codedQActor.png')
     sonar_init >> Edge(color='blue', style='solid', xlabel='sonarStart') >> sonar_shim
diag
