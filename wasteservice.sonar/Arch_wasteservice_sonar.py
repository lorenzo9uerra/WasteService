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
     with Cluster('ctx_raspberry_sonar', graph_attr=nodeattr):
          sonarshim=Custom('sonarshim(coded)','./qakicons/codedQActor.png')
     with Cluster('ctx_trolley', graph_attr=nodeattr):
          sonarinit=Custom('sonarinit','./qakicons/symActorSmall.png')
     sonarinit >> Edge(color='blue', style='solid', xlabel='sonarStart') >> sonarshim
diag
