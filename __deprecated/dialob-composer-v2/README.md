# dialob-composer

## Exports

```javascript
export {
  DialobComposer,                   // React component for Dialob Composer
  createDialobComposerReducer,      // Function that creates Composer's Redux reducer
  createDialobComposerMiddleware,   // Function that creates Composer's Redux middleware
  DEFAULT_ITEM_CONFIG,              // Default item editor configuration
  DEFAULT_ITEMTYPE_CONFIG,          // Default item type configuration
  DEFAULT_VALUESET_PROPS,           // Default valueset extra property configuration
  Item,                             // Base class for item editor React components
  SimpleField,                      // "Simple field"-type dialob component editor
  Group,                            // "Group"-type dialob component editor
  connectItem,                      // Function that connects React component to Composer's Redux store
  ItemMenu,
  DialobActions,                    // Action API for modifying Dialob form
  PropEditors,                      // React components for built-in item prop types
  MarkdownEditor                    // Markdown editor component with preview
};
```

## Embedding

Connect Composer's Redux reducer and middleware to your application store

```javascript
const reducers = {
  dialobComposer: createDialobComposerReducer()
};
const reducer = combineReducers(reducers);
const store = createStore(reducer, applyMiddleware(...createDialobComposerMiddleware()));
```

Embed react component

```jsx
  <DialobComposer formId={FORM_ID} configuration={DIALOB_COMPOSER_CONFIG}/>
```

## Configuration

```javascript
const DIALOB_COMPOSER_CONFIG = {
  transport: {
    csrf: {
      headerName: window.COMPOSER_CONFIG.csrfHeader,
      token: window.COMPOSER_CONFIG.csrf
    },
    apiUrl: window.COMPOSER_CONFIG.backend_api_url,
    previewUrl: window.COMPOSER_CONFIG.filling_app_url,
    tenantId: window.COMPOSER_CONFIG.tenantId,
  },
  documentationUrl: '',
  itemEditors: DEFAULT_ITEM_CONFIG,
  itemTypes: DEFAULT_ITEMTYPE_CONFIG,
  valueSetProps: CUSTOM_VALUESET_PROPS,
  postAddItem: (dispatch, action, lastItem) => {},
  closeHandler : () => {}
};
```

* **transport** - Transport configuration, CSRF header, token etc.
* **apiUrl** - URL for Dialob backend service API
* **previewUrl** - (Optional) URL for Dialob Filling preview application. If omitted, "Preview" feature is disabled.  `/<sessionId>` is appended to the URL for preview.
* **itemEditors** - Configuration for item editors
* **documentationUrl** - (Optional) URL for user guide documentaion, defaults to `https://docs.dialob.io/`
* **itemTypes** - Configuration for item types
* **valueSetProps** - (Optional) configuration for custom properties for valueset entries
* **postAddItem** - (Optional) callback function that gets called after a new item gets added to a form. Arguments: `dispatch` - Redux dispatch for dispatching additional actions into composer state, `action`- The Redux action that was used for creating the item, `lastItem` - The item that was added (including ID). Use this, for example, to create addtitional form structure depending on the created item, communicate to other parts of application etc.
* **closeHandler** - JS function that is called when toolbar `X` button is clicked.

### Item type configuration

For exmaple: `src/defaults/itemTypes.js`

Item type configuration corresponds to "Add new" item creation menu structure and also defines available item types including their configuration

