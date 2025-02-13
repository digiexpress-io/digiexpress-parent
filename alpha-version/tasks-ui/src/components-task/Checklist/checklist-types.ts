import { ChecklistItem } from "descriptor-task";

export interface ChecklistItemComponentProps {
  item: ChecklistItem;
  onChecked: () => void;
  onDeleteClick: () => void;
  onClick: (item: ChecklistItem, e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
}

export interface ChecklistItemActionProps {
  mode: 'add' | 'edit';
  dueDate: Date | string | undefined;
  assigneeIds: string[] | [];
  variant?: 'normal' | 'hover';
  onDeleteClick?: () => void;
  setDatePickerOpen: (value: React.SetStateAction<boolean>) => void;
  setAssigneePickerOpen: (value: React.SetStateAction<boolean>) => void;
}

export interface ChecklistItemDialogProps {
  mode: 'add' | 'edit';
  open: boolean;
  onClose: () => void;
  item?: ChecklistItem;
  onDeleteClick?: () => void;
  onSave: (item: ChecklistItem) => void;
  onUpdate: (item: ChecklistItem) => void;
}
