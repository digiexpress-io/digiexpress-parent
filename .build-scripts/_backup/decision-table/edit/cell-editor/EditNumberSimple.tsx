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
import { TextField } from '@material-ui/core';
import { FormattedMessage } from 'react-intl'
import { NumberBuilder } from '../builders'


export const EditNumberSimple: React.FC<{ builder: NumberBuilder, onChange: (value: string) => void }> = ({ builder, onChange }) => {
  return (<TextField type='number' fullWidth
        label={<FormattedMessage id='dt.cell.newvalue' />}
        value={builder.getValue()}
        onChange={({target}) => onChange(target.value)} />);
}
