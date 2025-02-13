# Task Management - Feature List - Iteration 1

1. **Create tasks**
    - Manually by user  
    - via REST API one-by-one or batch (bulk operation)

2. **Update tasks**
    - One by one
    - In bulk

3. **Admin board**
    - Admins can see and edit all tasks
    - Include filter/sort by/group by functionalities

4. **Task overview (common worker board)**
    - Show all tasks regardless of role/user assignment so that workers could be able to help users find their task handler or give a progress report
    - Include filter/sort by/group by functionalities
    - This view should redact personal information

5. **View assigned tasks and task details (personal worker board)**
    - Workers should be able to see all tasks assigned to them and their role
    - From this view, they should be able to expand a task to see its details and edit it

6. **Copy As/Linking/Parent-child tasks**
    - Add a linked sub-task within a parent
    - Could be used for grouping tasks together

7. **Remove tasks**
    - Tasks can never be truly deleted, but they can be archived
    - Archived tasks do not show up in any views where active tasks are shown

8. **Historization**
    - Tasks contain a history of all changes made to them
    - Option to rollback to earlier versions (in case of error, mistake, etc)

9. **Audit logging (admins)**
    - Keep a log of all actions taken by users in relation to tasks and messages
    - For security purposes, GDPR incidents, mistakes etc.
    - If admins suspect a user of malicious activity, they can check the audit log to see what actions the user has taken

10. **User and role visualization**
    - Overview of what users have what roles (for testing purposes)

11. **Process inactivity view**
    - See which processes were started X amount of time ago, and which have had no activity
    - This can be used to identify processes that are stuck or have been forgotten and need to be cancelled

12. **File sharing / attachments**
    - Enable users and workers to attach files to tasks

13. **Messaging / communication / notification module**
    - This should be separate from the task system itself
    - This will enable users to communicate with workers before submitting a task (completing a form)
    - Also enables keeping track of individual messages (read status, who read it, when, etc.)
    - Activity feed (see live updates on who has started / finished a task, sent a message to user etc.)
    - External messages to users
    - Reminder notifications of new tasks, tasks with upcoming due dates, task completion
    - Automatically send a message to users about closing inactive tasks

14. **Search**
    - Search for tasks by name, user, role, etc.

15. **“Kanban/Sprint” style board to show current/planned data** needs proof, tentative
    - Provide a view of tasks that are in progress, completed, etc

16. **Data export for reporting**
    - Setting targets for KPIs:
      - workers can set targets for themselves or admins can set targets for workers and groups of workers
      - this can be used for reporting

17. **My dashboard** 
    - For individual workers
    - This could be a place where workers can see their assigned tasks, messages, notifications, etc. in a visually coherent way
    - Calendar view of tasks with due dates
    - Cards for counts of new messages, new tasks, etc.

18. **Error handling**
    - The system should be able to handle errors gracefully and notify the user of the error

19. **Task linking / dependencies**
    - Show that task A is related to task B
    - Task A blocks task B
    - Task A is blocked by task B
