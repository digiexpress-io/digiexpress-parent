import React from 'react';
import { DialogContentText } from '@mui/material';

import * as Burger from '@/burger';
import { Client } from '../context';



const MsgTreeItem: React.FC<{
  nodeId: string;
  msg: Client.ProgramMessage;
  error?: boolean;
  children: React.ReactNode
}> = (props) => {
  const [open, setOpen] = React.useState(false);

  return (
    <>
      {open ? (<Burger.Dialog open={true} onClose={() => setOpen(false)}
        backgroundColor="uiElements.main"
        title={`programs.${props.error ? "error" : "warning"}.title`}>
        <DialogContentText>
          <b>{props.msg.id}</b><br />
          {props.msg.msg}
        </DialogContentText>
      </Burger.Dialog>) : undefined}

      <Burger.TreeItemRoot
        nodeId={props.nodeId}
        label={props.children}
        onClick={() => setOpen(true)}
      />
    </>);
}


export default MsgTreeItem;
