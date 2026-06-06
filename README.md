# SnapRecipe 📷🍳

Snap a photo of food and discover recipes for it. SnapRecipe identifies the dish
or main ingredient in a photo using a vision AI model, then surfaces matching
recipes from [TheMealDB](https://www.themealdb.com/).

## How it works

1. **Capture** — take a photo with the camera or pick one from the gallery.
2. **Identify** — the image is resized, base64-encoded, and sent to an
   open-source vision model (Qwen2.5-VL via [OpenRouter](https://openrouter.ai/))
   which returns the name of the main food or ingredient.
3. **Search** — that food name is looked up against TheMealDB, first by
   ingredient and then by name as a fallback.
4. **Browse** — matching recipes are shown as cards; tap one for full
   ingredients, measures, step-by-step instructions, and a YouTube link.

The UI is a single-Activity Jetpack Compose app with a state-driven flow:
`Idle → Loading (Analyzing → Searching) → Results / NotFound / Error`.

## Tech stack

- **Language:** Kotlin (JVM target 17)
- **UI:** Jetpack Compose + Material 3, Navigation Compose
- **Architecture:** MVVM — `RecipeViewModel` + `RecipeRepository`, manual DI (`AppContainer` / `RetrofitClient`)
- **Networking:** Retrofit 2 + OkHttp + Gson
- **Images:** Coil for async image loading
- **APIs:** OpenRouter (vision/food recognition) and TheMealDB (recipes)

## Project structure

```
app/src/main/java/com/example/snaprecipe/
├── MainActivity.kt              # Compose host
├── SnapRecipeApplication.kt     # App + DI container
├── data/
│   ├── model/                   # Meal & chat (OpenRouter) data classes
│   ├── remote/                  # Retrofit services + RetrofitClient
│   └── repository/              # RecipeRepository — orchestrates both backends
├── di/                          # AppContainer (manual dependency wiring)
├── ui/
│   ├── navigation/              # NavHost + routes
│   ├── screens/                 # Home, Loading, Results, Detail, NotFound, Error
│   ├── components/              # MealCard, etc.
│   ├── state/                   # RecipeUiState, LoadingPhase
│   ├── viewmodel/               # RecipeViewModel
│   └── theme/                   # Colors, type, Material theme
└── util/                        # ImageUtils (resize + base64 encode)
```

## Setup

### Requirements

- Android Studio (recent stable)
- JDK 17
- Android SDK 34 (`minSdk` 24, `targetSdk` 34)
- A free [OpenRouter](https://openrouter.ai/) API key

### Configure the API key

The OpenRouter key is injected into `BuildConfig` from `local.properties` and is
never committed to source control. Add it to `local.properties` in the project
root:

```properties
OPENROUTER_API_KEY=sk-or-your-key-here
```

> TheMealDB requires no key (the free public test endpoint is used).

### Build & run

```bash
# from the project root
./gradlew installDebug      # build and install on a connected device/emulator
# or open the project in Android Studio and press Run
```

## Configuration notes

- **Vision model** — set in `RecipeRepository` (`MODEL` constant). Defaults to
  `qwen/qwen-2.5-vl-72b-instruct`; alternatives (including free-tier models) are
  listed in the surrounding comments.
- **Permissions** — `INTERNET` and `CAMERA`. The camera hardware is optional, so
  the app installs on camera-less devices and still supports gallery picking.

## License

No license file is currently included. Add one if you intend to distribute.
