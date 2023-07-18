import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, List, ListItem, ListItemText, Typography, Divider } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';


import { Layout } from '../deps';
import { Dialog } from '../dialog';
import Resource from '../';


type SaveDialogProps = {
  handleClose: () => void;
};

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    button: {
      marginRight: theme.spacing(1),
    },
    text: {
      paddingTop: theme.spacing(2),
    },
    errors: {
      backgroundColor: theme.palette.background.paper,
      width: "100%",
      overflow: "auto",
    },
    errorTitle: {
      paddingTop: '20px',
      paddingBottom: '20px',
    }
  }),
);


const SaveDialog: React.FC<SaveDialogProps> = ({ handleClose }) => {
  const classes = useStyles();
  const resource = Resource.useContext();
  const layout = Layout.useContext();

  const tabs = layout.session.tabs;
  const active = tabs[layout.session.history.open];
  if (!active) {
    throw new Error("failed to load save dialog because there is no session!");
  }
  const editor = resource.session.editor.getContent(active.id);
  if (!editor) {
    throw new Error("failed to load save dialog because there is no session!");
  }

  const content = (<>
    <div className={classes.text}>
      <div>
        <FormattedMessage id={"dialog.save.content"} />
      </div>
      { editor.errors.length === 0 ? null : <>
        <div className={classes.errorTitle}>
          <FormattedMessage id={"dialog.save.error.title"} />
        </div>
        <div className={classes.errors}>
          <List>
            {editor.errors.map((error) => (<>
              <ListItem>
                <ListItemText
                  primary={` — ${error.id}`}
                  secondary={<Typography sx={{ display: 'inline' }} component="span" variant="body2" color="text.error">{error.msg}</Typography>}
                />
              </ListItem>
              <Divider variant="inset" component="li" />
            </>))}
          </List>
        </div>
      </>}
    </div>
  </>);

  const handleSave = () => {
    if (editor.body) {
      resource.actions.saveAsset(editor.origin, editor.body);
    }
    handleClose();
  }

  const actions = (
    <>
      <Button color="secondary" className={classes.button} onClick={handleClose}>
        <FormattedMessage id={"dialog.save.button.cancel"} />
      </Button>
      <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}>
        <FormattedMessage id={"dialog.save.button.confirm"} />
      </Button>
    </>);

  return (<Dialog
    title={<FormattedMessage id={"dialog.save.title"} values={{ name: editor.origin.name }} />}
    open={true}
    onClose={handleClose}
    content={content}
    actions={actions}
  />);
}


export type { SaveDialogProps };
export { SaveDialog };


