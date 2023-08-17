import { AppProviderProps, useApps as useAppsAlias, AppProvider } from './context/AppContext';
import { useDrawer as useDrawerAlias } from './context/drawer/DrawerContext';
import { useTabs as useTabsAlias } from './context/tabs/TabsContext';
import { useSecondary as useSecondaryAlias } from './context/secondary/SecondaryContext';
import { siteTheme } from './theme/siteTheme';

import {
  AppContextType, AppSession, AppActions, 
  App, AppId, AppState, AppStateCreate, AppStateRestore
} from './context/AppAPI';
import {
  DrawerContextType, DrawerSession, DrawerActions
} from './context/drawer/DrawerAPI';
import {
  TabsContextType, TabsSession, TabSession, TabsHistory, TabsActions
} from './context/tabs/TabsAPI';
import {
  SecondaryContextType, SecondarySession, SecondaryActions 
} from './context/secondary/SecondaryAPI';

import { StyledDialog, StyledDialogProps } from './styles/StyledDialog';
import { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption, StyledTreeItemProps } from './styles/StyledTreeItem';
import { StyledSelect, StyledSelectMultiple, StyledSelectProps } from './styles/StyledSelect';
import { StyledTextField, StyledNumberField, StyledFileField, StyledSearchField, StyledInputFieldProps, StyledDateField, StyledDateTimeField } from './styles/StyledInputField';
import { StyledTransferList, StyledTransferListProps } from './styles/StyledTransferList';
import { StyledPrimaryButton, StyledSecondaryButton} from './styles/StyledButton';
import { StyledCheckbox } from './styles/StyledCheckbox';
import { StyledSwitch, StyledSwitchProps } from './styles/StyledSwitch';
import { StyledRadioButton } from './styles/StyledRadioButton';


import { DateTimeFormat, DateTimeFormatProps } from './utils/DateTimeFormatter';
import { ReleaseTable as ReleaseTableAs, Release, ReleaseTableProps } from './releases/ReleaseTable';


declare namespace Burger {
  export { 
    AppProviderProps, AppContextType, AppSession, AppActions, App, AppId, AppState, AppStateCreate, AppStateRestore,
    DrawerContextType, DrawerSession, DrawerActions,
    TabsContextType, TabsSession, TabSession, TabsHistory, TabsActions,
    SecondaryContextType, SecondarySession, SecondaryActions,
  };
  export { StyledDialogProps, StyledSelectProps, StyledInputFieldProps, StyledTreeItemProps, StyledTransferListProps, StyledSwitchProps }
  export { DateTimeFormatProps }
  export { Release, ReleaseTableProps }
}

namespace Burger {
  export const Provider = AppProvider;
  export const useApps = useAppsAlias; 
  export const useDrawer = useDrawerAlias;
  export const useTabs = useTabsAlias;
  export const useSecondary = useSecondaryAlias;
  
  
  export const Dialog = StyledDialog;
  export const Select = StyledSelect;
  export const SelectMultiple = StyledSelectMultiple;
  export const TextField = StyledTextField;
  export const NumberField = StyledNumberField;
  export const FileField = StyledFileField;
  export const DateField = StyledDateField;
  export const DateTimeField = StyledDateTimeField;
  
  export const TreeItem = StyledTreeItem;
  export const TreeItemRoot = StyledTreeItemRoot;
  export const TreeItemOption = StyledTreeItemOption;
  
  export const SearchField = StyledSearchField;
  export const TransferList = StyledTransferList;
  export const PrimaryButton = StyledPrimaryButton;
  export const SecondaryButton = StyledSecondaryButton;
  export const Checkbox = StyledCheckbox;
  export const Switch = StyledSwitch;
  export const RadioButton = StyledRadioButton;

  export const DateTimeFormatter = DateTimeFormat;
  export const ReleaseTable = ReleaseTableAs;

}

export { siteTheme };
export default Burger;