import React from 'react';
import { useTheme, useMediaQuery } from '@mui/material';

import ReactMarkdown from 'react-markdown'
import gfm from 'remark-gfm';

import Divider from '@mui/material/Divider';
import Renderers from './Renderers';

import { Links } from './Links';
import Portal from '../../';

//import { Links } from './Links';

interface MarkdownViewProps {
  children: string
}


const Markdown: React.FC<MarkdownViewProps> = ({ children }) => {
  const view = Portal.useTopic();
  const theme = useTheme();
  const small = useMediaQuery(theme.breakpoints.down("sm"));

  if (!view || !view.blob) {
    return <div>not selected ...</div>
  }

  const onAnchorClick: (anchor: string) => void = () => console.log("link clicked");
  const createAnchorRef = (_name: string): React.RefObject<HTMLSpanElement> => {
    const value: React.RefObject<HTMLSpanElement> = React.createRef();
    return value;
  };


  return (
    <>
      <ReactMarkdown
        children={children}
        remarkPlugins={[Renderers.ViewPlugin, gfm]}
        components={{
          image: Renderers.Image,
          h1: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '16pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": '25pt' }}>{props.children}</div>;
          },
          h2: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '13pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": "20pt" }}>{props.children}</div>;
          },
          h3: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '11pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": "18pt" }}>{props.children}</div>;
          },
          h4: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '9pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": "18pt" }}>{props.children}</div>;
          },
          h5: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '9pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": "18pt" }}>{props.children}</div>;
          },
          h6: (props) => {

            
            if (small) {
              return <div style={{ "fontSize": '9pt' }}>{props.children}</div>
            } return <div style={{ "fontSize": "18pt" }}>{props.children}</div>;
          },
          link: (props) => Renderers.Link(onAnchorClick, props),
          text: (props) => Renderers.Text(createAnchorRef, props)
        }} />
      { view.links.length ? (<><Divider /><Links /></>) : null}
    </>);
}

export { Markdown };