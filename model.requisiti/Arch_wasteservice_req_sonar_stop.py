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
with Diagram('wasteservice_req_sonar_stopArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxreq_sonar', graph_attr=nodeattr):
          sonar_sonar=Custom('sonar_sonar','./qakicons/symActorSmall.png')
          trolley_sonar=Custom('trolley_sonar','./qakicons/symActorSmall.png')
     sonar_sonar >> Edge( xlabel='sonarStop', **eventedgeattr) >> sys
     sonar_sonar >> Edge( xlabel='sonarResume', **eventedgeattr) >> sys
     sys >> Edge(color='red', style='dashed', xlabel='sonarStop') >> trolley_sonar
     sys >> Edge(color='red', style='dashed', xlabel='sonarResume') >> trolley_sonar
diag
