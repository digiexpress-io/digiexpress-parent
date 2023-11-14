import React, {useState, useEffect, useRef} from 'react';
import {useSelector, useDispatch} from 'react-redux';
import { Search, Ref } from 'semantic-ui-react';
import {useDebounce} from 'use-lodash-debounce';
import escapeRegexp from 'lodash.escaperegexp';
import * as Defaults from '../defaults';
import {setActiveItem, showVariables} from '../actions';

const SearchMenu = () => {
  const items = useSelector(state => state.dialobComposer.form && state.dialobComposer.form.get('data'));
  const language = useSelector(state => (state.dialobComposer.editor && state.dialobComposer.editor.get('activeLanguage')) || Defaults.FALLBACK_LANGUAGE);
  const variables = useSelector(state => state.dialobComposer.form && state.dialobComposer.form.get('variables'));
  const [value, setValue] = useState('');
  const [results, setResults] = useState([]);
  const searchTerm = useDebounce(value, 500);
  const dispatch = useDispatch();
  const componentRef = useRef(null);

  useEffect(() => {
    if (componentRef && componentRef.current.firstChild) {
      componentRef.current.firstChild.classList.add('transparent');
    }
  });

  const navigate = (result) => {
    switch (result.type) {
      case 'item' :
        dispatch(setActiveItem(result.id));
        break;
      case 'variable' :
        dispatch(showVariables());
        break;
    }
  }

  useEffect(() => {
    if (!searchTerm || !items) {
      setResults([]);
      return;
    }
    const rxp = new RegExp(escapeRegexp(searchTerm), 'i');
    const itemMatcher = item =>
      rxp.test(item.get('id')) ||
      rxp.test(item.getIn(['description', language])) ||
      rxp.test(item.getIn(['label', language]));

    const itemResults = items.entrySeq()
                      .map(([k, v]) => v)
                      .filter(item => item.get('type') !== 'questionnaire')
                      .filter(itemMatcher)
                      .map((item, i) => (
                        {
                          childKey: item.get('id'),
                          title: item.getIn(['label', language]) || ('(Untitled)'),
                          description: item.get('id'),
                          type: 'item',
                          id: item.get('id')
                        }
                      )).toJS();

    const variablesResult = variables ? variables.filter(v => rxp.test(v.get('name')))
                       .map(v => (
                        {
                          childKey: v.get('name'),
                          title: v.get('name'),
                          description: v.get('context') ? 'Context' : 'Expression',
                          type: 'variable'
                        }
                       )).toJS() : [];

    const resultCategories = {};
    if (itemResults.length > 0) {
      resultCategories.items = {
      name: 'Items',
      results: itemResults
      };
    }
    if (variablesResult.length > 0) {
      resultCategories.variables = {
        name: 'Variables',
        results: variablesResult
      }
    }

    setResults(resultCategories);
  }, [searchTerm]);
  return (
    <Ref innerRef={componentRef}>
      <Search placeholder='Search' minCharacters={2} className='item' category value={value} onSearchChange={(e, {value}) => setValue(value)} results={results} onResultSelect={(e, {result}) => navigate(result)} />
    </Ref>
  );
}

export default SearchMenu;
