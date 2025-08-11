import React from 'react';
import {  Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';

import * as Burger from '@dxs-ts/eveli-primitives';
import { HdesApi } from '@dxs-ts/wrench-api';
import { FormattedMessage } from 'react-intl';
import { CancelButton } from '@dxs-ts/eveli-primitives';



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
          <CancelButton onClick={onClose} />
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
