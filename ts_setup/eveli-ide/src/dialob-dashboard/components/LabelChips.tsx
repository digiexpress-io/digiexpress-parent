import React, { useEffect, useRef, useState } from 'react'
import { Box, Chip, IconButton, OutlinedInput, SvgIcon } from '@mui/material';
import { LabelAction } from '../util';
import { Cancel as CancelIcon, Add as AddIcon } from '@mui/icons-material';

export interface LabelChipsProps {
  labels: string[];
  onUpdate: (label: any, action: LabelAction) => Promise<void>
}

export const LabelChips: React.FC<LabelChipsProps> = ({ labels, onUpdate }) => {
  const [newLabel, setNewLabel] = useState('');
  const [showInput, setShowInput] = useState<boolean>(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (showInput && inputRef.current) {
      inputRef.current.focus();
    }
  }, [showInput]);

  const handleAdd = () => {
    if (newLabel.trim()) {
      onUpdate(newLabel.trim(), LabelAction.ADD);
      setNewLabel('');
      setShowInput(false);
    }
  };

  return (
    <Box display="flex" flexWrap="wrap" gap={1} alignItems="center">
      {labels && labels.map((label, index) => (
        <Chip
          key={index}
          label={label}
          onDelete={() => onUpdate(label, LabelAction.DELETE)}
          deleteIcon={<CancelIcon />}
        />
      ))}
      <IconButton size="small" onClick={() => setShowInput(true)}>
        <SvgIcon fontSize="small"><AddIcon /></SvgIcon>
      </IconButton>
      {showInput && (
        <OutlinedInput
          inputRef={inputRef}
          value={newLabel}
          onChange={(e) => setNewLabel(e.target.value)}
          onBlur={() => {
            if (newLabel.length > 0) {
              handleAdd();
            }
            setShowInput(false);
          }}
          sx={{ height: "32px", maxWidth: `${newLabel.length + 3}ch` }}
        />
      )}
    </Box>
  );
};
