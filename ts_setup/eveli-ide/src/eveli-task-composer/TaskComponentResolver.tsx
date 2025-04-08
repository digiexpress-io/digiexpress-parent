

import React from 'react';
import { MenuItem, Tooltip} from '@mui/material';

import { TaskApi } from '../api-task';
import { mapIamRole, IamApi } from '../api-iam';


export interface ComponentResolver {
  taskLinkResolver?: (props:TaskLinkProps)=>JSX.Element|null;
  groupListItemResolver?:(groups: IamApi.UserGroup[])=>JSX.Element[];
  userListItemResolver?:(users?: IamApi.User[])=>JSX.Element[];
}

export type TaskLinkProps = {
  link: TaskApi.TaskLink
  taskId?: string
}

export interface TaskLinkOpenCallback {
  (link: TaskApi.TaskLink):void
}
export interface TaskLinkPdfCallback {
  (link: TaskApi.TaskLink, taskId: string):void
}

type Props = {
  openCallback: TaskLinkOpenCallback
  pdfCallback: TaskLinkPdfCallback
}


export class TasksComponentResolver implements ComponentResolver {
  callback: TaskLinkOpenCallback;

  public constructor(openCallback: TaskLinkOpenCallback) {
    this.callback = openCallback;
  }

  taskLinkResolver =  (props: TaskLinkProps)=> {
    const linkType = props.link.linkKey;
    return null;
  }

  groupListItemResolver = (groups: IamApi.UserGroup[]) => {
    let result:JSX.Element[] = [];
    groups.forEach(group => {
      result.push( 
        <MenuItem key={group.id} value={group.id}>
          <Tooltip key={group.id} placement='left' title={mapIamRole(group.id)} >
            <div>        
              {group.groupName || mapIamRole(group.id)}
            </div>
          </Tooltip>
        </MenuItem>
      )
    });
    return result;
  }
}