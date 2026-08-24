# Body Squirrel — Project Wiki

A project-specific knowledge base for the Body Squirrel (Bodybilka) app. It lives in `wiki/` and is version-controlled alongside the code.

## Structure

```
wiki/
├── AGENTS.md                  # this file
├── index.md                   # catalog of every page, organized by category
├── log.md                     # chronological, append-only log of all operations
├── pages/
│   ├── project-overview.md    # tech stack, architecture, folder structure, key concepts
│   ├── build-deploy.md        # build, test, lint, deploy instructions
│   ├── specs/                 # feature specifications (pre-implementation)
│   ├── plans/                 # implementation plans (pre-implementation)
│   ├── tickets/               # post-implementation summaries
│   └── research/              # research, analysis, investigation
```

## Page conventions

Every page MUST have YAML frontmatter with at minimum:

```yaml
---
created: YYYY-MM-DD
type: overview | build-deploy | spec | plan | ticket | research
tags: [tag1, tag2]
related: [[page-slug]] [[other-page]]
---
```

- `created` — ISO 8601 date the page was first created. **Never changes after creation.**
- `type` — page category (one of the six listed above).
- `tags` — freeform tags for searching and cross-referencing.
- `related` — optional wikilinks to connected pages.

Use **wikilinks** (`[[Page Name]]`) for cross-references. Spec pages link to their plan page; plan pages link to their spec and eventual ticket page.

When page content is updated, append an `## Updates` section at the bottom — never alter the `created` date.

## Slug conventions

- Specs and plans use the same slug (feature name, 2-5 words, lowercase, hyphen-separated), e.g. `add-meal-flow`.
- Tickets: `issue-<number>-<brief>`.
- Research: natural topic name, lowercase, hyphen-separated.

## Operations

- **Init** — create structure, auto-scan project for overview + build-deploy, write AGENTS/index/log.
- **Spec** — `pages/specs/<slug>.md`, update index + log.
- **Plan** — `pages/plans/<slug>.md`, update index + log.
- **Ticket** — `pages/tickets/<ticket-slug>.md`, update index + log.
- **Research** — `pages/research/<slug>.md`, update index + log.
- **Query** — read index first, drill in, answer with citations.
- **Lint** — check orphans, missing frontmatter, spec/plan pairing, stale content.
- **Update** — edit page, append `## Updates`, append log entry, never change `created`.

## log.md format

```
## [YYYY-MM-DD] <action> | <description>
```

## index.md format

Content-oriented catalog, organized by category (`## Overview`, `## Specs`, `## Plans`, `## Tickets`, `## Research`), each entry a wikilink + one-line summary + creation date.
