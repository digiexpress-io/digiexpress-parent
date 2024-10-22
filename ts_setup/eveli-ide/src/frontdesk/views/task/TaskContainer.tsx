
import React, { useCallback, useContext, useMemo, useState } from 'react';

import { Container } from '@mui/material';
import { TasksComponentResolver } from './LinkResolver';
import { QUESTIONNAIRE_REVIEW } from '../../components/task/TaskLinkKey';
import { ReviewDialog } from './ReviewDialog';
import { useFetch } from '../../hooks/useFetch';
import { Group as OrgGroup } from '../../types/Group';
import { ROLE_AUTHORIZED } from '../../util/rolemapper';
import { useIntl } from 'react-intl';
import { TaskView } from './TaskView';
import { AttachmentContextProvider } from '../../context/AttachmentContext';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { GroupMember } from '../../types/GroupMember';
import { useParams } from 'react-router-dom';
import { UserGroup } from '../../types/UserGroup';
import { TaskLink } from '../../types/task/TaskLink';


type OwnProps = {
  taskId?: number
}

type Props = OwnProps;

export const TaskContainer:React.FC<Props> = (props) => {
  const params = useParams();
  const {response:groupResponse} = useFetch<OrgGroup[]>(`/groupsList`);
  const intl = useIntl();
  const config = useConfig();
  const session = useContext(SessionRefreshContext);

  let id:any = props.taskId;
  if (!id) {
    id = params.id;
  }

  const groups:UserGroup[] = useMemo(() => {
    if (groupResponse) {
      let result = groupResponse.map(response=>{
        return {id:response.name, 
          groupName:response.description}});
      result.push({id:ROLE_AUTHORIZED, groupName: intl.formatMessage({id:'task.role.assignedAllUsers'})});
      return result;
    }
    return [];
  },[groupResponse, intl]);

  const getUsers = useCallback(async (groupName:string[]):Promise<GroupMember[]> => {
    if(!groupName || groupName.length === 0) {
      return [];
    }
    const filteredGroups = groupName.filter(name=>name!==ROLE_AUTHORIZED).join(',');
    if (!filteredGroups) {
      return [];
    }
    return await session.cFetch(`/groupMembership?groupName=${filteredGroups}`)
    .then(response => response.json());
  }, [session]);

  const openTaskLinkCallback = (link:TaskLink) => {
    setLink(link);
    if (link.linkKey ===QUESTIONNAIRE_REVIEW) {
      setReviewDialogOpen(true);
    }
  }
  const pdfTaskLinkCallback = (link:TaskLink, taskId: number) => {
    let url = `${config.wrenchApiUrl}/pdf?taskId=${taskId}&questionnaireId=${link.linkAddress}`;
    window.open(url);
  }

  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const [link, setLink] = useState<TaskLink|null>(null);
  const componentResolver = new TasksComponentResolver(openTaskLinkCallback, pdfTaskLinkCallback);


  return (
    <AttachmentContextProvider apiBaseUrl={`${config.wrenchApiUrl}/api/attachments`}>
    <Container maxWidth='lg'>
      <TaskView taskId={id} groups={groups} getUsers={getUsers} userSelectionFree={true}
        componentResolver={componentResolver} externalThreads={true}/>
      {!!link && reviewDialogOpen &&
        <ReviewDialog closeDialog={()=>setReviewDialogOpen(false)} link={link}/>
      }
    </Container>
    </AttachmentContextProvider>
  )
}