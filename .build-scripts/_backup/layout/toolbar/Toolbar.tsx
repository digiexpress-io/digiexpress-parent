import React from 'react';

import { createStyles, makeStyles, withStyles } from '@material-ui/styles';
import { Theme, List, ListItem, Tooltip, IconButton, Badge, Avatar, Box } from '@material-ui/core';

import { FormattedMessage } from 'react-intl';

import ToolbarAPI from './ToolbarAPI';
import { useLayout, Session } from '../context';


const useStyles = makeStyles((theme: Theme) => ({
  root: {
    height: '100%',
    display: 'flex',
    overflow: "hidden"
  },
  item: {
    width: '100%'
  },
  toolbar: {
    width: theme.spacing(7),
  },
  view: {
    flexGrow: 1
  }
}));


const SmallAvatar = withStyles((theme: Theme) =>
  createStyles({
    root: {
      width: "20px",
      height: "20px",
      fontSize: "unset",
      //color: theme.palette.primary.main,
      //background: "transparent",
      border: `2px solid ${theme.palette.background.paper}`,
    },
  }),
)(Avatar);


interface ToolbarProps {
  drawer: {
    open: boolean;
    onOpen: () => void;
  };
  children: Session.ToolbarItem[];
};

const Toolbar: React.FC<ToolbarProps> = ({ children, drawer }) => {
  const classes = useStyles();
  const { session, actions } = useLayout();
  const [active, setActive] = React.useState<string | undefined>();
  const [view, setView] = React.useState<React.ReactNode | undefined>();

  const activeView = { id: active, view };
  const links = ToolbarAPI.create({ session, actions, values: children });

  React.useEffect(() => {
    if (!session.linkId) {
      return;
    }
    const link = links.find(session.linkId);
    if (!link) {
      return;
    }

    const alreadyOpen = session.linkId === active;
    if (alreadyOpen) {
      return;
    }
    setActive(session.linkId);
    const type: any = link.type(link.id);
    if (!type.page) {
      return;
    }
    const linkView = type.page();
    if (linkView) {
      setView(linkView);
      drawer.onOpen();
    }

  }, [active, setView, setActive, session, children, drawer, links])

  const buttons = children.map((item, index) => (
    <Tooltip title={<FormattedMessage id={item.id} />} key={index}>
      <ListItem disableGutters>
        <Box display="flex" justifyContent="center" className={classes.item}>
          <IconButton
            disabled={item.enabled === false}
            color={links.color(item, activeView)}
            onClick={() => links.handle(item, activeView)}>

            {item.badge ?
              (<Badge
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                badgeContent={<SmallAvatar>{item.badge.text}</SmallAvatar>}>{item.icon}</Badge>) :
              (item.icon)
            }
          </IconButton>
        </Box>
      </ListItem>
    </Tooltip>)
  );

  let dialog: React.ReactNode;
  if (session.dialogId) {
    const type = (links.find(session.dialogId)?.type(session.dialogId) as { dialog: (onClose: () => void) => React.ReactNode });
    dialog = type.dialog(() => actions.handleDialog());
  } else {
    dialog = (<></>)
  }

  return (
    <div className={classes.root}>
      { dialog}
      <div className={classes.toolbar}>
        <List dense={true} disablePadding className={classes.toolbar}>{buttons}</List>
      </div>
      <div className={classes.view}>{drawer.open ? view : null}</div>
    </div>);
}

export default Toolbar;
