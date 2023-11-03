export type UserEventType = 'task-completed' | 'message-sent' | 'checklist-completed' | 'checklist-self-assigned' | 'comment-finished' | 'task-blocked' | 'attachment-added';

export interface UserActivity {
  id: string;
  eventDate: string;
  eventType: UserEventType;
  subjectTitle: string;
}

export type UserId = string;

export type RoleId = string;

export interface Org {
  users: Record<UserId, User>;
  roles: Record<RoleId, Role>;
}

export interface Role {
  roleId: RoleId;
  avatar: string;
  displayName: string;
}

export interface User {
  userId: UserId;
  userRoles: RoleId[];
  displayName: string;
  avatar: string;
  activity: UserActivity[];
  type: 'PROJECT_USER' | 'TASK_USER'
}


export function resolveAvatar(values: string[]): { twoletters: string, value: string }[] {
  return values.map(role => {
    const words: string[] = role.replaceAll("-", " ").replaceAll("_", " ").replace(/([A-Z])/g, ' $1').replaceAll("  ", " ").trim().split(" ");

    const result: string[] = [];
    for (const word of words) {
      if (result.length >= 2) {
        break;
      }

      if (word && word.length) {
        const firstLetter = word.substring(0, 1);
        result.push(firstLetter.toUpperCase());
      }
    }
    return { twoletters: result.join(""), value: role };
  });
}
