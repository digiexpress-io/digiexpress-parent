/*-
 * #%L
 * hdes-dev-app-ui
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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
import { Component } from 'inferno'
import { Row } from './Row'


export class Header extends Component {
  render() {
    const { view } = this.props
    const result = [];
    result.push(<Row id={view.id.start} keyword={view.id.keyword} value={view.id.value} />);

    if(view.description) {
      result.push(<Row id={view.description.start} keyword={view.description.keyword} value={view.description.value} />);
    }

    //const description = <Row />
    return result
  }
}
