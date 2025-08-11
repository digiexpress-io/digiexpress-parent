import React from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    Button
} from '@mui/material';
import { useIntl } from 'react-intl';
import { StyledCancelButton } from './StyledButtons';

export interface ConfirmDialogProps {
    open: boolean;
    title: string;
    message: string;
    onCancel: () => void;
    onConfirm: () => void;
    confirmLabel?: string;
}

export const StyledConfirmDialog: React.FC<ConfirmDialogProps> = ({
    open,
    title,
    message,
    onCancel,
    onConfirm,
    confirmLabel
}) => {
    const intl = useIntl();

    return (
        <Dialog
            open={open}
            onClose={onCancel}
            maxWidth="xs"
            fullWidth
            TransitionComponent={React.Fragment}
            keepMounted={false}
        >
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <DialogContentText>{message}</DialogContentText>
            </DialogContent>
            <DialogActions>
                <StyledCancelButton onClick={onCancel} />
                <Button onClick={onConfirm} variant="contained" color="primary">
                    {confirmLabel || intl.formatMessage({ id: 'button.confirm' })}
                </Button>
            </DialogActions>
        </Dialog>
    );
};
