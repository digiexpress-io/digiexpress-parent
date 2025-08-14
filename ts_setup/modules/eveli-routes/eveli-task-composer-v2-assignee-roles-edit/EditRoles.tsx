import React from 'react';
import { Autocomplete, Checkbox, TextField } from "@mui/material";
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import { useIntl } from "react-intl";
import { IamApi } from "@dxs-ts/eveli-api";



export type EditRolesProps = {
  assignedRoles: string[]
  groups: IamApi.UserGroup[]
  acceptNewRoles: (selectedRoles: IamApi.UserGroup[]) => void
}


const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;


export const EditRoles: React.FC<EditRolesProps> = ({ assignedRoles, groups, acceptNewRoles }) => {
  const { formatMessage } = useIntl();
  const [roles, setRoles] = React.useState(groups.filter(g => assignedRoles.includes(g.id)));

  const handleChange = (_event: any, newValue: IamApi.UserGroup[]) => {
    setRoles(newValue);
    acceptNewRoles(newValue);
  };


  return (
    <Autocomplete multiple value={roles} options={groups}
      onChange={handleChange}
      disableCloseOnSelect
      getOptionLabel={(option) => option.groupName}
      renderOption={(props, option, { selected }) => (
        <li {...props} key={option.groupName}>
          <Checkbox
            icon={icon}
            checkedIcon={checkedIcon}
            checked={selected}
          />
          {option.groupName}
        </li>
      )}
      renderInput={(params) => (
        <TextField {...params} value={assignedRoles}
          placeholder={formatMessage({ id: 'taskDialog.assignedTo' })}
          autoFocus={true}
        />
      )}
    />
  )
}
