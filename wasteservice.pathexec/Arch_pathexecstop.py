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
with Diagram('pathexecstopArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctx_pathexecstop', graph_attr=nodeattr):
          pathexecstop=Custom('pathexecstop','./qakicons/symActorSmall.png')
          timer=Custom('timer','./qakicons/symActorSmall.png')
     with Cluster('ctxbasicrobot', graph_attr=nodeattr):
          basicrobot=Custom('basicrobot(ext)','./qakicons/externalQActor.png')
     pathexecstop >> Edge(color='blue', style='solid', xlabel='cmd') >> basicrobot
     pathexecstop >> Edge(color='magenta', style='solid', xlabel='setAlarm') >> timer
     pathexecstop >> Edge(color='magenta', style='solid', xlabel='step') >> basicrobot
     sys >> Edge(color='red', style='dashed', xlabel='alarm') >> pathexecstop
diag
