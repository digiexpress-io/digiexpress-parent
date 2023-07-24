import { StyledDialog, StyledDialogProps } from './StyledDialog';
import { StyledTreeItem, StyledTreeItemRoot, StyledTreeItemOption, StyledTreeItemProps } from './StyledTreeItem';
import { StyledSelect, StyledSelectMultiple, StyledSelectProps } from './StyledSelect';
import { 
  StyledTextField, StyledNumberField, 
  StyledFileField, StyledSearchField, 
  StyledDateField, StyledDateTimeField, StyledInputFieldProps 
} from './StyledInputField';
import { StyledTransferList, StyledTransferListProps } from './StyledTransferList';
import { StyledPrimaryButton, StyledSecondaryButton} from './StyledButton';
import { StyledCheckbox } from './StyledCheckbox';
import { StyledSwitch, StyledSwitchProps } from './StyledSwitch';
import { StyledRadioButton } from './StyledRadioButton';


declare namespace BurgerStyles {
  export type { StyledDialogProps, StyledSelectProps, StyledInputFieldProps, StyledTreeItemProps, StyledTransferListProps, StyledSwitchProps }
}

namespace BurgerStyles {
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
}

export default BurgerStyles;

