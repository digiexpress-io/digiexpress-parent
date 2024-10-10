import React from 'react'

import * as d3 from 'd3';

import { StencilClient } from '../context';

const width = 600;
const radius = width / 2;
//viewBox="the points "seen" in this SVG drawing area. 4 values separated by white space or commas. (min x, min y, width, height)"
const viewBox = `${-width / 2},  ${-width / 2}, ${width}, ${width}`;
const colornone = "#ccc"
const colorin = "#00f";
const colorout = "#f00";


function bilink(root) {


  const map = new Map(root.leaves().map(d => [id(d), d]));
  console.log(map);


  for (const d of root.leaves()) {
    d.incoming = [];
    d.outgoing = d.data.imports.map(i => [d, map.get(i)]);

  };

  for (const d of root.leaves()) {
    for (const o of d.outgoing) {
      o[1].incoming.push(o);
    }
  };
  return root;
}

function id(node) {
  return `${node.parent ? id(node.parent) + "." : ""}${node.data.name}`;
}

interface ReleaseNode {
  name: string;
  type: "article" | "link" | "release" | "workflow" | "locale" |
  "root" | "workflows" | "links" | "articles" | "locales" | "releases";

  imports: string[];
  release?: StencilClient.Release;
  article?: StencilClient.ArticleReleaseItem;
  locale?: StencilClient.LocaleReleaseItem;
  link?: StencilClient.LinkReleaseItem;
  workflow?: StencilClient.WorkflowReleaseItem;
  children: ReleaseNode[];
}

class ReleaseGraphVisitor {
  private _ref: React.RefObject<HTMLDivElement>;
  private _site: StencilClient.Site;

  private _workflows: ReleaseNode = { type: "workflows", name: "workflows", children: [], imports: [] };
  private _links: ReleaseNode = { type: "links", name: "links", children: [], imports: [] };
  private _articles: ReleaseNode = { type: "articles", name: "articles", children: [], imports: [] };
  private _releases: ReleaseNode = { type: "releases", name: "releases", children: [], imports: [] };
  private _locales: ReleaseNode = { type: "locales", name: "locales", children: [], imports: [] };
  private _svgNode: any;

  private _data: ReleaseNode = {
    type: "root", name: "graph", imports: [], children: [
      this._workflows,

      //this._links,
      this._locales,

      this._articles,
      this._releases,
    ]
  };
  private _visited: string[] = []
  constructor(props: {
    ref: React.RefObject<HTMLDivElement>;
    site: StencilClient.Site;
  }) {
    this._ref = props.ref;
    this._site = props.site;
  }

  visitRelease(release: StencilClient.Release) {
    if (this._visited.includes(release.id)) {
      return;
    }
    this._visited.push(release.id);
    const releaseNode: ReleaseNode = { name: release.body.name, children: [], type: "release", release, imports: [] };

    release.body.locales.forEach(locale => this.visitLocale(locale, release, releaseNode))
    release.body.articles.forEach(article => this.visitArticle(article, release, releaseNode))
    release.body.links.forEach(link => this.visitLink(link, release, releaseNode))
    release.body.workflows.forEach(workflow => this.visitWorkflow(workflow, release, releaseNode))

    this._data.children.push(releaseNode);
  }
  visitArticle(article: StencilClient.ArticleReleaseItem, release: StencilClient.Release, parent: ReleaseNode) {
    const name = article.name;
    parent.imports.push(this._data.name + "." + this._articles.name + "." + name);
    if (this._visited.includes(article.id)) {
      return;
    }
    this._visited.push(article.id);
    this._articles.children.push({ name, children: [], type: "article", article, imports: [] })

  }
  visitLocale(locale: StencilClient.LocaleReleaseItem, release: StencilClient.Release, parent: ReleaseNode) {
    const name = locale.value;
    parent.imports.push(this._data.name + "." + this._locales.name + "." + name);
    if (this._visited.includes(locale.id)) {
      return;
    }
    this._visited.push(locale.id);
    this._locales.children.push({ name, children: [], type: "locale", locale, imports: [] })

  }
  visitLink(link: StencilClient.LinkReleaseItem, release: StencilClient.Release, parent: ReleaseNode) {
    const name = link.value;
    //parent.imports.push(this._data.name + "." + this._links.name + "." + name);
    if (this._visited.includes(link.id)) {
      return;
    }
    this._visited.push(link.id);
    this._links.children.push({ name, children: [], type: "link", link, imports: [] })

  }
  visitWorkflow(workflow: StencilClient.WorkflowReleaseItem, release: StencilClient.Release, parent: ReleaseNode) {
    const name = workflow.value;
    parent.imports.push(this._data.name + "." + this._workflows.name + "." + name);
    if (this._visited.includes(workflow.id)) {
      return;
    }
    this._visited.push(workflow.id);
    const node: ReleaseNode = { name: workflow.value, children: [], type: "workflow", workflow, imports: [] };
    this._workflows.children.push(node)
  }