```javascript
 DEFAULT_ITEMTYPE_CONFIG = {
   categories: [
    {
      title: 'Structure',
      type: 'structure',
      items: [
        {
          title: 'Group',
          optionEditors: [
            {name: 'Additional option', editor: GenericOptionEditor}
          ],
          propEditors: {
            columns: {
              component: PropEditors.InputProp,
              props: {
                type: 'number',
                min: 1,
                max: 20
              }
            }
          },
          config: {
            type: 'group',
            props: {
              columns: 1
            }
          }
        },
        {
          title: 'Multi-choice',
          convertible: ['list'],
          propEditors: {
            display: {
              component: PropEditors.ChoiceProp,
              props: {
                options: [
                  {key: 'dropdown', label: 'Dropdown'},
                  {key: 'button', label: 'Button'},
                  {key: 'checkbox', label: 'Checkbox'}
                ]
              }
            }
          },
          config: {
            type: 'multichoice',
            props: {
              display: 'dropdown',
            }
          }
        }
      ]
    },
        // ....
      ]
    },
    // ....
  ]
 }
```

* `categories` defines top-level categories, category object contains following attributes:
  * `Title` Label used in UI
  * `type` Category type, allowed values: `structure`, `input`, `output` - These are used to limit certain categories of items to be added into form depending on conditions
  * `items` Array of item configurations within this category. Item objects contain following attributes
    * `title` Label used in UI
    * `convertible` (Optional) Array of item type identifiers into which this item can be converted. Entries are first matched by `view` attribute, if not found then by `type`. If omitted, item can't be converted to other types.
    * `optionEditors` (Optional) Array of additional pages for item options dialog. Array of objects: `{name: 'Title of page', editor: OptionEditorComponent}` (see below)
    * `propEditors` (Optional) if custom property editors are configured for item. If prop editor is not defined, it will be fallen back to plain text. Editor configuration is set of objects having prop name as a key:
        * `component` : React component to use for editing the prop
        * `props` : (Optional) Additional properties for the editing component. (see below)
    * `config` : Snippet of Dialob form item configuration (See Dialob Form API). Any predefined structure is supported. only mandatory attribute is `type`. Item's default ID will be based on `view` attribute falling back to `type`

**Note!** `props` Are item specific properties that are available at filling time

#### Prop editor configrurations

Built in editors:

* `PropEditors.InputProp` - Plain input component, supports HTML `<input>` attributes as props for defining input type etc.

* `PropEditors.ChoiceProp` - Dropdown selection list, `options` prop having an array of `{key: '', label: ''},` entries, where `key` is value stored in prop and `label` is text displayed in UI.

* `PropEditors.BoolProp` - Boolean switch.

* `PropEditors.MultiChoiceProp` - Dropdown selection list, allows multiple selections (Array of string value). `options` prop as in `ChoiceProp`, `allowAdditions` true/false -- Allow adding arbitrary strings to list.

Custom editing component template
```javascript
const CustomProp = ({ onChange, value, name, item, ...props }) => {
  // onChange(value) - callback function for setting the prop's value
  // value - current value of the prop
  // name - prop name
  // item - item data for current item. (Immutable.Map)
  // props - additional editor component props passed on from configuration

  // Return react component here that renders UI for prop editor
  return (<Input onChange={(e) => onChange(e.target.value)} value={value || ''} {...props} />);
};
```

#### Item option dialog pages

Custom item option dialog page component template
```javascript
const OptionEditorComponent = ({item}) => {
  // item - item data for current item. (Immutable.Map)

  // Return react compinent here that renders UI for option dialog page
}
```

### Item editor configuration

For example: `src/defaults/itemConfig.js`

Item editor configuration defines which kind of item editing components are used in which conditions.

*WIP*

### Valueset property configuration

This is for controlling additional metadata properties for value set entries that are stored with form data, but *not communicated to filling side*.

For example: `src/defaults/valueSetProps.js`

```javascript
export const DEFAULT_VALUESET_PROPS = [
  {
    title: 'Custom attribute',
    name: 'attr',
    editor: GenericValueSetPropEditor
  }
];
```

* `title` Title shown in table column header
* `attr` attribute name
* `editor` React component for editing the value

Valueset entry metadata prop editor React component template
```javascript
const CustomValueSetProp = ({ onChange, value }) => {
  // onChange(value) - callback function for setting the prop's value
  // value - current value of the prop

  // Return react component here that renders UI for prop editor
};
```
