import { Box, Dialog, TextField, Typography, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

export const MUI_NAME = 'CockpitEditDialog';

export interface CockpitEditDialogClasses {
    editDialog: string;
    textField: string;
}
export type CockpitEditDialogClassKey = keyof CockpitEditDialogClasses;

export const useUtilityClasses = () => {
    const slots = {
        editDialog: ['editDialog'],
        textField: ['textField']
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
};

export const StyledCockpitEditDialog = styled(Dialog, {
    name: MUI_NAME, slot: 'EditDialog',
    overridesResolver: (_p, styles) => [
        styles.editDialog,
        styles.textField]
})(() => ({}));

export const StyledTextField = styled(TextField, {
    name: MUI_NAME, slot: 'TextField',
    overridesResolver: (_p, styles) => [styles.textField],
})(({ theme }) => ({
    width: '100%',
    '& .MuiInputBase-input': { height: '2.5rem', padding: '0 12px' },
    '& .MuiInputBase-multiline': { paddingLeft: 0, paddingRight: 0 },
}));

export const StyledEllipsisTypography = styled(Typography, {
    name: MUI_NAME, slot: 'EllipsisTypography',
    overridesResolver: (_p, styles) => [styles.ellipsisTypography],
})(() => ({
    maxWidth: '100%',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    cursor: 'default'
}));


export const StyledConfigBox = styled(Box)(({ theme }) => ({
    border: '1px solid',
    borderColor: theme.palette.divider,
    padding: theme.spacing(2),
    backgroundColor: theme.palette.grey[50],
}));