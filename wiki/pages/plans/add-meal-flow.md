---
created: 2026-08-24
type: plan
tags: [add-meal, meal-entry, llm, photo-scan, implementation]
related: [[specs/add-meal-flow]] [[project-overview]]
---

# Plan — Add Meal Flow

Links to [[specs/add-meal-flow]]. Implements the "Add Meal" button flow.

## Step-by-step

1. **Entry point** — replace the `onAddMeal` TODO in `ui/MainScreen.kt:67` with a `ModalBottomSheet`.
2. **State machine** — new `AddMealViewModel` with a sealed `AddMealStep`: `ChooseMethod → ManualPick | ManualNew | PhotoCapture → ScanDraft → Done`.
3. **Manual pick** — search catalog, select dish, choose meal type + portion weight, save log.
4. **Manual new dish** — form (name, meal type, portion weight, total kcal); save dish + log (ingredients deferred).
5. **Photo capture** — camera + gallery pick, preview, retake/use.
6. **Scan draft** — editable proposal card + single clarify prompt; save creates optional catalog dish + log.
7. **LLM layer** — `MealAnalyzer` interface + Silicon Flow impl (OpenAI-compatible JSON mode).
8. **Data write paths** — add insert/getById methods; wire new dependencies.

## Files to create

- `ui/addmeal/AddMealSheet.kt` — bottom-sheet entry (Scan / Manual + recent dishes).
- `ui/addmeal/AddMealViewModel.kt` — state machine + draft/proposal state.
- `ui/addmeal/ManualDishPicker.kt` — search + select + meal type + weight.
- `ui/addmeal/ManualNewDish.kt` — new-dish form.
- `ui/addmeal/PhotoCapture.kt` — camera/gallery + preview.
- `ui/addmeal/ScanDraft.kt` — editable proposal card + clarify prompt.
- `data/llm/MealAnalyzer.kt` — interface `suspend fun analyze(image: ByteArray): MealScanResult`.
- `data/llm/MealScanResult.kt` — result DTO.
- `data/llm/SiliconFlowMealAnalyzer.kt` — OpenAI-compatible implementation (image base64, JSON mode).
- `data/llm/network/*` — Retrofit/OkHttp API service + request/response DTOs.

## Files to modify

- `ui/MainScreen.kt` — wire `onAddMeal` to open the sheet; pass analyzer.
- `BodySquirrelApplication.kt` — expose `MealAnalyzer` (and network client).
- `data/local/dao/DishDao.kt` — add `insert`, `getById`.
- `data/local/dao/IngredientDao.kt` — add `insert`.
- `data/local/dao/DishIngredientDao.kt` — add `insert`.
- `data/local/repository/CatalogRepository.kt` / `MealRepository.kt` — expose write methods (`insertDish`, `insertIngredient`, `insertLog`).
- `gradle/libs.versions.toml` + `app/build.gradle.kts` — add networking + serialization + camera dependencies.

## Dependencies to add

- Retrofit (or OkHttp) + kotlinx.serialization for the API.
- Camera: `ActivityResultContracts.TakePicture` + `PickVisualMedia` (avoids heavy CameraX dep), or CameraX if live preview is desired.

## Database migrations

None required — the existing schema already supports the flow. New entities (dish/ingredient) are written to existing tables.

## Testing checklist

- [x] Unit: `PortionCalculator` reuse for manual portion scaling.
- [x] Unit: `MealAnalyzer` JSON parsing of `MealScanResult` from a mock response.
- [x] Unit: `AddMealViewModel` state transitions (choose → pick → draft → done).
- [ ] Manual: photo capture, retake, scan, edit weights, accept/cancel.
- [ ] Manual: clarify prompt round → fallback to manual.
- [ ] Manual: catalog write-back (corrected scan becomes reusable dish).

## Risks & rollback

- **LLM output variability** — mitigated by JSON mode + a `MealScanResult` parser with graceful fallback to the manual form.
- **No network/camera yet** — new deps are additive; feature is isolated behind the sheet.
- **API key absent in some builds** — analyzer should no-op/disable scan gracefully when key is empty.
- **Rollback** — the flow is self-contained behind the `onAddMeal` callback; reverting the wiring restores the pre-feature behavior.
