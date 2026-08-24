---
created: 2026-08-24
type: overview
tags: [architecture, tech-stack, kotlin, compose, room]
related: [[build-deploy]]
---

# Project Overview — Body Squirrel

Body Squirrel (working name "Bodybilka") is a tamagotchi-like fitness and meal tracker. Phase One is a meal tracker. This wiki documents the native Android implementation.

## Tech stack

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3) |
| Build | AGP 9.3.1, Gradle KTS, KSP 2.3.6 |
| Architecture | MVVM (ViewModel + StateFlow), single-module `:app` |
| Persistence | Room 2.8.4 |
| Navigation | navigation-compose 2.8.9 |
| DI | Manual (no Hilt/Koin); `BodySquirrelApplication` exposes repositories |
| Networking | None yet (planned for the LLM photo-scan feature) |
| LLM provider | Silicon Flow (key via `local.properties` → `BuildConfig.SILICON_FLOW_KEY`) |

## Package tree

```
com.otakeessen.bodysquirrel/
├── BodySquirrelApplication.kt   # app entry; lazily builds DB + repositories; seeds catalog
├── MainActivity.kt              # single activity, edge-to-edge, Compose content
├── data/
│   ├── MealType.kt              # enum BREAKFAST, LUNCH, DINNER, SNACKS, WATER
│   ├── MealTypeTotal.kt         # per-type kcal + water totals
│   ├── PortionCalculator.kt     # scale kcal by eaten weight
│   ├── DateUtils.kt             # ISO date helper
│   └── local/
│       ├── AppDatabase.kt       # Room DB v1 ("body_squirrel_database")
│       ├── SeedData.kt          # seed categories, ingredients, dishes
│       ├── entity/              # CategoryEntity, IngredientEntity, DishEntity,
│       │                        #   DishIngredientEntity, MealLogEntity
│       ├── dao/                 # CategoryDao, IngredientDao, DishDao,
│       │                        #   DishIngredientDao, MealLogDao
│       └── repository/          # CatalogRepository, MealRepository
└── ui/
    ├── MainScreen.kt            # Scaffold + bottom nav + NavHost
    ├── navigation/Destination.kt# Home, Meals, Progress, Profile
    ├── home/                    # HomeScreen + HomeViewModel
    └── theme/                   # Color, Theme, Type
```

## Key concepts

- **Meal types** — five fixed types including `WATER` (logged as ml rather than kcal).
- **Entities** — UUID primary keys (`String`). `DishEntity` = named dish with a portion weight + total kcal. `IngredientEntity` = name + kcal/100g. `DishIngredientEntity` = junction with per-ingredient weight (dish kcal = sum of ingredients). `MealLogEntity` = a logged meal on a date, referencing a dish (optional) + portion weight + kcal + water ml.
- **Categories** — `CategoryEntity` exists (tags concept) but no dish/ingredient↔category junction is wired yet.
- **Home screen** — fixed hero background (squirrel PNG) behind a scrollable `LazyColumn` of cards (energy, today's progress, daily tip). "Add Meal" button currently a TODO in `MainScreen.kt`.

## Current state / gaps

- "Add Meal" button not wired to any flow (`MainScreen.kt:67`).
- No networking, camera, or image libraries.
- Repositories only expose read/seed methods; no write paths for new dishes/ingredients.
- No dish↔category junction tables.

## Product vision (see `bodybilka-ideas.md` in author's Draft folder)

- Tamagotchi-like hero (squirrel) with animated HeroSection.
- Meal tracker first, then fitness.
- Manual + automatic (photo → LLM) meal entry.
- Catalog of dishes/ingredients with category tags.
