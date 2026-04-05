---
name: agility
description: >
  Look up stories and work items on the Digital.AI Agility platform using the
  `agility` CLI. Use this skill when the user wants to view, reference, or
  retrieve details about a story or work item by its number (e.g., S-1001),
  or when working in a project that uses Digital.AI Agility for issue tracking.
compatibility: Requires the `agility` binary on PATH and a configured ~/.agility/config.json or AGILITY_URL / AGILITY_ACCESS_TOKEN environment variables.
---

## Prerequisites

- The `agility` binary must be on your PATH
- Configuration at `~/.agility/config.json`:
  ```json
  {
    "url": "https://your-instance.agility.com",
    "accessToken": "your-access-token"
  }
  ```
  Or set `AGILITY_URL` and `AGILITY_ACCESS_TOKEN` environment variables.

## Commands

### View a story

```bash
agility story view <storyNumber>
```

Returns JSON with the story's number, name, and description.

**Example:**

```bash
agility story view S-1001
```

**Output:**

```json
{
  "Number": "S-1001",
  "Name": "Implement login page",
  "Description": "As a user, I want to log in so that..."
}
```

**Exit codes:**
- `0` — story found
- `1` — story not found
