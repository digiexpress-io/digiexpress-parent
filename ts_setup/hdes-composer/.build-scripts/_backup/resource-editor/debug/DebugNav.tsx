import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Breadcrumbs, Link, Typography } from '@material-ui/core';

import Resource from '../';
import { Hdes } from '../deps';
import { useContext, Session } from './context';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      padding: theme.spacing(1),
      color: theme.palette.primary.main
    },
  }),
);


const DebugNav: React.FC<{}> = () => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const context = useContext();

  const getModel = (debug: Session.DebugModel): Hdes.ModelAPI.Model | undefined => {
    try {
      return resource.session.getModel(debug.model);
    } catch (e) {
      return undefined;
    }
  }

  return (<Breadcrumbs separator="-" className={classes.root}>
    {
      context.session.models
        .map(m => getModel(m)).filter(m => m)
        .map(m => m as Hdes.ModelAPI.Model)
        .map((m, index) => {
          if (m.id !== context.session.active) {
            return (<Link key={index} underline="hover" href="_blank" onClick={(event: any) => {
              event.preventDefault();
              context.actions.handleSetModel(m);
            }}>
              {m.name}
            </Link>);
          }
          return (<Typography key={index} color="text.primary">{m.name}</Typography>);
        })
    }
    {
      context.session.models.length ? null : (<Typography>&nbsp;</Typography>)
    }
  </Breadcrumbs>);
}

export { DebugNav };
