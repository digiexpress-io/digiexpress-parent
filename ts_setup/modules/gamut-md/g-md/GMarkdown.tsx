import React from 'react';
import { Typography, useThemeProps } from '@mui/material';
import ReactMarkdown, { Components } from 'react-markdown';
import { useUtilityClasses, GMarkdownRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '@dxs-ts/gamut-api';



export interface GMarkdownProps {
  children: string | undefined;
  remarkPlugins?: any[] | undefined;
  overrides?: Components
  component?: GOverridableComponent<GMarkdownProps>;
}

export const GMarkdown: React.FC<GMarkdownProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();
  const Root = props.component ?? GMarkdownRoot;
  return (
    <Root ownerState={props} className={classes.root}>
      <ReactMarkdown
        remarkPlugins={props.remarkPlugins ?? undefined}
        children={props.children ?? "no children to render"} components={
          {
            h1: (props) => (<Typography variant={'h1'}>{props.children}</Typography>),
            h2: (props) => (<Typography variant={'h2'}>{props.children}</Typography>),
            h3: (props) => (<Typography variant={'h3'}>{props.children}</Typography>),
            h4: (props) => (<Typography variant={'h4'}>{props.children}</Typography>),
            h5: (props) => (<Typography variant={'h5'}>{props.children}</Typography>),
            h6: (props) => (<Typography variant={'body1'}>{props.children}</Typography>),
            p: (props) => (<Typography variant={'body1'}>{props.children}</Typography>),
            li: (props) => (<li><Typography variant={'body1'}>{props.children}</Typography></li>),
            a: linkRenderer,
            ...(props.overrides ?? {})
        }} />
    </Root>)
}

function linkRenderer(props: any) {
  return (
    <a href={props.href} target="_blank" rel="noreferrer">
      {props.children}
    </a>
  );
}
