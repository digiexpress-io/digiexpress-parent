
import React, { useState } from 'react';
import { Container } from '@mui/material';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { QUESTIONNAIRE_REVIEW } from '../../components/task/TaskLinkKey';
//import { ReviewDialog } from './ReviewDialog';

import { AttachmentContextProvider } from '../../context/AttachmentContext';

import { TaskView } from './TaskView';
import { TasksComponentResolver } from './LinkResolver';
import { TaskApi } from '@/burger';


export const TaskContainer: React.FC<{ taskId?: string }> = (props) => {
  let id: any = props.taskId;

  const { groups } = useFetch('$org/groupsList.GET', {});
  const { getUsers } = useFetch('$org/groupMembership.GET', {});
  const { pdfTaskLinkCallback } = useFetch('worker/rest/api/pdf.GET', {});

  const openTaskLinkCallback = (link: TaskApi.TaskLink) => {
    setLink(link);
    if (link.linkKey === QUESTIONNAIRE_REVIEW) {
      setReviewDialogOpen(true);
    }
  }


  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [link, setLink] = useState<TaskApi.TaskLink | null>(null);
  const componentResolver = new TasksComponentResolver(openTaskLinkCallback, pdfTaskLinkCallback);


  return (
    <AttachmentContextProvider>
      <Container maxWidth='lg'>
        <TaskView taskId={id} groups={groups} getUsers={getUsers} userSelectionFree={true}
          componentResolver={componentResolver} externalThreads={true} />
        {/*!!link && reviewDialogOpen &&
        <ReviewDialog closeDialog={()=>setReviewDialogOpen(false)} link={link}/>
      */}
      </Container>
    </AttachmentContextProvider>
  )
}