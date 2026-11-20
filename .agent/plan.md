# Project Plan

Create an open-source project/library for the Obolo Bible (https://github.com/lumgwun/OboloBibles). The public can use this for their projects. We need to extract the Bibles from AndroidStudioProjects/OboloAnglicanApp.

## Project Brief

# Project Brief: Obolo Language Bibles

## Features
1. **Three Distinct Obolo Bibles:** Exposes the 'Obolo Jehovah Witness Bible' and two additional distinct Obolo Bible versions (extracted from OboloAnglicanApp) as the core offerings.
2. **Open-Source Public API:** Structured as a modular library that allows other developers to easily fetch, search, and integrate these Obolo Bible texts into their own applications.
3. **Bible Text Reader Interface:** A minimal reference UI in the sample app for selecting books, chapters, and verses, demonstrating the library's capabilities with a clean reading view.

## High-Level Tech Stack
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Navigation & Adaptive Strategy:** Jetpack Navigation 3 (state-driven routing) and Compose Material Adaptive library for fluid, multi-form factor layouts.
* **Concurrency:** Kotlin Coroutines (for lightweight, non-blocking background operations when parsing text assets).

## Implementation Steps

### Task_1_Setup_Library_Assets: Create the Android library module for the Bibles, extract the three Obolo Bibles, and include them as text assets, renaming the first to 'Obolo Jehovah Witness Bible'.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Library module is created
  - Bible texts are included as assets
  - First Bible is renamed correctly
- **StartTime:** 2026-11-20 10:25:17 WAT

### Task_2_Data_Access_API: Implement the open-source data access API in the library module using Kotlin Coroutines to parse the Bible assets and expose functionality to fetch, search, and retrieve books, chapters, and verses.
- **Status:** PENDING
- **Acceptance Criteria:**
  - API exposes clean methods to fetch books, chapters, and verses
  - Coroutines are used for non-blocking asset parsing
  - API is accessible publicly within the library module

### Task_3_Sample_App_UI: Build a Jetpack Compose sample app that consumes the library module. Implement a minimal Bible Text Reader UI for selecting books, chapters, and verses using Jetpack Navigation 3 and Compose Material Adaptive for multi-form factor support.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Sample app successfully integrates the library module API
  - User can select Bible version, book, chapter, and read verses
  - UI is implemented cleanly in Jetpack Compose

### Task_4_Run_and_Verify: Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, check the data access API functionality, and report any critical UI issues.
- **Status:** PENDING
- **Acceptance Criteria:**
  - project builds successfully
  - make sure all existing tests pass
  - build pass
  - app does not crash

