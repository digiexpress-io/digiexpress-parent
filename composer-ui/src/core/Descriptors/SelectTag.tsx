import React from "react";
import {
  Box, Typography, styled, Divider,
  AppBar, Toolbar, IconButton, Badge, InputBase, BadgeProps,
  TextField, MenuItem
} from "@mui/material";



const ComposerMenu: React.FC<{ value: DeClient.ServiceDefinition }> = ({ value }) => {

    <TextField
          id="outlined-select-currency"
          select
          label="Select"
          defaultValue="EUR"
          helperText="Please select your currency"
        >
          {currencies.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </TextField>
}