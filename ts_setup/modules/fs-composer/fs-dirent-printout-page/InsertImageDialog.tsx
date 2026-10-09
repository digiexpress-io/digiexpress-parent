import React from 'react';
import { Dialog, DialogContent, DialogTitle, List, ListItemButton, ListItemText } from '@mui/material';
import { FsDirentPrintoutPageDialogList, FsDirentPrintoutPageThumbnailPreview, FsDirentPrintoutPageThumbnailTooltip, useUtilityClasses } from './useUtilityClasses';
import { FsIcons } from '../fs-theme';
import { Fs } from '@dxs-ts/fs-api';


export const InsertImageDialog: React.FC<{
  open: boolean;
  direntId: string;
  images: Fs.PrintoutResourceProps[];
  onSelect: (resource: Fs.PrintoutResourceProps) => void;
  onClose: () => void;
}> = ({ open, direntId, images, onSelect, onClose }) => {
  const classes = useUtilityClasses();

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Insert image</DialogTitle>
      <DialogContent>
        <FsDirentPrintoutPageDialogList>
          <List>
            {images.map(image => {
              const isActive = image.printoutPageIds.includes(direntId);
              const src = image.content
                ? `data:${image.contentType === 'image/*' ? 'image/png' : image.contentType};base64,${image.content}`
                : undefined;
              return (
                <ListItemButton
                  key={image.id}
                  className={`${classes.dialogListItem}${isActive ? ` ${classes.dialogListItemActive}` : ''}`}
                  onClick={() => onSelect(image)}
                >
                  {isActive && (
                    <FsIcons.Checkmark className={classes.dialogCheckmark} />
                  )}
                  <ListItemText primary={image.resourceName} />
                  <div className={classes.dialogItemEnd}>
                    {src && (
                      <FsDirentPrintoutPageThumbnailTooltip
                        title={<FsDirentPrintoutPageThumbnailPreview src={src} alt={image.resourceName} />}
                        placement='right'
                        arrow={false}
                      >
                        <div className={classes.dialogThumbnailAnchor}>
                          <img className={classes.dialogThumbnail} src={src} alt={image.resourceName} />
                        </div>
                      </FsDirentPrintoutPageThumbnailTooltip>
                    )}
                  </div>
                </ListItemButton>
              );
            })}
          </List>
        </FsDirentPrintoutPageDialogList>
      </DialogContent>
    </Dialog>
  );
};
