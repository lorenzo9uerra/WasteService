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
with Diagram('wasteservice_pro_depositArch', show=False, outformat='png', graph_attr=graphattr) as diag:
  with Cluster('env'):
     sys = Custom('','./qakicons/system.png')
     with Cluster('ctxpro_deposit', graph_attr=nodeattr):
          pro_dep_wasteservice=Custom('pro_dep_wasteservice','./qakicons/symActorSmall.png')
          pro_dep_trolley=Custom('pro_dep_trolley','./qakicons/symActorSmall.png')
          pro_dep_storagemanager=Custom('pro_dep_storagemanager','./qakicons/symActorSmall.png')
          dep_init=Custom('dep_init','./qakicons/symActorSmall.png')
     pro_dep_wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyMove') >> pro_dep_trolley
     pro_dep_wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyCollect') >> pro_dep_trolley
     pro_dep_wasteservice >> Edge(color='magenta', style='solid', xlabel='trolleyDeposit') >> pro_dep_trolley
     pro_dep_trolley >> Edge(color='blue', style='solid', xlabel='storageDeposit') >> pro_dep_storagemanager
     dep_init >> Edge(color='magenta', style='solid', xlabel='loadDeposit') >> pro_dep_wasteservice
diag
