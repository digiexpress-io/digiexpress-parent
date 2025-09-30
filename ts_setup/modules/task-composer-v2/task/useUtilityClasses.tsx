import { Dialog, TextField, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { DatePicker } from '@dxs-ts/xui-datetime';
import type { DatePickerProps } from '@dxs-ts/xui-datetime';

export const MUI_NAME = 'TaskEditDialog';

export interface TaskEditDialogClasses {
    editDialog: string;
    textField: string;
}
export type TaskEditDialogClassKey = keyof TaskEditDialogClasses;

export const useUtilityClasses = () => {
    const slots = {
        editDialog: ['editDialog'],
        textField: ['textField']
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
};

export const StyledTaskEditDialog = styled(Dialog, {
    name: MUI_NAME, slot: 'EditDialog',
    overridesResolver: (_p, styles) => [
        styles.editDialog,
        styles.textField],
})(() => ({}));

export const StyledTextField = styled(TextField, {
    name: MUI_NAME, slot: 'TextField',
    overridesResolver: (_p, styles) => [styles.textField],
})(({ theme }) => ({
    width: '100%',
    '& .MuiInputBase-input': { height: '2.5rem', padding: '0 12px' },
    '& .MuiInputBase-multiline': { paddingLeft: 0, paddingRight: 0 },
}));

export const StyledDatePicker: React.FC<DatePickerProps> = ({ sx, ...rest }) => (
    <DatePicker {...rest} sx={{ pt: 0.5, ...(sx || {}) }} />
);
