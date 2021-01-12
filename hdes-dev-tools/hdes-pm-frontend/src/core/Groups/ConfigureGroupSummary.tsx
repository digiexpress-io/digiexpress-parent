import React from 'react';

import { createStyles, makeStyles, Theme } from '@material-ui/core/styles';

import ListSubheader from '@material-ui/core/ListSubheader';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Collapse from '@material-ui/core/Collapse';
import Divider from '@material-ui/core/Divider';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';
import PersonOutlinedIcon from '@material-ui/icons/PersonOutlined';
import LibraryBooksOutlinedIcon from '@material-ui/icons/LibraryBooksOutlined';


import { Backend } from '.././Resources';

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      width: '100%',
      backgroundColor: theme.palette.background.paper,
    },
    nested: {
      paddingLeft: theme.spacing(4),
    },
  }),
);


interface ConfigureProjectSummaryProps {
  group: Backend.GroupBuilder;
  users: Backend.UserResource[];
  projects: Backend.ProjectResource[];
};

const ConfigureProjectSummary: React.FC<ConfigureProjectSummaryProps> = (props) => {
  const classes = useStyles();
  const [openUsers, setOpenUsers] = React.useState(true);
  const [openProjects, setOpenProjects] = React.useState(true);

  return (<div className={classes.root}>
    <List className={classes.root} component="nav" 
      aria-labelledby="nested-list-subheader"
      subheader={<ListSubheader component="div" id="nested-list-subheader">{`Group '${props.group.name}' summary`}</ListSubheader>}>

      <Divider />
      
      <ListItem button onClick={() => setOpenProjects(!openProjects)}>
        <ListItemText primary={`Projects to join: (${props.group.projects.length})`} />
        {openProjects ? <ExpandLess /> : <ExpandMore />}
      </ListItem>
      <Collapse in={openProjects} timeout="auto" unmountOnExit>
        <List component="div" disablePadding>
          {props.group.projects
            .map(id => props.projects.filter(p => p.project.id === id)[0].project)
            .map(p => <ListItem key={p.id} button className={classes.nested}><ListItemIcon><LibraryBooksOutlinedIcon /></ListItemIcon><ListItemText primary={p.name} /></ListItem>)}
        </List>
      </Collapse>

      <ListItem button onClick={() => setOpenUsers(!openUsers)}>
        <ListItemText primary={`Users to join: (${props.group.users.length})`} />
        {openUsers ? <ExpandLess /> : <ExpandMore />}
      </ListItem>
      <Collapse in={openUsers} timeout="auto" unmountOnExit>
        <List component="div" disablePadding>
          {props.group.users
            .map(id => props.users.filter(p => p.user.id === id)[0].user)
            .map(p => <ListItem key={p.id} button className={classes.nested}><ListItemIcon><PersonOutlinedIcon /></ListItemIcon><ListItemText primary={p.name} /></ListItem>)}
        </List>
      </Collapse>
      
    </List>
  </div>);
}

export default ConfigureProjectSummary;