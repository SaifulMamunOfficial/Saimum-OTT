<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\UserPlaylist;
use App\Models\UserPlaylistSong;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

class PlaylistController extends Controller
{
    public function index()
    {
        $user = Auth::user();
        $playlists = UserPlaylist::where('user_id', $user->id)
            ->withCount('songs')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $playlists
        ]);
    }

    public function store(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
        ]);

        $playlist = UserPlaylist::create([
            'user_id' => Auth::id(),
            'name' => $request->name,
            'is_public' => $request->is_public ?? false,
        ]);

        return response()->json([
            'success' => true,
            'data' => $playlist
        ]);
    }

    public function show($id)
    {
        $playlist = UserPlaylist::where('user_id', Auth::id())
            ->with(['songs'])
            ->findOrFail($id);

        return response()->json([
            'success' => true,
            'data' => $playlist
        ]);
    }

    public function addSong(Request $request, $id)
    {
        $request->validate([
            'song_id' => 'required|integer',
        ]);

        $playlist = UserPlaylist::where('user_id', Auth::id())->findOrFail($id);

        $song = UserPlaylistSong::firstOrCreate([
            'playlist_id' => $playlist->id,
            'song_id' => $request->song_id,
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Song added to playlist'
        ]);
    }

    public function removeSong(Request $request, $id)
    {
        $request->validate([
            'song_id' => 'required|integer',
        ]);

        UserPlaylistSong::where('playlist_id', $id)
            ->where('song_id', $request->song_id)
            ->delete();

        return response()->json([
            'success' => true,
            'message' => 'Song removed from playlist'
        ]);
    }

    public function destroy($id)
    {
        UserPlaylist::where('user_id', Auth::id())->where('id', $id)->delete();

        return response()->json([
            'success' => true,
            'message' => 'Playlist deleted'
        ]);
    }
}
