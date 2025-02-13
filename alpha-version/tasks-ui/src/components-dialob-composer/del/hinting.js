import {RESERVED_WORDS} from './language';

const IGNORED_ITEM_TYPES = [
  'questionnaire',
  'group',
  'note'
];

const hinter = (context, form) => {
  let result = RESERVED_WORDS
    .sort((a, b) => a.localeCompare(b))
    .map(w => ({
      text: w,
      className: 'del-hint-keyword'
    }));

  if (form) {

    const itemIds = form.get('data')
      .entrySeq()
      .map(e => e[1])
      .filter(i => IGNORED_ITEM_TYPES.indexOf(i.get('type')) === -1)
      .map(i => ({
        text: i.get('id'),
        className: 'del-hint-item'
      }))
      .toJS()
      .sort((a, b) => a.text.localeCompare(b.text));

    result = result.concat(itemIds);

    if (form.get('variables')) {
      const variables = form.get('variables')
        .map(v => ({
          text: v.get('name'),
          className: v.get('context') ? 'del-hint-variable-context' : 'del-hint-variable'
        }))
        .toJS()
        .sort((a, b) => a.text.localeCompare(b.text));

      result = result.concat(variables);
    }
  }

  if (!context.isEmpty) {
    result = result.filter(h => h.text.startsWith(context.content));
  }

  return result;
};

export {
  hinter as default
};
