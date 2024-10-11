/*-
 * #%L
 * wrench-assets-ide
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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
import { HdesApi } from '../../../client';

import NumberBuilder from './TypeNumberBuilder';
import StringBuilder from './TypeStringBuilder';
import DateBuilder from './TypeDateBuilder';
import BooleanBuilder from './TypeBooleanBuilder';
import ValueSetBuilder from './TypeValueSetBuilder';

const Builder = (props: {header: HdesApi.TypeDef, value?: string, values?: string[]}) => {
  const value = props.value ? props.value : '';
  const header = props.header;
  const type = header.valueType;
  const values = props.values;
  if (values) {
    return new ValueSetBuilder({header, values});
  }
  if(type === 'INTEGER' || type === 'LONG' || type === 'DECIMAL') {
    return new NumberBuilder({value, header});
  } else if(type === 'STRING') {
    return new StringBuilder({value, header});
  } else if(type === 'DATE') {
    return new DateBuilder({value, header});
  } else if(type === 'DATE_TIME') {
    return new DateBuilder({value, header});
  } else if(type === 'BOOLEAN') {
    return new BooleanBuilder({value, header});
  }
  return {};
}

export { EditBoolean } from './EditBoolean';
export { EditString } from './EditString';
export { EditStringSimple } from './EditStringSimple';
export { EditNumber } from './EditNumber';
export { EditNumberSimple } from './EditNumberSimple';
export { EditDateTime } from './EditDateTime';
export { EditDateTimeSimple } from './EditDateTimeSimple';
export { EditDate } from './EditDate';
export { EditDateSimple } from './EditDateSimple';
export {NumberBuilder, StringBuilder, DateBuilder, BooleanBuilder}
export default Builder;