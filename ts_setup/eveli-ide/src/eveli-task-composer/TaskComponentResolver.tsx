

import React from 'react';
import { MenuItem, Tooltip, Button, Stack } from '@mui/material';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';
import { FormattedMessage } from 'react-intl';

import { TaskApi } from '../api-task';
import { mapIamRole, IamApi } from '../api-iam';


export const QUESTIONNAIRE_REVIEW = 'questionnaireId';

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
  pdfCallback: TaskLinkPdfCallback;

  public constructor(openCallback: TaskLinkOpenCallback, pdfCallback: TaskLinkPdfCallback) {
    this.callback = openCallback;
    this.pdfCallback = pdfCallback;
  }

  taskLinkResolver =  (props: TaskLinkProps)=> {
    const linkType = props.link.linkKey;
    if (linkType === QUESTIONNAIRE_REVIEW) {
      return <TaskComponentLink key={props.link.id} link={props.link} taskId={props.taskId} openCallback={this.callback} pdfCallback={this.pdfCallback}/>
    }
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


const TaskComponentLink: React.FC<Props & TaskLinkProps> = (props) => {
  const link = props.link;
  const taskId = props.taskId;
  const pdfCallback = props.pdfCallback;
  return (
    <Stack direction='row' spacing={2}>
      <Button
        onClick={()=>props.openCallback(link)}
        size='small'
        color='secondary'
        variant='contained'
        sx={{borderRadius: 1}}
        endIcon={<ArrowRightIcon/>}
      >
        <FormattedMessage id='taskLink.button.open' />
      </Button>
      { taskId && (
        <Button
          onClick={()=>pdfCallback(link, taskId)}
          size='small'
          color='secondary'
          variant='contained'
          sx={{borderRadius: 1}}
          endIcon={<ArrowRightIcon/>}
        >
          <FormattedMessage id='taskLink.pdf.open' />
        </Button>
      )}
    </Stack>
  )
}
