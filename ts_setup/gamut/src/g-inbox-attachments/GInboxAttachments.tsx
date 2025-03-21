import React from 'react';
import { Avatar, Chip, useThemeProps } from '@mui/material';
import FilePresentIcon from '@mui/icons-material/FilePresent';
import { GInboxAttachmentsRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { useContracts } from '../api-contract';


export interface GInboxAttachmentsProps {
  name: string;
  subjectId: string;
  attachmentId: string;
  onClick: (subjectId: string, attachmentId: string) => void;
}


export const GInboxAttachments: React.FC<GInboxAttachmentsProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { name, subjectId, attachmentId, onClick } = props;
  const classes = useUtilityClasses();
  const { getContractAttachment } = useContracts();

  function handleDownload() {
    getContractAttachment(subjectId, name)
      .then(({download}) => window.open(download, '_blank')?.focus());

    props.onClick(props.subjectId, props.attachmentId);
  }

  return (
    <GInboxAttachmentsRoot className={classes.root}>
      <Chip onClick={handleDownload}
        className={classes.attachmentItem}
        label={name}
        avatar={
          <Avatar className={classes.attachmentAvatar}>
            <FilePresentIcon className={classes.attachmentIcon} />
          </Avatar>
        }
      />

    </GInboxAttachmentsRoot>
  )
}



