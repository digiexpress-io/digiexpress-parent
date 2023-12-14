/*-
 * #%L
 * wrench-assets-ide
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import React from 'react'

import { InputLabel, Select, MenuItem, TextField, Checkbox, Button, Box, FormControl, FormControlLabel, Grid } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { NumberBuilder } from '../builders'


export const EditNumber: React.FC<{ builder: NumberBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  const handleComparisonTypeChange = (type: string) => {
    if (type === builder.getType()) {
      return;
    }
    onChange(builder.withType(type))
  }

  const handleComparisonOperatorChange = (value: string) => {
    onChange(builder.withOperator(value))
  }

  const handleComparisonValueChange = (value: string) => {
    onChange(builder.withOperatorValue(value))
  }

  const handleRangeStartChange = (value: string) => {
    onChange(builder.withStart(value))
  }

  const handleRangeEndChange = (value: string) => {
    onChange(builder.withEnd(value))
  }

  const handleRangeStartIncludeChange = (value: boolean) => {
    onChange(builder.withStartInclude(value))
  }

  const handleRangeEndIncludeChange = (value: boolean) => {
    onChange(builder.withEndInclude(value))
  }

  const comparisonRenderer = () => {

    return <Box component="form" noValidate autoComplete="off">
      <Box display="flex" sx={{ pb: 1 }}>
        <Box flexGrow={1} />
        <Button variant="outlined" onClick={() => handleComparisonTypeChange('range')}><FormattedMessage id='dt.cell.change.range' /></Button>
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={3}>
          <FormControl variant="filled" fullWidth>
            <InputLabel><FormattedMessage id='dt.cell.comparison' /></InputLabel>
            <Select fullWidth
              onChange={({ target }) => handleComparisonOperatorChange(target.value)}
              value={builder.getOperator()}
              label={<FormattedMessage id='dt.cell.comparison' />}>
              {builder.comparisonOperators.map(v => (<MenuItem  key={v.value} value={v.value}>{v.text}</MenuItem>))}
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={9}>
          <TextField type='number' fullWidth
            label={<FormattedMessage id='dt.cell.newvalue' />}
            value={builder.getValue()}
            onChange={({ target }) => handleComparisonValueChange(target.value)} />
        </Grid>
      </Grid>
    </Box>
  }

  const rangeRenderer = () => {
    
    return <Box component="form" noValidate autoComplete="off">
      <Box display="flex" sx={{ pb: 1 }}>
        <Box flexGrow={1} />
        <Button variant="outlined" onClick={() => handleComparisonTypeChange('operator')}><FormattedMessage id='dt.cell.change.comparison' /></Button>
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={9}>
          <TextField type='number' fullWidth
            label={<FormattedMessage id='dt.cell.range.start' />}
            value={builder.getStart()}
            onChange={({ target }) => handleRangeStartChange(target.value)} />
        </Grid>
        <Grid item xs={3}>
          <FormControl sx={{ m: 1 }}>
            <FormControlLabel
              control={<Checkbox checked={builder.isStart()} onChange={({ target }) => handleRangeStartIncludeChange(target.checked)} />}
              label={<FormattedMessage id='dt.cell.range.include' />} />
          </FormControl>
        </Grid>
        <Grid item xs={9}>
          <TextField type='number' fullWidth
            label={<FormattedMessage id='dt.cell.range.end' />}
            value={builder.getEnd()}
            onChange={({ target }) => handleRangeEndChange(target.value)} />
        </Grid>
        <Grid item xs={3}>
          <FormControl sx={{ m: 1 }}>
            <FormControlLabel
              control={<Checkbox checked={builder.isEnd()} onChange={({ target }) => handleRangeEndIncludeChange(target.checked)} />}
              label={<FormattedMessage id='dt.cell.range.include' />} />
          </FormControl>
        </Grid>
      </Grid>
    </Box>
  }


  return builder.getType() === 'range' ? rangeRenderer() : comparisonRenderer()
}
