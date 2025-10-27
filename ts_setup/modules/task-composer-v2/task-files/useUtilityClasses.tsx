import { alpha, Dialog, generateUtilityClass, styled } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import type { TaskCardStyleDefinition } from '../task-card';

/**
 * ==========================
 * FilesEditor styles & classes
 * ==========================
 */
export const FILES_EDITOR_MUI_NAME = 'FilesEditor';

export const useFilesEditorClasses = () => {
    const slots = {
        root: ['root'],
        headerRow: ['headerRow'],
        fileRow: ['fileRow'],
        colName: ['colName'],
        colDate: ['colDate'],
        colAction: ['colAction'],
        colNameRow: ['colNameRow'],
        colDateRow: ['colDateRow'],
        fileIcon: ['fileIcon'],
        fileName: ['fileName'],
        noWrapEllipsis: ['noWrapEllipsis'],
        deleteIcon: ['deleteIcon'],
        downloadIcon: ['downloadIcon'],
        noFiles: ['noFiles'],
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(FILES_EDITOR_MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
};

export const FilesEditorRoot = styled('div', {
    name: FILES_EDITOR_MUI_NAME,
    slot: 'Root',
    overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => {
    const border = `1px solid ${alpha(theme.palette.divider, 0.5)}`;
    return {
        width: '100%',

        [`& .${FILES_EDITOR_MUI_NAME}-headerRow`]: {
            display: 'flex',
            alignItems: 'center',
            padding: theme.spacing(1),
            fontWeight: 'bold',
            borderBottom: `1px solid ${theme.palette.divider}`,
        },

        [`& .${FILES_EDITOR_MUI_NAME}-fileRow`]: {
            display: 'flex',
            alignItems: 'center',
            padding: theme.spacing(1),
            borderBottom: border,
        },

        [`& .${FILES_EDITOR_MUI_NAME}-colName`]: {
            flex: 1,
            minWidth: 0,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-colDate, & .${FILES_EDITOR_MUI_NAME}-colDateRow`]: {
            width: 260,
            minWidth: 260,
            flexShrink: 0,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-colAction`]: {
            width: 40,
            minWidth: 40,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
        },

        [`& .${FILES_EDITOR_MUI_NAME}-colNameRow`]: {
            display: 'flex',
            alignItems: 'center',
            gap: theme.spacing(1),
            minWidth: 0,
            flex: 1,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-fileIcon`]: {
            color: theme.palette.primary.main,
            flexShrink: 0,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-fileName`]: {
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
        },

        [`& .${FILES_EDITOR_MUI_NAME}-noWrapEllipsis`]: {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
        },

        [`& .${FILES_EDITOR_MUI_NAME}-deleteIcon`]: {
            color: theme.palette.error.main,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-downloadIcon`]: {
            color: theme.palette.primary.main,
        },

        [`& .${FILES_EDITOR_MUI_NAME}-noFiles`]: {
            padding: theme.spacing(1),
            color: theme.palette.text.secondary,
        },
    };
});


/**
 * ==========================
 * FilesEditDialog styles & classes
 * ==========================
 */
export const FILES_EDIT_DIALOG_MUI_NAME = 'FilesEditDialog';

export const useFilesEditDialogClasses = () => {
    const slots = {
        root: ['root'],
        titleRow: ['titleRow'],
        actionsRow: ['actionsRow'],
        grow: ['grow'],
        hiddenInput: ['hiddenInput'],
        uploadBtn: ['uploadBtn'],
        mockedBadge: ['mockedBadge'],
        content: ['content'],
        dialogActions: ['dialogActions'],
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(FILES_EDIT_DIALOG_MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
};

export const FilesEditDialogRoot = styled(Dialog, {
    name: FILES_EDIT_DIALOG_MUI_NAME,
    slot: 'Root',
    overridesResolver: (_props, styles) => [styles.root],
})(({ theme }) => {
    return {
        [`&.${FILES_EDIT_DIALOG_MUI_NAME}-root`]: {},

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-titleRow`]: {
            display: 'flex',
            alignItems: 'center',
            gap: theme.spacing(2),
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-actionsRow`]: {
            display: 'flex',
            alignItems: 'center',
            gap: theme.spacing(1.5),
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-grow`]: {
            flexGrow: 1,
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-hiddenInput`]: {
            display: 'none',
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-uploadBtn`]: {},

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-mockedBadge`]: {
            marginLeft: theme.spacing(2),
            color: theme.palette.text.secondary,
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-content`]: {
            display: 'flex',
            flexDirection: 'column',
        },

        [`& .${FILES_EDIT_DIALOG_MUI_NAME}-dialogActions`]: {},
    };
});

// ===========================================
// FilesReadOnly styles & classes
// ===========================================
export const FILES_READ_ONLY_MUI_NAME = 'TaskFiles';

export const useFilesReadOnlyClasses = () => {
    const slots = {
        root: ['root'],
        file: ['file'],
        fileIcon: ['fileIcon'],
        empty: ['empty'],
        grow: ['grow'],
        timestamp: ['timestamp'],
    };
    const getUtilityClass = (slot: string) => generateUtilityClass(FILES_READ_ONLY_MUI_NAME, slot);
    return composeClasses(slots, getUtilityClass, {});
};

export const FilesReadOnlyRoot = styled('div', {
    name: FILES_READ_ONLY_MUI_NAME,
    slot: 'Root',
    overridesResolver: (_props, styles) => [styles.root],
})<{ style: TaskCardStyleDefinition }>(({ theme, style }) => {
    return {
        [`& .${FILES_READ_ONLY_MUI_NAME}-file`]: {
            padding: theme.spacing(1),
            display: 'flex',
            alignItems: 'center',
            width: '100%',
            '.MuiTypography-root': {
                ...style.bodyTypography,
            },
        },

        [`& .${FILES_READ_ONLY_MUI_NAME}-fileIcon`]: {
            marginRight: theme.spacing(1),
            color: theme.palette.primary.main,
            flexShrink: 0,
        },

        [`& .${FILES_READ_ONLY_MUI_NAME}-empty`]: {
            padding: theme.spacing(1),
            color: theme.palette.error.main,
            ...style.bodyTypography,
        },

        [`& .${FILES_READ_ONLY_MUI_NAME}-grow`]: {
            flexGrow: 1,
        },

        [`& .${FILES_READ_ONLY_MUI_NAME}-timestamp`]: {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            ...style.bodyTypography,
        },
    };
});
