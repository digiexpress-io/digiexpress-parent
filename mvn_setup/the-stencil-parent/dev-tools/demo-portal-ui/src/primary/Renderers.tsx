import React from 'react';
import { Plugin, Transformer } from "unified";

import { Node } from 'unist';
import visit from 'unist-util-visit';



interface TextNode extends Node {
  value: string;
  anchor?: string;
}


const ViewPlugin: Plugin = (options) => {
  const transformer: Transformer = (tree, _file) => {
    const anchors: TextNode[] = [];
    visit(tree, 'text', (node: TextNode) => {
      const start = node.value.indexOf("{#");
      if (start < 0) {
        return;
      }

      const end = node.value.indexOf("}", start);
      node.anchor = node.value.substring(start, end + 1);
      node.value = node.value.replace(node.anchor, "");
      anchors.push(node);
    })
    return tree;
  };

  return transformer;
}


const Text = (
  createAnchor: (name: string) => React.RefObject<HTMLSpanElement>,
  props: any) => {

  const textNode = props.node as TextNode;
  if (textNode.anchor) {
    const ref: React.RefObject<HTMLSpanElement> = createAnchor(textNode.anchor);
    return (<span ref={ref}>{props.value}</span>)
  }
  return props.value;
}

const Image = (props: any) => {
  return <img {...props}
    alt={props.src}
    src={`${process.env.PUBLIC_URL}/images/${props.src}`}
    style={{ maxWidth: '75%' }} />
}

const Link = (onClick: (anchor: string) => void, props: any) => {
  if (props.href.indexOf("#") === 0) {
    const path = props.href.substring(1);
    return <a href={`${process.env.PUBLIC_URL}/${path}`} onClick={(e) => {
      e.preventDefault();
      onClick(path);
    }
    }>{props.node.children[0].value}</a>
  }
  // eslint-disable-next-line
  return (<a {...props} />);
}

const Renderers = { Text, Image, Link, ViewPlugin };
export default Renderers;