import React from 'react';
import { Typography, Box } from '@mui/material';
import { OpenInNew as OpenInNewIcon } from '@mui/icons-material';
import ReactMarkdown from 'react-markdown';

import { FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-primitives';
import { FsHelpProps, helpMarkdownMock } from './FsHelpProps';
import { useOwnerState } from './useOwnerState';
import { useUtilityClasses, FsHelpRoot } from './useUtilityClasses';


export const FsHelp: React.FC<FsHelpProps> = (props) => {
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  const linkRenderer = (props: any) => {
    return (
      <a href={props.href} target="_blank" rel="noreferrer" className={classes.externalLink}>
        {props.children}
        <OpenInNewIcon fontSize="small" />
      </a>
    );
  };

  return (
    <FsPanel title="Help" icon={<FsIcon icon={FsIcons.Help} large />} activeNode={true}>
      <FsHelpRoot className={classes.root} ownerState={ownerState}>
        <ReactMarkdown components={
          {
            h1: (props) => (<Typography className={classes.h1}>{props.children}</Typography>),
            h2: (props) => (<Typography className={classes.h2}>{props.children}</Typography>),
            h3: (props) => (<Typography className={classes.h3}>{props.children}</Typography>),
            h4: (props) => (<Typography className={classes.h4}>{props.children}</Typography>),
            h5: (props) => (<Typography className={classes.h5}>{props.children}</Typography>),
            h6: (props) => (<Typography className={classes.h6}>{props.children}</Typography>),
            p: (props) => (<Typography className={classes.paragraph}>{props.children}</Typography>),
            li: (props) => (<li className={classes.listItem}><Typography component="span" variant={'body1'}>{props.children}</Typography></li>),
            a: linkRenderer,
            code: (props) => (
              <Box component="code" className={classes.codeBlock}>
                <Box className={classes.codeContent}>
                  {props.children}
                </Box>
              </Box>
            ),
            ...(props.overrides ?? {})
          }}
        >
          {helpMarkdownMock}
        </ReactMarkdown>
      </FsHelpRoot>
    </FsPanel>
  );
};

