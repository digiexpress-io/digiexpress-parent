import React, { ReactElement } from 'react';

import { createStyles, makeStyles } from '@material-ui/styles';
import { Theme, Button } from '@material-ui/core';
import { FormattedMessage } from 'react-intl';

import { Hdes } from '../../deps';
import { Dialog } from '../../dialog';
import Builder from './../builders';

import {EditBoolean} from './';

import {EditString} from './';
import {EditStringSimple} from './';

import {EditNumber} from './';
import {EditNumberSimple} from './';

import {EditDateTime} from './';
import {EditDateTimeSimple} from './';

import {EditDate} from './';
import {EditDateSimple} from './';



const useStyles = makeStyles((theme: Theme) =>
  createStyles({
    root: {

    },
    button: {
      marginRight: theme.spacing(1),
    },
    dialog: {
      marginTop: theme.spacing(1),
      //marginLeft: theme.spacing(1),
      //marginRight: theme.spacing(1),
    },
  }),
);


interface CellEditProps {
  dt: Hdes.AstAPI.Dt,
  header: Hdes.AstAPI.DtHeader;
  cell: Hdes.AstAPI.DtCell;
  onClose: () => void;
  onChange: (commands: Hdes.AstAPI.DtCommand) => void
};

const CellEdit: React.FC<CellEditProps> = (props) => {
  const classes = useStyles();
  
  const [value, setValue] = React.useState<{value?: string, builder: any}>({
    value: props.cell.value,
    builder: Builder({ header: props.header, value: props.cell.value })  as any
  });
  const input = props.header.direction === 'IN'
  const type = props.header.value;
  

  const handleChangeValue = (value: string) => {
    const builder = Builder({ header: props.header, value })  as any;
    setValue({value, builder});
  }

  const handleSave = () => {
    const command: Hdes.AstAPI.DtCommand = { id: props.cell.id, value: value.value, type: 'SET_CELL_VALUE' };
    props.onChange(command);
    props.onClose();
  }

  let editor: ReactElement;
  if (type === 'STRING') {
    editor = input ?
      <EditString builder={value.builder} onChange={handleChangeValue} /> :
      <EditStringSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'INTEGER' || type === 'LONG' || type === 'DECIMAL') {
    editor = input ?
      <EditNumber builder={value.builder} onChange={handleChangeValue} /> :
      <EditNumberSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'DATE') {
    editor = input ?
      <EditDate builder={value.builder} onChange={handleChangeValue} /> :
      <EditDateSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'DATE_TIME') {
    editor = input ?
      <EditDateTime builder={value.builder} onChange={handleChangeValue} /> :
      <EditDateTimeSimple builder={value.builder} onChange={handleChangeValue} />

  } else if (type === 'BOOLEAN') {
    editor = <EditBoolean builder={value.builder as any} onChange={handleChangeValue} />
  } else {
    editor = (<></>);
  }

  const actions = (<>
    <Button color="secondary" className={classes.button} onClick={props.onClose}><FormattedMessage id={"dt.cell.edit.button.cancel"} /></Button>
    <Button variant="contained" color="primary" className={classes.button} onClick={handleSave}><FormattedMessage id={"dt.cell.edit.button.confirm"} /></Button>
  </>);

  const title = {
    name: props.dt.name,
    column: props.header.name,
    value: props.cell.value
  }
  return (<div className={classes.root}>
    <Dialog content={<div className={classes.dialog}>{editor}</div>} onClose={props.onClose}
      actions={actions}
      title={<span><FormattedMessage id='dt.cell.edit.title' values={title}/></span>}
      open={true} />
  </div>);
}

export type { CellEditProps };
export { CellEdit };
