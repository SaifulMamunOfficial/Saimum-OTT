<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\HomeController;
use App\Http\Controllers\Api\SongController;
use App\Http\Controllers\Api\StudentHubController;
use App\Http\Controllers\Api\FavoriteController;
use App\Http\Controllers\Api\PlaylistController;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
*/

// Public Routes
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);
Route::get('/home', [HomeController::class, 'index']);
Route::get('/search', [HomeController::class, 'search']);
Route::get('/album/{id}', [HomeController::class, 'albumDetails']);
Route::get('/artist/{id}', [HomeController::class, 'artistDetails']);
Route::get('/banner/{id}', [HomeController::class, 'bannerDetails']);
Route::get('/all-songs', [HomeController::class, 'allSongs']);
Route::get('/all-albums', [HomeController::class, 'allAlbums']);
Route::get('/all-artists', [HomeController::class, 'allArtists']);

Route::post('/songs/{id}/increment-views', [SongController::class, 'incrementViews']);

// Protected Routes (Require Token)
Route::group(['middleware' => ['auth:sanctum']], function () {
    Route::get('/profile', [AuthController::class, 'profile']);
    Route::post('/logout', [AuthController::class, 'logout']);
    
    // Student Hub Routes
    Route::get('/student/dashboard', [StudentHubController::class, 'dashboard']);

    // Favorite Routes
    Route::get('/favorites', [FavoriteController::class, 'index']);
    Route::post('/favorites/toggle', [FavoriteController::class, 'toggle']);
    Route::get('/favorites/check', [FavoriteController::class, 'check']);
    
    // Playlists
    Route::get('/playlists', [PlaylistController::class, 'index']);
    Route::post('/playlists', [PlaylistController::class, 'store']);
    Route::get('/playlists/{id}', [PlaylistController::class, 'show']);
    Route::delete('/playlists/{id}', [PlaylistController::class, 'destroy']);
    Route::post('/playlists/{id}/songs', [PlaylistController::class, 'addSong']);
    Route::delete('/playlists/{id}/songs', [PlaylistController::class, 'removeSong']);
});
