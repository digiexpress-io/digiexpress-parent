import React from 'react'

import { FormattedMessage } from 'react-intl'
import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button, TextareaAutosize } from '@material-ui/core';

import { Hdes } from '../deps';
import { Dialog } from '../dialog';


const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {

    },
    button: {
      marginRight: theme.spacing(1),
    },
    dialog: {
      marginTop: theme.spacing(1),
    },
  }),
);


interface UploadCSVProps {
  onClose: () => void;
  onChange: (commands: Hdes.AstAPI.DtCommand[]) => void;
}

const UploadCSV: React.FC<UploadCSVProps> = ({ onChange, onClose }) => {
  const classes = useStyles();

  const [csv, setCsv] = React.useState('');

  const editor = (
    <TextareaAutosize minRows={10}
      style={{ width: '100%', height: '100%' }}
      value={csv}
      onChange={({ target }) => setCsv(target.value)}
    />
  );

  const handleSave = () => {
    const commands: Hdes.AstAPI.DtCommand[] = [];
    if (csv.trim().length > 0) {
      commands.push({ type: 'IMPORT_ORDERED_CSV', value: csv, id: '' });
    }
    if (commands.length > 0) {
      onChange(commands);
    }
    onClose();
  }

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);

  return (<Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={onClose}
    actions={actions}
    title={<span><FormattedMessage id='dt.edit.upload.csv.title' /></span>}
    open={true} />);
}

export type { UploadCSVProps };
export default UploadCSV;
