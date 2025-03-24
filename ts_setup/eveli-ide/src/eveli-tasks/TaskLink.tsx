import React from 'react';
import { Box, Link, LinkProps as MuiLinkProps } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';
import EmojiPeopleOutlinedIcon from '@mui/icons-material/EmojiPeopleOutlined';
import { Link as RouterLink } from '@tanstack/react-router'
import { useFetch } from '@dxs-ts/eveli-fetch';

export type TaskLinkProps = {
  title: string
  id?: string
  keywords?: string[]
}

export const TaskLink: React.FC<TaskLinkProps> = ({ title, id, keywords }) => {

  const { unreadTasks } = useFetch('worker/rest/api/tasks/unread.GET', {});

  const renderLink = React.useMemo(
    () => React.forwardRef<any, MuiLinkProps>((itemProps, ref) => <RouterLink
        ref={ref}
        from='/secured/$locale/worker'
        to='/secured/$locale/worker/tasks/$taskId' 
        params={{ taskId: `${id}`}} 
        children={itemProps.children}
      />),
    [],
  );
  const link = (
    <Link href="#" component={renderLink as any}>
      {title}
    </Link>
  );
  if (id && unreadTasks.includes(id)) {
    if (keywords && keywords[0]?.includes('Protected')) {
      return (
        <Box display="flex" alignItems="center" justifyContent="space-between">
          {link}
          <Box display="flex">
            <LockIcon color='secondary' fontSize='small' sx={{ marginLeft: 1 }} />
            <EmojiPeopleOutlinedIcon color='primary' fontSize='small' sx={{ marginLeft: 1 }} />
          </Box>
        </Box>
      )
    } else {
      return (
        <Box display="flex" alignItems="center" justifyContent="space-between">
          {link}
          <EmojiPeopleOutlinedIcon fontSize='small' color='primary' sx={{ marginLeft: 1 }} />
        </Box>
      )
    }
  }
  if (keywords && keywords[0]?.includes('Protected')) {
    return (
      <Box display="flex" alignItems="center" justifyContent="space-between">
        {link}
        <LockIcon color='primary' fontSize='small' sx={{ marginLeft: 1 }} />
      </Box>
    )
  }
  return link;
}

