import React from 'react';
import { TaskLinkComponent, TaskLinkOpenCallback, TaskLinkPdfCallback, TaskLinkProps } from '../../components/task/TaskLinkComponent';
import { QUESTIONNAIRE_REVIEW } from '../../components/task/TaskLinkKey';
import { MenuItem, Tooltip } from '@mui/material';
import { mapRole } from '../../util/rolemapper';
import { UserGroup } from '../../types/UserGroup';
import { ComponentResolver } from '../../context/ComponentResolver';


export class TasksComponentResolver implements ComponentResolver {
  callback: TaskLinkOpenCallback;
  pdfCallback: TaskLinkPdfCallback;
  public constructor(openCallback: TaskLinkOpenCallback, pdfCallback: TaskLinkPdfCallback) {
    this.callback = openCallback;
    this.pdfCallback = pdfCallback;
  }
  taskLinkResolver =  (props: TaskLinkProps)=> {
    const linkType = props.link.linkKey;
    if (linkType === QUESTIONNAIRE_REVIEW ) {
      return <TaskLinkComponent key={props.link.id} link={props.link} taskId={props.taskId} openCallback={this.callback} pdfCallback={this.pdfCallback}/>
    }
    return null;
  }

  groupListItemResolver = (groups: UserGroup[]) => {
    let result:JSX.Element[] = [];
    groups.forEach(group=> {
      result.push( 
        <MenuItem key={group.id} value={group.id}>
          <Tooltip key={group.id} placement='left' title={mapRole(group.id)} >
            <div>        
              {group.groupName || mapRole(group.id)}
            </div>
          </Tooltip>
        </MenuItem>
      )
    });
    return result;
  }
}