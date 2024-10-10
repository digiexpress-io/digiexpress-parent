import React from 'react';
import { Box, darken } from '@mui/material';
import ReactMarkdown from 'react-markdown'
import gfm from 'remark-gfm';

import Renderers from './Renderers';
import Portal from '@the-stencil-io/portal';

interface AppFooterProps {

}


export const Footer: React.FC<AppFooterProps> = () => {
  const site = Portal.useSite();
  const blob = site.getBlob();

  if (!site) {
    return null;
  }

  const onAnchorClick: (anchor: string) => void = () => console.log("link clicked");
  const createAnchorRef = (name: string): React.RefObject<HTMLSpanElement> => {
    const value: React.RefObject<HTMLSpanElement> = React.createRef();
    return value;
  };

  return (
    <Box
      sx={{
        p: 1,
        borderTop: '2px solid',
        borderColor: 'secondary.light',
        backgroundColor: 'secondary.dark',
        color: 'secondary.contrastText',
        width: '100%',
        position: 'fixed',
        bottom: 0,
      }}>

      <Box>
        Footer item 1
        </Box>
      <Box>
        Footer item 2
        </Box>

      <Box>
        Footer item 2
        </Box>


      {blob ? (
        <ReactMarkdown
          children={""}
          remarkPlugins={[Renderers.ViewPlugin, gfm]}
          components={{
            image: Renderers.Image,
            h1: (_props) => (<></>),
           // a: (props) => Renderers.Link(onAnchorClick, (form) => console.log(form), "footer", props),
            text: (props) => Renderers.Text(createAnchorRef, props)
          }} />
      ) : null}
    </Box>

  );
}
