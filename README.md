# Thena - JSON storage framework
  
Thena is a JSON storage framework with GIT-like features on top of a relational database.
* **Project Scope**: 
Multiple projects in the same installation with a separate set of tables for each project.
* **Immutability**: 
TODO::
* **CRUD operation based on commits**: 
Any changes to data are done using the commit concept. TODO::
* **Tagging**: 
TODO::
* **Branching**: 
TODO::
* **Full audit**:
Every change of the JSON that is manipulated(created, edited and even deleted) is stored and linked with the author.
* **Search from all versions**:
Supports queries over active and historic(every version that has existed) JSON documents.
* **Reactive streams**:
Vert.x and SmallRye Mutiny is used for reactive streams and reactive database drivers.
* **Relational database support**:
There are 2 implementations: generic ANSI SQL and PostgreSQL.


## Getting Started

* [Wiki](https://github.com/the-wrench-io/thena-parent/wiki)
