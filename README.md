# ABAP Quick Fix plugin for ADT Additional Quick Fixes S4Conversions

Direct installation from https://leuc.de/abapquickfixs4c

ABAP Quick Fix Additions for S4 Conversion Projects.

## Features

#### Convert _select single_ into _select up to 1 rows with endselect_:

<img src="https://user-images.githubusercontent.com/90344490/173096279-8fd57c8b-8732-49a0-8c39-f059f7363277.gif" width="80%">

#### Convert old SQL style _select single_ into new style using '@' and commas in lists

#### Convert old SQL style _select / endselect_ or _select into table_ into new style using '@' and commas in lists

#### move-corresponding to corresponding #()
  
### Preferences

* Provide / change default _order by_ sequences for tables.
* Optional: automatically add a change comment preceeding the change.
* Optional: automatically uncomment changed code instead of replacing it.
* Optional: use new syntax using _field_.

### Changes

#### 1.4.0

* Switched to execution environment Java 17.
* Adapt to upstream changes (build 20221025).
* Allow various orders of clauses: into ..., where ..., from ...
* Fix and improve formatting and conversion to new styles.
* Re-init default order by list preferences.
* Do not process statements containing join.

#### 1.3.3

* Named groups in regular expressions.
* Allow _into table_ for _new syle_ fixes.
* Include initialization button for _order by_ lists in preferences.
* Fix issues with replacements and formatting.

#### 1.2.2

* Respects upper case key words.

