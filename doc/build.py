from typing import Iterable, List
from xml.etree import ElementTree
import mistune
from mistune.directives import DirectiveToc
from mistune.util import escape_html
import os
from pygments import highlight
from pygments.lexers import get_lexer_by_name
from pygments.formatters import html
import urllib.parse as urlparse
import lxml.html
from lxml.html import HtmlElement
import lxml.etree as etree
from xml.dom import minidom
import re

abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)

def build_sprint_doc(title: str, files: "list[str]"):
    filename = title.lower().replace(' ', '_')
    outfile = f"pages/{filename}.md"
    with open(outfile, "w") as out:
        # 2 indici, uno usato per sidebar
        out.write(f"""# {title}

.. toc::
    :depth: 2

### Indice

.. toc::
    :depth: 3

"""
        )
        for fname in files:
            with open(fname, encoding='utf-8') as infile:
                out.writelines(infile)
            out.write('\n')
    return outfile

def build_sprint_from_name(title: str):
    sprintid = title.lower().replace(' ', '')
    return build_sprint_doc(title, [
        "requisiti_committente.md",
        f"{sprintid}_requisiti.md",
        f"{sprintid}_analisi_problema.md",
        f"{sprintid}_progetto.md",
        f"{sprintid}_recap.md",
    ])

class DocRenderer(mistune.HTMLRenderer):
    def update_link(self, link):
        if bool(urlparse.urlparse(link).netloc):
            return link
        else:
            # Assume markdown links lead to other rendered pages
            if not re.search(r'\.md$', link):
                link = "../" + link
            if re.search(r'sprint(\d).*\.md', link):
                link = re.sub(r'sprint(\d).*\.md', "sprint_$1.md", link)

            link = re.sub(r'\.md$', '.html', link)
            return link

    def check_youtube_embed(self, link):
        if re.search(r'(?:youtube\.com/watch|youtu\.be)', link):
            embedlink = re.sub(r'youtube.com/watch/?v=(\w+)', f'youtube.com/embed/\g<1>', link)
            embedlink = re.sub(r'youtu.be/(\w+)', f'youtube.com/embed/\g<1>', link)
            return f"""

<br><iframe width="560" height="315" src="{embedlink}" title="YouTube video player" frameborder="0" 
allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen>
</iframe>"""
        else:
            return ""

    def link(self, link, text=None, title=None):
        if text is None:
            text = link

        s = '<a href="' + self._safe_url(self.update_link(link)) + '"'
        if title:
            s += ' title="' + escape_html(title) + '"'
        return s + '>' + (text or link) + '</a>' + self.check_youtube_embed(link)

    def image(self, src, alt="", title=None):
        src = self._safe_url(self.update_link(src))
        alt = escape_html(alt)
        s = '<img src="' + src + '" alt="' + alt + '"'
        if title:
            s += ' title="' + escape_html(title) + '"'
        return s + ' />'

    # Attualmente rompe i linguaggi riconosciuti
    # highlight code
    # def block_code(self, code, lang=None):
    #     if lang:
    #         if lang=="Qak":
    #             # highlight qak?
    #             return '<pre><code>' + mistune.escape(code) + '</code></pre>'
    #         else:
    #             lexer = get_lexer_by_name(lang, stripall=True)
    #             formatter = html.HtmlFormatter()
    #             return highlight(code, lexer, formatter)
    #     return '<pre><code>' + mistune.escape(code) + '</code></pre>'

def selcss(el: HtmlElement, css: str) -> "List[HtmlElement]":
    return el.cssselect(css)

def buildhtml(html: HtmlElement):
    for element in selcss(html, "h1"):
        element.classes.add('w3-container')
        element.classes.add('w3-indigo')
    for element in selcss(html, "h2"):
        element.classes.add('w3-container')
        element.classes.add('w3-teal')
    for element in selcss(html, "h3"):
        element.classes.add('w3-container')
        element.classes.add('w3-green')
    for element in selcss(html, "pre code"):
        div = etree.Element('div')
        div.set("class", "w3-code")
        div.text = element.text
        parent: HtmlElement = element.getparent()
        parent.append(div)
        parent.remove(element)

    # Indice
    tocsel = selcss(html, "section.toc")
            
    toindex = etree.Element('div')
    toindex.set("class", "sidebar-index w3-deep-purple")
    toindex_link = etree.Element('a')
    toindex_link.set("href", "./index.html")
    toindex_link.text = "Indice generale"
    toindex.append(toindex_link)

    if len(tocsel) > 0:
        toc = tocsel[0]
        tocelements = selcss(toc, "a")
        toc.clear()
        toc.classes.add("toc-sidebar")

        toc.append(toindex)

        for element in tocelements:
            div = etree.Element('div')
            div.append(element)
            toc.append(div)
    else:
        blanksidebar = etree.Element("section")
        blanksidebar.set("class", "toc toc-sidebar")
        blanksidebar.append(toindex)
        html.append(blanksidebar)

markdown = mistune.create_markdown(
    renderer=DocRenderer(),
    plugins=[
        DirectiveToc(),
    ],
)

def rendermarkdown(file):
    print(f"Rendering {file}...")
    outname = os.path.basename(file).replace(".md", ".html")
    with open(f'html/{outname}', 'w') as out:
        out.write("""<head>
        <link rel="stylesheet" href="../css/w3.css"> 
        <link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css"> 
        <!-- <link rel="stylesheet" href="../css/doc.css"> -->
</head>"""
        )
        out.write("<body>\n")
        out.write("<style>\n")
        with open("css/doc.css") as cssf:
            out.writelines(cssf)
        out.write("</style>\n")
        with open(file) as infile:
            md_content = infile.read()
            html_rendered = markdown(md_content)
            html_data = lxml.html.fromstring(html_rendered)
            buildhtml(html_data)
            tree = ElementTree.ElementTree(html_data)
            ElementTree.indent(tree, space="  ", level=0)
            tree.write(out, encoding='unicode', method='html')
        out.write("</body>\n")


sprints = [
    "Sprint 1",
    "Sprint 2",
    "Sprint 3",
]
sprintfiles = [
    build_sprint_from_name(sprint) for sprint in sprints
]

rendermarkdown("intro.md")
rendermarkdown("conclusioni.md")
rendermarkdown("index.md")

for file in sprintfiles:
    rendermarkdown(file)