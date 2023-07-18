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
import { Hdes } from '../../deps';

import NumberBuilder from './NumberBuilder';
import StringBuilder from './StringBuilder';
import DateBuilder from './DateBuilder';
import BooleanBuilder from './BooleanBuilder';

const Builder = (props: {header: Hdes.AstAPI.DtHeader, value?: string}) => {
  const value = props.value ? props.value : '';
  const header = props.header;
  const type = header.value;
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

export {NumberBuilder, StringBuilder, DateBuilder, BooleanBuilder}
export default Builder;