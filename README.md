# THDR Estimator (Android)

Native Android Kotlin and Jetpack Compose rewrite of the offline-first House DR EZ Estimator.

## Architecture & Persistence

- **UI**: Jetpack Compose + Material 3 (`enableEdgeToEdge()`, adaptive `NavigationBar` / `NavigationRail`).
- **Local Storage**: Room SQLite database (`thdr_estimator_db`) pre-seeded on first launch from `app/src/main/assets/pricebook_seed.json` (300 items across 12 categories) and `app/src/main/assets/timeframes_seed.json` (16 project timeframe benchmarks). All saved proposals and Price Book edits remain offline on the device.
- **Backup & Printing**:
  - Export and import full Price Book + Timeframe JSON backups via Android Storage Access Framework (`CreateDocument` / `OpenDocument`).
  - Print or save customer-ready estimate proposals as PDF via Android `PrintManager`.

## Core Screens

1. **EZ Estimator**: Proposal metadata, saved local proposals, Interior/Exterior area workflows, room-by-room dimensions & measurements, service pricing pulled from the Price Book with manual quantity/rate overrides, consolidated project materials, discount/tax, internal cost & profit summary, and Print/Save PDF.
2. **Price Book**: Searchable & filterable 300-item catalog with benchmark/low/high rate views, category-wide bulk editing, subcategory/hourly pricing editors, JSON backup export/import, and editable Project Timeframe Benchmarks.
3. **Materials**: Consolidated project material takeoff, custom material entries, and 6 construction calculators (Concrete, CMU, Framing, Drywall, Paint, Flooring & Tile).
