---
created: 2026-08-24
type: spec
tags: [add-meal, meal-entry, llm, photo-scan, ui-ux]
related: [[plans/add-meal-flow]] [[project-overview]]
---

# Spec — Add Meal Flow

## Summary

Wire up the "Add Meal" button to a dialog offering two entry methods: **manual** (pick a dish from the catalog or create a new one) and **photo/scan** (send a picture to a user-chosen LLM, then review and correct the result before saving).

## Requirements

- Tapping "Add Meal" opens an entry dialog/sheet with two clear options: **Scan / Photo** and **Enter manually**.
- **Manual path:**
  - Search and pick an existing dish from the catalog; select meal type and portion weight; log it.
  - Add a new dish (name, meal type, portion weight, total kcal) and log it.
- **Photo path:**
  - Capture (camera) or pick (gallery) a photo; preview with retake.
  - Send the image to the selected LLM provider.
  - LLM returns a structured result (dish name, items with weights/confidence, total kcal, optional clarification).
  - Present the result as an **editable draft** the user can accept or correct.
  - If the model is uncertain, surface a single clarification question with tappable options.
  - On save, persist the meal log and (optionally) a reusable catalog dish.

## Scope

**In scope (v1):**
- Bottom-sheet entry with two options.
- Manual dish pick (search + meal type + weight) and manual new dish (name + kcal + weight).
- Photo capture/pick, preview, LLM structured scan, editable draft card, single clarify round, save.
- Pluggable `MealAnalyzer` interface with a Silicon Flow implementation.

**Out of scope (deferred):**
- Full ingredient builder in the manual "new dish" form (v1 stores name + kcal + weight only).
- Provider selection UI (Settings screen) — the interface is pluggable, the UI comes later.
- Multi-round free-form chat with the squirrel persona.
- Dish/ingredient category tags and dish↔category junction tables.
- On-the-fly icon generation.

## Design decisions

1. **Editable structured card, not a chat.** The LLM output is a structured proposal (editable fields, weight steppers, swappable ingredient chips), not prose. Chat bubbles appear only for targeted disambiguation.
2. **Human-in-the-loop confirm.** The LLM never writes to the DB; saving is an explicit user action.
3. **Manual "new dish" = name + kcal + weight first.** Fast path; ingredient breakdown added later.
4. **Pluggable LLM layer from day one.** `MealAnalyzer` interface + Silicon Flow impl (OpenAI-compatible chat-completions, JSON mode).
5. **Bounded clarification.** Max 1 clarify round, then fall back to the manual editor.

## Constraints

- min SDK 24; single module; no DI framework (manual wiring via `BodySquirrelApplication`).
- Silicon Flow key comes from `BuildConfig.SILICON_FLOW_KEY`.
- Room DB v1, UUID `String` primary keys, no migrations planned yet.
- LLM must not block the UI; provide loading + cancel + a manual fallback at every step.

## UI/UX considerations

- **Entry:** `ModalBottomSheet` with two large visual options (and optionally a "recent dishes" row for one-tap logging).
- **Confidence surfaced:** "I think this is oatmeal (~80%)" with a fast "not quite / something else" affordance.
- **Inline correction beats conversation:** weight steppers + swappable ingredient chips are faster than re-explaining.
- **Recoverable:** cancel the scan and fall back to manual at any point.
- **Learnable:** a corrected scan can be saved as a reusable dish so the same mistake isn't repeated.
- **Non-prose LLM output:** enforce a JSON schema mapped to existing entities.

## Data model implications

- `MealScanResult` DTO (new) — `dishName`, `guessedMealType`, `items[{name, weightG, confidence}]`, `totalKcal`, `confidence`, `needsClarification`, `question`, `options[]`.
- New write methods on repos/DAOs (`DishDao.insert/getById`, `IngredientDao.insert`, `DishIngredientDao.insert`).
- Scan draft state is ephemeral ViewModel state, not a DB entity.
