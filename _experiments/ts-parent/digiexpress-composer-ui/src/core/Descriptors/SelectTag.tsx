import React from "react";
import {
  Box, Typography, styled, Divider,
  AppBar, Toolbar, IconButton, Badge, InputBase, BadgeProps,
  TextField, MenuItem
} from "@mui/material";
import DeClient from '@declient';



const ComposerMenu: React.FC<{ value: DeClient.ServiceDefinition }> = ({ value }) => {


/**
          {currencies.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}

 */

return     <TextField
          id="outlined-select-currency"
          select
          label="Select"
          defaultValue="EUR"
          helperText="Please select your currency"
        >

        </TextField>
}