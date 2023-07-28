import React from "react";

import { Box, Typography, styled } from "@mui/material";
import ForumIcon from '@mui/icons-material/Forum';

import { demoThreads } from "./DemoThreads";
import { Thread } from "./thread-types";
import ThreadPreview from "./ThreadPreview";
import ThreadDetails from "./ThreadDetails";

const LeftSideContainer = styled(Box)(({ theme }) => ({
  borderRight: '1px solid',
  borderColor: theme.palette.divider,
  height: '100vh',
  width: '50%',
}))

const RightSideContainer = styled(Box)(({ theme }) => ({
  width: '50%',
}))

const Inbox: React.FC<{}> = () => {
  const lastestThread = demoThreads.sort((a, b) => b.messages[0].date.getTime() - a.messages[0].date.getTime())[0];
  const [activeThread, setActiveThread] = React.useState<Thread>(lastestThread);

  const handleThreadClick = (thread: Thread) => {
    setActiveThread(thread);
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <LeftSideContainer>
        <Box sx={{ display: 'flex', m: 2 }}>
          <ForumIcon sx={{ mr: 1 }} color='primary' />
          <Typography fontWeight='bold'>Threads</Typography>
        </Box>
        {demoThreads.map(thread => <ThreadPreview key={thread.id} thread={thread} onClick={handleThreadClick} />)}
      </LeftSideContainer>
      <RightSideContainer>
        <ThreadDetails thread={activeThread} />
      </RightSideContainer>
    </Box>
  )
}

export { Inbox }
