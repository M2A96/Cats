# Cats App

A modern Android application showcasing cat breeds using Clean Architecture, Jetpack Compose, and MVI pattern.

## Features

- Browse cat breeds with infinite scrolling
- Search functionality with debouncing
- Favorite breeds management
- Detailed breed information with images
- Modern Material 3 UI design

## Architecture

The application follows Clean Architecture principles with an MVI (Model-View-Intent) pattern for the presentation layer:

### Layers

- **Presentation**: Implements MVI pattern using Jetpack Compose and ViewModels
  - UI States (HomeScreenState, DetailScreenState)
  - Events (HomeScreenEvent, DetailScreenEvent)
  - ViewModels manage state and handle user actions

- **Domain**: Contains business logic and use cases
  - Use Cases: GetCatBreedsUseCase, SearchBreedsUseCase, UpdateFavoriteStatusUseCase
  - Models: Cat, Resource

- **Data**: Handles data operations and external services

### Key Technologies

- **Kotlin**: Primary development language
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Retrofit**: Network operations
- **Room**: Local data persistence
- **Coroutines & Flow**: Asynchronous programming
- **Material 3**: UI design system

## Setup Requirements

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 11
- Android SDK 35 (compileSdk)
- Minimum SDK: 24

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app using Android Studio

## Build Configuration

```kotlin
android {
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }
}
```

## Development Practices

- **Code Style**: Enforced by ktlint
- **Architecture**: Clean Architecture with MVI pattern
- **State Management**: Unidirectional data flow with ViewModels and StateFlow
- **Testing**: JUnit for unit tests, Espresso for UI tests

## Libraries

- **UI & Compose**
  - androidx.activity:activity-compose
  - androidx.compose.material3:material3
  - androidx.compose.ui:ui

- **Networking**
  - retrofit2
  - okhttp3
  - gson

- **Dependency Injection**
  - dagger.hilt.android

- **Navigation**
  - androidx.navigation:navigation-compose

- **Local Storage**
  - androidx.room:room-runtime
  - androidx.room:room-ktx

## Project Structure

```
app/src/main/
├── java/io/maa96/cats/
│   ├── data/           # Data layer implementation
│   ├── domain/         # Business logic and models
│   │   ├── model/
│   │   └── usecase/
│   └── presentation/   # UI layer
│       └── ui/
│           ├── home/   # Home screen components
│           └── details/# Detail screen components
└── res/               # Resources
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.