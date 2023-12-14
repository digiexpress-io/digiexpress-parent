import React from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';


import { Dialog } from '../dialog';


type InfoDialogProps = {
  handleClose: () => void;
};

const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    button: {
      marginRight: theme.spacing(1),
    }
  }),
);


const InfoDialog: React.FC<InfoDialogProps> = ({ handleClose }) => {
  const classes = useStyles();

  //  const { service, session } = React.useContext(Resources.Context);
  //  const { users, projects } = session.data;
  // const [group, setGroup] = React.useState(service.groups.builder());
  //  const [activeStep, setActiveStep] = React.useState(0);
  //const handleFinish = () => service.groups.save(group).onSuccess(onClose);

  const content = (<>test</>);

  const actions = (
    <>
      <Button variant="contained" color="primary" className={classes.button}><FormattedMessage id={"dialog.info.button.ok"}/></Button>
    </>);

  return (<Dialog
    title={<FormattedMessage id={"dialog.info.title"}/>}
    open={true}
    onClose={handleClose}
    content={content}
    actions={actions}
  />);
}


export type { InfoDialogProps };
export { InfoDialog };


