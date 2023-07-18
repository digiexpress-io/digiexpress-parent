import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, List, ListItem, ListItemText, Typography, Divider } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';


import { Hdes } from '../deps';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    errors: {
      backgroundColor: theme.palette.background.paper,
      width: "100%",
      overflow: "auto",
    },
    errorTitle: {
      color: theme.palette.error.main,
      paddingTop: theme.spacing(1),
      paddingBottom: theme.spacing(1),
    }
  }),
);

const ErrorList: React.FC<{children?: Hdes.StoreError}> = ({ children }) => {
  const classes = useStyles();
  if(!children || !children.errors) {
    return null;
  }

  return children.errors.length === 0 ? null : (<>
      <div className={classes.errorTitle}>
        <FormattedMessage id={"error.list.title"} />
      </div>
      <div className={classes.errors}>
        <List disablePadding dense>
          {children.errors.map((error) => (<>
            <ListItem>
              <ListItemText
                primary={` — ${error.id}`}
                secondary={<Typography sx={{ display: 'inline' }} component="span" variant="body2" color="text.error">{error.value}</Typography>}
              />
            </ListItem>
            <Divider variant="inset" component="li" />
          </>))}
        </List>
      </div>
    </>)
  ;
}

export { ErrorList };


