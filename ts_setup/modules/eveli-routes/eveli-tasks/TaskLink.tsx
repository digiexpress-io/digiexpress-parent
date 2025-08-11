import React from 'react';
import { Box, Link, LinkProps } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';
import EmojiPeopleOutlinedIcon from '@mui/icons-material/EmojiPeopleOutlined';
import { Link as RouterLink } from '@tanstack/react-router'
import { useFetch } from '@dxs-ts/envir-fetch';


export type TaskLinkProps = {
  title: string
  id?: string
  keywords?: string[]
}

const LinkOverride = React.forwardRef<any, LinkProps & { taskId?: string }>((itemProps, ref) => {
  const { taskId } = itemProps;
  return (<RouterLink
    ref={ref}
    from='/secured/$locale/worker'
    to='/secured/$locale/worker/tasks/$taskId'
    params={{ taskId: `${taskId!}` }}
    children={itemProps.children}
  />)
})

export const TaskLink: React.FC<TaskLinkProps> = ({ title, id, keywords }) => {
  const { unreadTasks } = useFetch('worker/rest/api/tasks/unread.GET', {});
  const isUnread = id && unreadTasks.includes(id);
  const isProtected = keywords && keywords[0]?.includes('Protected');

  const link = (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link
        href="#"
        component={LinkOverride}
        taskId={id}
      >
        {title}
      </Link>
    </Box>
  );

  const icons = (
    <>
      {isProtected && (
        <LockIcon
          color={isUnread ? 'secondary' : 'primary'}
          fontSize="small"
          sx={{ ml: 1, flexShrink: 0 }}
        />
      )}
      {isUnread && (
        <EmojiPeopleOutlinedIcon
          color="primary"
          fontSize="small"
          sx={{ ml: 1, flexShrink: 0 }}
        />
      )}
    </>
  );

  return (
    <Box display="flex" alignItems="center">
      <Box sx={{ minWidth: 0, flexGrow: 1, overflow: 'hidden' }}>
        {link}
      </Box>
      {icons}
    </Box>
  );
};



