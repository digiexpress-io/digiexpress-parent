import { useApps as useAppsAlias, AppProvider } from './context/AppContext';
import { useDrawer as useDrawerAlias } from './context/drawer/DrawerContext';
import { useTabs as useTabsAlias } from './context/tabs/TabsContext';
import { useSecondary as useSecondaryAlias } from './context/secondary/SecondaryContext';
import { siteTheme } from './theme/siteTheme';


import { StyledDialog } from './styles/StyledDialog';
import { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption } from './styles/StyledTreeItem';
import { StyledSelect, StyledSelectMultiple } from './styles/StyledSelect';
import { StyledTextField, StyledNumberField, StyledFileField, StyledSearchField, StyledDateField, StyledDateTimeField } from './styles/StyledInputField';
import { StyledTransferList } from './styles/StyledTransferList';
import { StyledPrimaryButton, StyledSecondaryButton} from './styles/StyledButton';
import { StyledCheckbox } from './styles/StyledCheckbox';
import { StyledSwitch } from './styles/StyledSwitch';
import { StyledRadioButton } from './styles/StyledRadioButton';


import { DateTimeFormat } from './utils/DateTimeFormatter';
import { ReleaseTable as ReleaseTableAs } from './releases/ReleaseTable';
export type { BurgerApi } from './BurgerApi';


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