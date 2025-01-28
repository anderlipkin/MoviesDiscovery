# MoviesDiscovery 
The application that allows user to review the list of movies and add them to favorites. 

### Functional requirements:

- [x] The main screen contains two tabs: All and Favorites.
- [x] Tab All should display a list of movies with pagination. Movies should be grouped by month.
- [x] Tab Favorites should display a list of bookmarked movies.
- [x] User can add / remove movie to / from his favorites. All changes should be visible immediately on both tabs. Bookmarks can be saved localy (without api call).
- [x] User can refresh movies list using pull-to-refresh.
- [x] User can see early loaded content without internet connection (only 1st page, without pagination)
- [x] Movies list filtered (vote average: 7+ and vote count: 100+) and sorted by primary release date
- [x] Screen UI display one of the following states:
- Loading
- Refreshing
- Loading More (pagination)
- Error
- Content (just showing movies)

### Optional Requirements
- [ ] Implement sign-in with Google or Facebook
- [x] Implement movie sharing via any existing provider

### Tech stack
- [x] Kotlin
- [x] Room
- [x] MVVM
- [x] Clean Architecture as [recommended by Google](https://developer.android.com/topic/architecture).
- [x] Dependency injection with Koin
- [x] Jetpack Compose
- [ ] Unit-tests (under construction)

### Resources
The movies database: 
- docs: https://developer.themoviedb.org/docs
- api method: https://developer.themoviedb.org/reference/discover-movie
