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

        // layout slots
        headerRow: ['headerRow'],
        fileRow: ['fileRow'],

        // columns
        colName: ['colName'],
        colDate: ['colDate'],
        colAction: ['colAction'],
        colNameRow: ['colNameRow'],
        colDateRow: ['colDateRow'],

        // text & icons
        fileIcon: ['fileIcon'],
        fileName: ['fileName'],
        noWrapEllipsis: ['noWrapEllipsis'],
        deleteIcon: ['deleteIcon'],
        downloadIcon: ['downloadIcon'],

        // empty state
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

        // header
        [`& .${FILES_EDITOR_MUI_NAME}-headerRow`]: {
            display: 'flex',
            alignItems: 'center',
            padding: theme.spacing(1),
            fontWeight: 'bold',
            borderBottom: `1px solid ${theme.palette.divider}`,
        },

        // rows
        [`& .${FILES_EDITOR_MUI_NAME}-fileRow`]: {
            display: 'flex',
            alignItems: 'center',
            padding: theme.spacing(1),
            borderBottom: border,
        },

        // columns layout
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

        // name + icon in rows
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

        // date text
        [`& .${FILES_EDITOR_MUI_NAME}-noWrapEllipsis`]: {
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
        },

        // icons
        [`& .${FILES_EDITOR_MUI_NAME}-deleteIcon`]: {
            color: theme.palette.error.main,
        },
        [`& .${FILES_EDITOR_MUI_NAME}-downloadIcon`]: {
            color: theme.palette.primary.main,
        },

        // empty state
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

        // title & actions
        titleRow: ['titleRow'],
        actionsRow: ['actionsRow'],
        grow: ['grow'],
        hiddenInput: ['hiddenInput'],
        uploadBtn: ['uploadBtn'],
        mockedBadge: ['mockedBadge'],

        // content & actions
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
        // Root area
        [`&.${FILES_EDIT_DIALOG_MUI_NAME}-root`]: {},

        // Title row layout
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
        // optional: 'grow' if you want a class instead of <Box flexGrow={1} />
        grow: ['grow'],
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
        // one row
        [`& .${FILES_READ_ONLY_MUI_NAME}-file`]: {
            padding: theme.spacing(1),
            display: 'flex',
            alignItems: 'center',
            width: '100%',
            // apply TaskCard's typography to all Typography in the row
            '.MuiTypography-root': {
                ...style.bodyTypography,
            },
        },

        // file icon
        [`& .${FILES_READ_ONLY_MUI_NAME}-fileIcon`]: {
            marginRight: theme.spacing(1),
            color: theme.palette.primary.main,
            flexShrink: 0,
        },

        // empty state
        [`& .${FILES_READ_ONLY_MUI_NAME}-empty`]: {
            padding: theme.spacing(1),
            color: theme.palette.error.main,
            ...style.bodyTypography,
        },

        // optional grow helper (if you want a class instead of <Box flexGrow={1} />)
        [`& .${FILES_READ_ONLY_MUI_NAME}-grow`]: {
            flexGrow: 1,
        },
    };
});
