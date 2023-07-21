# Tasks UI

## Development guidelines TODO

### Dev "space"

#### Description
* Dev is a place for testing components.  Before actually implementing components in the “real” codebase, they should be implemented and tested in Dev first.  
* The intention is that we can catch bugs in Dev before they spread into the live code.
* If needed, multiple Dev-s can be created, such as Dev1, Dev2, etc.  

### Usage

* Declare components in their appropriate folders.
* Import and implement them in Dev.
* **Do not declare components within Dev.**

See example: 


```typescript
import TaskOps from '../TaskOps';

const Dev: React.FC = () => {

  return (
    <>
       <Box>COMPONENT 1 create task preview</Box>
       <TaskOps.CreateTaskView />       
    </>);
}
```

### Naming conventions

To ensure consistency, there are certain naming conventions that must be followed.

#### Styled components

All components in the `styles/` folder have the prefix `Styled...`.

Example:  `StyledButton`, `StyledLink` 


#### Other components

* Names should be as concise as possible
* Names should clearly describe what a component does, or what it is for

Good example (Folder name/component name):

`TaskHistory/TaskHistoryTable` 

Bad example: **too long**

`TaskHistory/ViewTaskHistoryTable`

Bad example: **unclear**

`TaskHistory/TaskView`


#### Translation naming

* Translations have a prefix
* The prefix is a path to where the translation is used
* The path starts with the Module (top level folder -- `application` or `core` or `styles`, etc.), and continues with Module sub-folder -- `AdminBoard` or `Reporting` or `SearchBoard`, etc. 


Example prefix (path):

`Module/Folder/...`  
`core/AdminBoard/...`

translation:

`'core.adminBoard.xxx'`

The rest of the translation is open for interpretation, but as always, it should be short, clear, and to the point.

#### Global translations

Some translations are global and must always be declared in the same way:

Button translations

* `buttons.xxx`
* `buttons.cancel`

Activities

* `activities.xxx`

TODO


