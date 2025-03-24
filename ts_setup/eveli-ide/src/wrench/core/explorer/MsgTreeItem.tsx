import React from 'react';
import {  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

import * as Burger from '@/burger';
import { HdesApi } from '@/burger';
import { FormattedMessage } from 'react-intl';



const MsgTreeItem: React.FC<{
  nodeId: string;
  msg: HdesApi.ProgramMessage;
  error?: boolean;
  children: React.ReactNode
}> = (props) => {
  const [open, setOpen] = React.useState(false);
  const onClose = () => setOpen(false);
  return (
    <>
      {open ? (
      <Dialog open={true} onClose={onClose}>
        <DialogTitle><FormattedMessage id={`programs.${props.error ? "error" : "warning"}.title`} /></DialogTitle>
        <DialogContent>
          <b>{props.msg.id}</b><br />
          {props.msg.msg}
        </DialogContent>
        <DialogActions>
          <Button variant='text' onClick={onClose}>
            <FormattedMessage id='button.cancel'/>
          </Button>
        </DialogActions>
      </Dialog>) : undefined}

      <Burger.TreeItemRoot
        itemId={props.nodeId}
        label={props.children}
        onClick={() => setOpen(true)}
      />
    </>);
}


export default MsgTreeItem;
