import React from 'react';
import { Tooltip, useThemeProps } from '@mui/material';
import HelpOutlineOutlinedIcon from '@mui/icons-material/HelpOutlineOutlined';
import { useUtilityClasses, MUI_NAME, GTooltipRoot } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';

export interface GTooltipProps {
  children: React.ReactNode,
  title: string;
  component?: GOverridableComponent<GTooltipProps>
}



export const GTooltip: React.FC<GTooltipProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = {
    ...props
  }

  const Root = initProps.component ?? GTooltipRoot;

  return (
    <Root ownerState={ownerState} className={classes.root}>
      {props.children}
      <Tooltip title={props.title} arrow disableFocusListener>
        <HelpOutlineOutlinedIcon className={classes.icon} />
      </Tooltip>
    </Root>
  )
}
