import { Org, User, Role } from './org-types';

export const mockRoles: Record<string, Role> = {
  "admin-role": {
    roleId: "admin-role",
    avatar: "AR",
    displayName: "Admin Role"
  },
  "water-department": {
    roleId: "water-department",
    avatar: "WD",
    displayName: "Water Department"
  },
  "education-department": {
    roleId: "education-department",
    avatar: "ED",
    displayName: "Education Department"
  },
  "elderly-care-department": {
    roleId: "elderly-care-department",
    avatar: "EC",
    displayName: "Elderly Care Department"
  },
  "sanitization-department": {
    roleId: "sanitization-department",
    avatar: "SD",
    displayName: "Sanitization Department"
  }
};

export const mockUsers: User[] = [
  {
    displayName: "OP MIFID",
    userId: "op-mifid",
    userRoles: ["admin-role"],
    avatar: "OP",
    type: 'PROJECT_USER',
    activity: []
  },
  {
    displayName: "Sippo",
    userId: "sippo",
    userRoles: ["admin-role"],
    avatar: "SO",
    type: 'PROJECT_USER',
    activity: []
  },
  {

    displayName: "Carrot Ironfoundersson",
    userId: "carrot ironfoundersson",
    userRoles: ["admin-role"],
    avatar: "CI",
    type: 'TASK_USER',
    activity: [
      {
        id: "activity1",
        eventDate: "2022/04/15",
        eventType: 'task-completed',
        subjectTitle: "Sewage water disposal"
      },
      {
        id: "activity2",
        eventDate: "2023/11/07",
        eventType: 'task-blocked',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity3",
        eventDate: "2022/03/04",
        eventType: 'checklist-self-assigned',
        subjectTitle: "checklist1"
      },
      {
        id: "activity4",
        eventDate: "2022/12/09",
        eventType: 'task-completed',
        subjectTitle: "Sewage water disposal"
      },
      {
        id: "activity5",
        eventDate: "2023/11/30",
        eventType: 'task-blocked',
        subjectTitle: "Request for elderly care"
      },
      {
        id: "activity6",
        eventDate: "2023/10/31",
        eventType: 'message-sent',
        subjectTitle: "Building permit"
      }
    ]
  },
  {
    displayName: "Sam Vimes",
    userId: "sam vimes",
    userRoles: ["admin-role"],
    avatar: "SV",
    type: 'TASK_USER',
    activity: [
      {
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
      }
    ]
  },
  {
    displayName: "Lord Vetinari",
    userId: "lord vetinari",
    userRoles: ["admin-role"],
    avatar: "LV",
    type: 'TASK_USER',
    activity: [
      {
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
      }
    ]
  },
  {
    displayName: "Lady Sybil Vimes",
    userId: "lady sybil vimes",
    userRoles: ["admin-role"],
    avatar: "LS",
    type: 'TASK_USER',
    activity: [
      {
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
      }
    ]
  },
  {
    displayName: "Nobby Nobbs",
    userId: "nobby nobbs",
    userRoles: ["admin-role"],
    avatar: "NN",
    type: 'TASK_USER',
    activity: [
      {
        id: "activity1",
        eventDate: "2023/10/31",
        eventType: 'checklist-completed',
        subjectTitle: "checklist2"
      },
      {
        id: "activity2",
        eventDate: "2022/12/09",
        eventType: 'task-blocked',
        subjectTitle: "General message"
      },
      {
        id: "activity3",
        eventDate: "2023/11/07",
        eventType: 'comment-finished',
        subjectTitle: "comment1"
      }
    ]
  },
]

export const mockOrg: {
  org: Org,
  user: User,
  today: Date
} = {
  org: {
    roles: mockRoles,
    users: mockUsers.reduce((acc, item) => ({ ...acc, [item.userId]: item }), {})
  },
  user: mockUsers[3],
  today: new Date(),
}