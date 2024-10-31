import React from 'react';
import { Typography, useThemeProps } from '@mui/material';
import ReactMarkdown from 'react-markdown'
import { useUtilityClasses, GMarkdownRoot, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';



export interface GMarkdownProps {
  children: string | undefined;
  component?: GOverridableComponent<GMarkdownProps>;
}

export const GMarkdown: React.FC<GMarkdownProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const classes = useUtilityClasses();

  const components = React.useMemo(() => ({
    h1: delegate({ fontVariant: "h1" }),
    h2: delegate({ fontVariant: "h2" }),
    h3: delegate({ fontVariant: "h3" }),
    h4: delegate({ fontVariant: "h4" }),
    h5: delegate({ fontVariant: "h5" }),
    h6: delegate({ fontVariant: "body1" }),
    p: delegate({ fontVariant: "body1" }),
    ul: delegate({ fontVariant: "body1" })
  }), []);

  const Root = props.component ?? GMarkdownRoot;
  return (
    <Root ownerState={props} className={classes.root}>
      <ReactMarkdown 
        children={props.children ?? "no children to render"} components={components} />
    </Root>)
}

const delegate = (sx: {
  fontVariant: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'body1',

}) => (props: { children: React.ReactNode }) => {
  const variant = sx.fontVariant;
  return (<Typography variant={variant}>{props.children}</Typography>);
}
