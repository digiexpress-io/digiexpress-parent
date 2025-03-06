import { useThemeProps } from '@mui/system';
import { UiDevExplorer } from './UiDevExplorer';
import { MUI_NAME, UiDevAppRoot, useUtilityClasses } from './useUtilityClasses';
import { UiDevSearchAsset } from './UiDevSearchAsset';


export interface UiDevAppProps {
  
}


export const UiDev: React.FC<UiDevAppProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const ownerState = {
    ...props
  }
  const classes = useUtilityClasses();

  return (
    <UiDevAppRoot ownerState={ownerState} className={classes.root}>
      <UiDevExplorer />
      <UiDevSearchAsset />
    </UiDevAppRoot>
  )
}