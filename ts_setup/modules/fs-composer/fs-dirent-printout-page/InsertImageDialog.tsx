import React from 'react';
import { Dialog, DialogContent, DialogTitle, List, ListItemButton, ListItemText } from '@mui/material';
import { FsDirentPrintoutPageDialogList, useUtilityClasses } from './useUtilityClasses';
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
            {images.map(image => (
              <ListItemButton key={image.id} className={classes.dialogListItem} onClick={() => onSelect(image)}>
                <ListItemText primary={image.resourceName} />
                <div className={classes.dialogItemEnd}>
                  {image.printoutPageIds.includes(direntId) && (
                    <FsIcons.Checkmark className={classes.dialogCheckmark} />
                  )}
                  {image.content && (
                    <img className={classes.dialogThumbnail}
                      src={`data:${image.contentType === 'image/*' ? 'image/png' : image.contentType};base64,${image.content}`}
                    />
                  )}
                </div>
              </ListItemButton>
            ))}
          </List>
        </FsDirentPrintoutPageDialogList>
      </DialogContent>
    </Dialog>
  );
};