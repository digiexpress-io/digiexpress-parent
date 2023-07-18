import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Box, Paper } from '@material-ui/core';

import { DebugOptions } from './DebugOptions';
import { DebugInputs } from './DebugInputs';
import { DebugOutputs } from './DebugOutputs';
import { DebugNav } from './DebugNav';
import { DebugErrors } from './DebugErrors';
import { DebugToolbar } from './DebugToolbar';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      height: '100%',
      overflow: 'auto',
      '& .MuiTextField-root': {
        padding: theme.spacing(1)
      },
    },
    toolbar: {
      flexGrow: 1,
      alignSelf: "center",
      padding: theme.spacing(1)
    },
  }),
);

const DebugExplorer: React.FC<{}> = () => {
  const classes = useStyles();

  return (
    <Paper className={classes.root}>
      <DebugNav />
      <Box display="flex" component="form" noValidate autoComplete="off">
        <Box width="50%"><DebugOptions /></Box>
        <Box className={classes.toolbar}><DebugToolbar /></Box>
      </Box>
      <DebugInputs />
      <DebugOutputs />
      <DebugErrors />
    </Paper>);
}

export { DebugExplorer };
