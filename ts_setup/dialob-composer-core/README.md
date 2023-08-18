# Dialob Form Editor Core

* `yarn install` - install deps
* `yarn build` - build library under lib/
* `yarn test` - run test cases

### Where's What?

* **src/types.ts** - Type definitions for Dialob Form document
* **src/reducer.ts** - Dialob form document editing functions + reducer function using Immer
* **src/actions.ts** - Action definitions for reducer

* **src/test** - Unit test cases and test data

* **src/react** - React specific extensions
* **src/react/ComposerContext** - Context and Provider for managing form document state using React's useReducer hook internally
* **src/react/useComposer** - React hook for form editing operations based on ComposerContext

## Types

`ComposerState` - Root type for Dialob form document  
`DialobItemTemplate` - Dialob item (question, group, page etc.) attributes.  
`DialobItem` - Dialob item type (DialobItemTemplate + id)  
`ValidationRule` - Validation rule entry  
`DialobITemType` - Supported dialob item types  
`ValueSet` - Valueset (choice list)  
`ValueSetEntry` - Valueset entry (choice list item)  
`ContextVariable` - Context variable definition  
`ContextVariableType` - Supported context variable types  
`Variable` - Variable definition  
`LocalizedString` - Value type for localized strings  

## API

## React

`ComposerContext` - React context for Dialob form document editing  
`ComposerProvider` - Context provider component for ComposerContext  
`useComposer` - React hook exposing form data and editing operations within ComposerContext  

