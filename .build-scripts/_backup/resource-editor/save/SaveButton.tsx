import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme } from '@material-ui/core';
import SaveIcon from '@material-ui/icons/Save';

import { Layout } from '../deps';
import Resource from '../';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
     
    }
  }),
);


const SaveButton: React.FC<{}> = () => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const layout = Layout.useContext();
  
  const tabs = layout.session.tabs;
  const active = tabs[layout.session.history.open];
  
  if(!active) {
    return (<SaveIcon />);
  }
  
  const editor = resource.session.editor.getContent(active.id);
  if(!editor || editor.saved === true) {
    return (<SaveIcon />);
  }
  
  const errors = editor.errors;
  if(errors.length > 0) {
    return (<SaveIcon color="error" className={classes.root}/>);
  }
  
  return (
    <SaveIcon color="secondary" className={classes.root}/>
  );
}
export { SaveButton };


