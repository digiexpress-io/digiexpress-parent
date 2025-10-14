import React from 'react';
import { Avatar, Chip, useThemeProps } from '@mui/material';
import { DescriptionOutlined as DescriptionOutlinedIcon } from '@mui/icons-material';
import { GInboxFormAssignedNotCompleteRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GInboxFormAssignedNotCompleteProps {
  formName: string;
  onClick: () => void
}


export const GInboxFormAssignedNotComplete: React.FC<GInboxFormAssignedNotCompleteProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { formName, onClick } = props;
  const classes = useUtilityClasses();



  return (
    <GInboxFormAssignedNotCompleteRoot className={classes.root}>
      <Chip onClick={(event ) => {
        event.stopPropagation();
        onClick();
      }}
        className={classes.formItem}
        label={formName}
        avatar={
          <Avatar className={classes.formAvatar}>
            <DescriptionOutlinedIcon className={classes.formIcon} />
          </Avatar>
        }
      />
    </GInboxFormAssignedNotCompleteRoot>
  )
}



