
# 2.9.4

*  Added new component "ListLabel" for displaying different types of list (Global, Empty, Local)
# 2.9.3

*  Added download and upload CSV features to TranslationDialog.js

# 2.9.2

*  Added 'Can add new row' and 'Can remove row' rule editing for rowgroup

# 2.9.1

* Dropped nwb build and introduced typescript

# 2.8.2

* Download feature for all versions.

# 2.8.0

It is now possible to create a global choice list from a local choice list and make a local copy of a global choicelist.

* In case of a local list, "Make Global" button will make the list global and switch to this list.
* If a global list is chosen for an item, "Copy as local" button will create a local copy of the choicelist.

# 2.7.0

Requires Dialob Backend v. 1.0.13 or newer

Show global valueset users with navigation to focus them

Descriptiont text for version tags

Configurable user guide documenation URL

# 2.6.0

Added `MultiChoiceProp` PropEditor
Added configuration for `address` item

# 2.5.1

Export `MarkdownEditor` component

# 2.5.0

Requires Dialob Backend v. 1.0.12 or newer

Added error message texts related to multirow aggregate functions

Added Form option to set all answers required

Show markdownguide website as MD editor help

# 2.4.0

Requires Dialob Backend v. 1.0.11 or newer

### Valueset entry visibility rule editing.

Click on valueset entry to see its visibility rule expression below the entry list.

### Context and Expression Variable publish control

Expression and Context variables have checkbox for publish control. When checkbox is checked, variable value is communicated to filling side. 

# 2.3.0

Upload and download valueset entries from CSV file.

For upload, click upload button in any valueset editor. CSV file structure:

|ID|en|fi|...|
|-|-|-|-|
|e1|English label|Finnish label|...
|e2|English label|Finnish label|...

* First row is header row
  * First column of header row must be `ID``
  * Rest of header columns must be two-letter language ID-s for labels
* First column of content rows is entry ID
* Rest of content colums are entry labels in corresponding language

**Notes**

* Empty rows are ignored
* Order of entries in the file is retained
* All defined languages are imported, in case form doesn't support a language, value remains hidden.
* All entry ID-s are imported as is, in case of conflicts or other problems, error messages will be displayed and these are to be resolved by user as normal.

For download, click download button in any valueset editor. Valueset entries are downloaded as CSV file in the same format as described above.

**Following import modes are supported**

* **Replace all** Replaces all valueset entries with values from file
* **Append** Appends values from file to existing valueset entries
* **Update** Updates existing entries by ID and adds new entries from file

# 2.1.2-5

Visibility and requirement rules are now edited in a separate section in the bottom of the page. Activate an item to edit.

# 2.1.2

Visibility and Requirement rules are now hidden for all items that are not being edited. When item is clicked and activated, all options will be visible.
> "For performance reasons."
