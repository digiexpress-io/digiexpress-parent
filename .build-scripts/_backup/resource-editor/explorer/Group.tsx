import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, List, ListItem, ListItemText, Collapse, Typography } from '@material-ui/core';
import ExpandLess from '@material-ui/icons/ExpandLess';
import ExpandMore from '@material-ui/icons/ExpandMore';
import { FormattedMessage } from 'react-intl';
import moment from 'moment-timezone';

import { Hdes, Layout } from '../deps';
import ExplorerAPI from './ExplorerAPI';
import {openTab} from './Tab';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {
      paddingLeft: theme.spacing(1),
      paddingTop: 0,
      paddingBottom: 0,
    },
    nested: {
      paddingLeft: theme.spacing(2),
      paddingTop: 0,
      paddingBottom: 0,
    },
    nestedText: {
      marginTop: "3px",
      marginBottom: "3px",
    },
    error: {
      color: theme.palette.error.main
    },
    ok: {

    },
    small: {
      width: theme.spacing(3),
      height: theme.spacing(3),
    },
    timestamps: {
      display: "inline",
    }
  }),
);

interface GroupProps {
  id: string;
  type: Hdes.ModelAPI.ServiceType;
  label: boolean;
  models: Hdes.ModelAPI.Model[];
  modes: ExplorerAPI.FilterType[];
};

const Group: React.FC<GroupProps> = ({ id, models, modes, label, type }) => {
  const classes = useStyles();
  const { actions } = Layout.useContext();
  const [open, setOpen] = React.useState(false);
  const openAsset = (model: Hdes.ModelAPI.Model) => openTab(model, actions);

  let errors = false;
  const list: React.ReactElement[] = [];
  for (const model of models.sort((m0, m1) => m0.name.localeCompare(m1.name))) {
    let secondary = (<></>);
    if (modes.includes("TS")) {
      const created = moment(model.created).format('DD.MM.YYYY HH:mm:ss');
      const modified = moment(model.modified).format('DD.MM.YYYY HH:mm:ss');
      secondary = (
        <Typography component="span" variant="body2" color="text.secondary" className={classes.timestamps}>
          {created}<br />{modified}
        </Typography>
      );
    }

    if(model.errors.length > 0) {
      errors = true;   
    }
    list.push(
      <ListItem key={model.id} button className={classes.nested} onClick={() => openAsset(model)}>
        <ListItemText primary={<span className={model.errors.length > 0 ? classes.error : classes.ok}>{model.name}</span>} secondary={secondary} className={classes.nestedText} />
      </ListItem>);
  }

  if (!label) {
    return (<>{list}</>);
  }

  return (<div key={type}>
    <ListItem button className={classes.root} onClick={() => setOpen(!open)}>
      <ListItemText secondary={<span className={errors ? classes.error : classes.ok}><FormattedMessage id={id} />&nbsp;({models.length})</span>} />
      {open ? <ExpandLess /> : <ExpandMore />}
    </ListItem>
    <Collapse in={open} timeout="auto" unmountOnExit>
      <List component="div" disablePadding dense>{list}</List>
    </Collapse>
  </div>
  );
}

export type { GroupProps };
export default Group;
