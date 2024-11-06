
import React, { useCallback, useContext, useMemo, useState } from 'react';
import { Container } from '@mui/material';
import { useParams } from 'react-router-dom';
import { useIntl } from 'react-intl';

import { QUESTIONNAIRE_REVIEW } from '../../components/task/TaskLinkKey';
//import { ReviewDialog } from './ReviewDialog';
import { useFetch } from '../../hooks/useFetch';

import { ROLE_AUTHORIZED } from '../../util/rolemapper';

import { AttachmentContextProvider } from '../../context/AttachmentContext';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';

import { GroupMember } from '../../types/GroupMember';
import { UserGroup } from '../../types/UserGroup';
import { TaskLink } from '../../types/task/TaskLink';
import { Group as OrgGroup } from '../../types/Group';

import { TaskView } from './TaskView';
import { TasksComponentResolver } from './LinkResolver';


type Props = {
  taskId?: number
}

export const TaskContainer: React.FC<Props> = (props) => {
  const params = useParams();
  const { response: groupResponse } = useFetch<OrgGroup[]>(`/groupsList`);
  const intl = useIntl();
  const { serviceUrl } = useConfig();
  const session = useContext(SessionRefreshContext);

  let id: any = props.taskId;
  if (!id) {
    id = params.id;
  }

  const groups: UserGroup[] = useMemo(() => {
    if (groupResponse) {
      let result = groupResponse.map(response => {
        return {
          id: response.name,
          groupName: response.description
        }
      });
      result.push({ id: ROLE_AUTHORIZED, groupName: intl.formatMessage({ id: 'task.role.assignedAllUsers' }) });
      return result;
    }
    return [];
  }, [groupResponse, intl]);

  const getUsers = useCallback(async (groupName: string[]): Promise<GroupMember[]> => {
    if (!groupName || groupName.length === 0) {
      return [];
    }
    const filteredGroups = groupName.filter(name => name !== ROLE_AUTHORIZED).join(',');
    if (!filteredGroups) {
      return [];
    }
    return await session.cFetch(`/groupMembership?groupName=${filteredGroups}`)
      .then(response => response.json());
  }, [session]);

  const openTaskLinkCallback = (link: TaskLink) => {
    setLink(link);
    if (link.linkKey === QUESTIONNAIRE_REVIEW) {
      setReviewDialogOpen(true);
    }
  }
  const pdfTaskLinkCallback = (link: TaskLink, taskId: number) => {
    let url = `${serviceUrl}rest/api/worker/pdf?taskId=${taskId}&questionnaireId=${link.linkAddress}`;
    window.open(url);
  }

  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [link, setLink] = useState<TaskLink | null>(null);
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