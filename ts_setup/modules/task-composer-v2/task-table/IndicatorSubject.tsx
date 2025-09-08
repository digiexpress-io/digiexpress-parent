import React from 'react';
import { Box, Link } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';
import EmojiPeopleOutlinedIcon from '@mui/icons-material/EmojiPeopleOutlined';

import { useTaskBackend } from '@dxs-ts/task-api';



export type IndicatorSubjectProps = {
  title: string
  id?: string
  keywords?: string[]
}



export const IndicatorSubject: React.FC<IndicatorSubjectProps> = ({ title, id, keywords }) => {
  const backend = useTaskBackend();
  const [unreadTasks, setUnreadTasks] = React.useState<string[]>([]);
  const isUnread = id && unreadTasks.includes(id);
  const isProtected = keywords && keywords[0]?.includes('Protected');

  React.useEffect(() => {
    backend.persistence.findAllUnreadTasks().then(setUnreadTasks);
  }, [...backend.deps]);

  const link = (
    <Box
      sx={{
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: 0,
      }}
    >
      <Link href="#" onClick={(event) => {
        event.stopPropagation();
        event.preventDefault();
        backend.navigate.openOneTask(id!);
      }}>
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



