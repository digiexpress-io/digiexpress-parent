import React from 'react';
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme } from '@material-ui/core';

import { Vis, Hdes } from '../deps';
import GraphAPI from './GraphAPI';


const useStyles = makeStyles((_theme: Theme) =>
  createStyles({
    root: {
      height: '100%',
      backgroundColor: 'floralwhite'
    },
  }),
);


const options = {
  layout: {
    //      hierarchical: {
    //        enabled: true,
    //        levelSeparation: 100,
    //        parentCentralization: true,
    //        direction: 'UD',
    //        sortMethod: 'directed',
    //        nodeSpacing: 250,
    //        treeSpacing: 200,
    //      }
  },
  physics: {
    enabled: false
  },
  nodes: {
    shape: 'box',
    margin: 10,
    widthConstraint: {
      maximum: 200
    },
    shadow: {
      enabled: true, size: 10, x: 5, y: 5,
      color: 'rgba(0,0,0,0.5)',
    }
  },
  edges: {
    //color: '#fff',
    font: {
      size: 12
    },
    widthConstraint: {
      maximum: 90
    }
  }
}

interface GraphTreeProps {
  children: Hdes.AstAPI.Fl;
  models: Hdes.ModelAPI.Models;
  onClick: (id: string) => void;
  onDoubleClick: (id: string) => void;
};

const GraphTree: React.FC<GraphTreeProps> = ({ children, onClick, onDoubleClick, models }) => {
  const classes = useStyles();
  return (<div className={classes.root}>{
    Vis.create({
        events: { onClick, onDoubleClick },
        options: options,
        model: GraphAPI.create({ fl: children, models }),
      })
  }</div>);
}

export type { GraphTreeProps };
export { GraphTree };
