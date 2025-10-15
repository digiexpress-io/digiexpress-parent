# Understanding Our Audit System: A Simple Guide

## What Is Audit Data?

Imagine you're writing in a diary that you can never erase. Every time something important happens, you write it down with the date, time, and what happened. That's basically what our audit system does for the digital service platform.

When citizens like Marja submit requests, or when government workers like Juhani process those requests, the system creates a permanent record of what happened. This record can never be changed or deleted (well, almost never - we'll explain later).

## Why Do We Track Everything?

Think about these situations:

**Accountability**: If Marja asks "Who looked at my building permit application?", we can tell her exactly which workers viewed it and when.

**Transparency**: If something goes wrong with a request, we can look back and see exactly what happened, step by step.

**Safety**: If someone tries to do something they shouldn't, we have a record of it.

**Trust**: Citizens know their information is being handled properly because everything is tracked.

## A Real Example: Marja's Building Permit

Let's follow what happens when Marja submits a building permit request:

```
Day 1, 9:00 AM
┌─────────────────────────────────────────┐
│ Marja submits building permit request   │
│ System creates: Record #1               │
│ - Who: Marja (anonymized)               │
│ - What: Created new permit request      │
│ - When: 2025-10-15 09:00:00            │
└─────────────────────────────────────────┘
           ↓
Day 1, 10:30 AM
┌─────────────────────────────────────────┐
│ Worker Juhani opens the request         │
│ System creates: Access Record           │
│ - Who viewed: Juhani (Worker ID: J123) │
│ - What: Viewed permit request           │
│ - When: 2025-10-15 10:30:00            │
└─────────────────────────────────────────┘
           ↓
Day 2, 2:00 PM
┌─────────────────────────────────────────┐
│ Juhani assigns request to Specialist    │
│ System creates: Record #2               │
│ - Who: Juhani                           │
│ - What: Assigned to Liisa               │
│ - Before: Unassigned                    │
│ - After: Assigned to Liisa              │
│ - When: 2025-10-16 14:00:00            │
└─────────────────────────────────────────┘
```

Each of these records is called a "commit" - like taking a snapshot of what changed.

## How the System Remembers Changes

The system works like a photo album where photos are connected in order:

```
Photo 1          Photo 2          Photo 3          Photo 4
(Created) -----> (Assigned) ----> (Updated) -----> (Completed)
   |                |                 |                 |
   └── Shows what   └── Shows what    └── Shows what   └── Shows what
       was created      changed         changed next     was finished
```

Each "photo" (commit) contains:
- **Before picture**: What things looked like before the change
- **After picture**: What things look like after the change
- **Timestamp**: Exactly when it happened
- **Who did it**: Which worker made the change (or if a citizen made it)
- **Description**: What happened in simple words

## Protecting Privacy

Here's something important: when Marja views her own request, the system records that "a citizen viewed their request" but it doesn't store her name in a readable way. Instead, it uses a special code that only Marja can prove belongs to her.

**For citizens:**
- Access is recorded with a special privacy code
- Workers can see "a citizen accessed this" but not which citizen
- The citizen themselves can verify "yes, that was me" when they log in

**For workers:**
- Access is recorded with their worker ID
- Citizens can see which workers looked at their request
- Supervisors can review worker activity

## What Happens to Old Data?

Sometimes, data needs to be removed - either because it's very old, or because someone requests it under privacy laws. Our system handles this in three careful stages:

### Stage 1: Hidden (Reversible)
```
┌──────────────────────┐
│ Request marked as    │
│ "archived"           │
│                      │
│ Still in system,     │
│ just hidden from     │
│ normal view          │
└──────────────────────┘
```
Like putting a file in the recycling bin. It's still there if you need it back.

### Stage 2: Compressed (Partially Reversible)
```
┌──────────────────────┐
│ All history          │
│ compressed into      │
│ one recovery file    │
│                      │
│ Original data        │
│ deleted              │
└──────────────────────┘
```
Like compressing all photos into a zip file and deleting the originals. You can still recover it, but it takes effort.

### Stage 3: Permanent Deletion (Not Reversible)
```
┌──────────────────────┐
│ Recovery file        │
│ deleted              │
│                      │
│ No way to get        │
│ data back            │
└──────────────────────┘
```
The recycling bin is emptied. The data is truly gone.

## Why This Matters

This audit system ensures:

✓ **Citizens trust the platform** - They know who accessed their information
✓ **Workers are accountable** - Every action is recorded
✓ **Problems can be investigated** - We can always look back and see what happened
✓ **Privacy is protected** - Citizen identities are kept private in access logs
✓ **Data can be removed** - When required by law, data can be properly deleted

## Summary

Think of our audit system as a complete video recording of everything that happens to each citizen request. We can pause at any moment and see:
- What the request looked like
- Who touched it
- What they changed
- When they did it

This recording can never be edited (until it's time to delete it properly), which means everyone can trust that the records are accurate and complete.

---

*This system helps our digital platform be transparent, trustworthy, and compliant with data protection laws while serving citizens and government workers effectively.*