  visit() {
    Object.values(this._site.releases).forEach(rel => this.visitRelease(rel))

    const svg = d3.create("svg").attr("viewBox", viewBox);
    const tree = d3.cluster().size([2 * Math.PI, radius - 150])
    const line = d3.lineRadial()
      .curve(d3.curveBundle.beta(0.85))
      .radius(d => (d as any).y)
      .angle(d => (d as any).x);



    const d3Format = d3.hierarchy(
      //flare
      this._data
    ).sort((a, b) => d3.ascending(a.height, b.height) || d3.ascending((a.data as any).name, (b.data as any).name));

    const root = tree(

      bilink(d3Format)
    );





    // data labels
    const node = svg.append("g")
      .attr("font-family", "sans-serif")
      .attr("font-size", 10)
      .selectAll("g")
      .data(root.leaves())
      .join("g")
      .attr("transform", (props0) => {
        return `rotate(${(props0 as any).x * 180 / Math.PI - 90}) translate(${(props0 as any).y},0)`;
      })

      .append("text")
      .attr("dy", "0.31em")
      .attr("x", d => (d as any).x < Math.PI ? 6 : -6)
      .attr("text-anchor", d => (d as any).x < Math.PI ? "start" : "end")
      .attr("transform", d => (d as any).x >= Math.PI ? "rotate(180)" : null)
      .text(d => (d as any).data.name)
      .each(function(d) { (d as any).text = this; })
      .on("mouseover", overed)
      .on("mouseout", outed)
      .call(text => text.append("title").text(d => {
        //console.log(d);
        return `${id(d)} ${(d as any).outgoing.length} outgoing ${(d as any).incoming.length} incoming`;
      }));


    const outgoing = (root.leaves() as any).flatMap(leaf => (leaf as any).outgoing) as any;
    const link = svg.append("g")
      .attr("stroke", colornone)
      .attr("fill", "none")
      .selectAll("path")
      .data(outgoing)
      .join("path")
      .style("mix-blend-mode", "multiply")
      .attr("d", (props: any) => {

        const i = props[0];
        const o = props[1];

        const path = i.path(o);
        const resp = line(path);
        return resp;
      })
      .each(function(d) { (d as any).path = this; });

    const that = this;

    function overed(event, d) {
      link.style("mix-blend-mode", null);
      //d3.select(that._svgNode).attr("font-weight", "bold");
      d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", colorin).raise();
      d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", colorin).attr("font-weight", "bold");
      d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", colorout).raise();
      d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", colorout).attr("font-weight", "bold");
    }

    function outed(event, d) {
      link.style("mix-blend-mode", "multiply");
      //d3.select(that._svgNode).attr("font-weight", null);
      d3.selectAll(d.incoming.map(d => d.path)).attr("stroke", null);
      d3.selectAll(d.incoming.map(([d]) => d.text)).attr("fill", null).attr("font-weight", null);
      d3.selectAll(d.outgoing.map(d => d.path)).attr("stroke", null);
      d3.selectAll(d.outgoing.map(([, d]) => d.text)).attr("fill", null).attr("font-weight", null);
    }
    const oldNodes = this._ref.current?.childNodes;
    if (oldNodes) {
      oldNodes.forEach(n => this._ref.current?.removeChild(n));
    }
    this._svgNode = svg.node() as any;
    this._ref.current?.append(this._svgNode);
  }
}

export { ReleaseGraphVisitor };