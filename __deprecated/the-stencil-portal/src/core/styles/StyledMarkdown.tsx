import React from 'react';
import ReactMarkdown from 'react-markdown'
import { Typography, SxProps, Theme } from '@mui/material';


const delegate = (sx: SxProps<Theme>) => (props: { children: React.ReactNode }) => (<Typography sx={sx}>{props.children}</Typography>);

interface StyledMarkdownProps {
  md?: string,
  notFound?: React.ReactNode,
  config: {
    h1: SxProps<Theme>,
    h2: SxProps<Theme>,
    h3: SxProps<Theme>,
    h4: SxProps<Theme>,
    h5: SxProps<Theme>,
    h6: SxProps<Theme>,
    p:  SxProps<Theme>,
    ul: SxProps<Theme>,
  }
}

const StyledMarkdown: React.FC<StyledMarkdownProps> = (props) => {
  const components = React.useMemo(() => ({
    h1: delegate(props.config.h1),
    h2: delegate(props.config.h2),
    h3: delegate(props.config.h3),
    h4: delegate(props.config.h4),
    h5: delegate(props.config.h5),
    h6: delegate(props.config.h6),
    p:  delegate(props.config.p),
    ul: delegate(props.config.ul)
  }), [
    props.config
  ]);
  return props.md ? (<ReactMarkdown children={props.md} components={components} />) : <>{props.notFound}</>;
}

export type { StyledMarkdownProps };
export { StyledMarkdown };

