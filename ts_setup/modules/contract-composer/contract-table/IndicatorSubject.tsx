import React from 'react';
import { Box, Link } from '@mui/material';
import { Lock as LockIcon } from '@mui/icons-material';
import { EmojiPeopleOutlined as EmojiPeopleOutlinedIcon } from '@mui/icons-material';

import { useContractBackend } from '@dxs-ts/contract-api';



export type IndicatorSubjectProps = {
  title: string
  id?: string
}

export const IndicatorSubject: React.FC<IndicatorSubjectProps> = ({ title, id }) => {
  const backend = useContractBackend();


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
        backend.navigate.openOneContract(id!);
      }}>
        {title}
      </Link>
    </Box>
  );

  const isSaving = false;
  const isPension = false;

  const icons = (
    <>
      {isSaving && (
        <LockIcon
          color={'primary'}
          fontSize="small"
          sx={{ ml: 1, flexShrink: 0 }}
        />
      )}
      {isPension && (
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



